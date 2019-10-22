package com.ubox.card.vs.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.ubox.card.config.CardJson;
import com.ubox.card.util.logger.Logger;

public class HttpsLogin {
	
	public static Context vmContext;
	
	public static void init(Context context){
		vmContext = context;
	}

	private static final String HTTPS_ACTION_URL = "/vcardServer/client/httpsServlet";
	private static final int    TIMEOUT          = 30 * 1000;

	/** https客户端登入字段 */
	private static final String VMID     = CardJson.vmId;
	private static final String USERNAME = CardJson.userName;
	private static final String PASSWORD = CardJson.password;
	private static final String ADDRESS  = CardJson.address;
	private static final int    PORT     = CardJson.httpsPort;

	/** https服务端反馈的字段 */
	private static String key;            // 密钥，用于传输数据的加密
	private static String sslSession;     // SSL验证通过生成的session，http同步数据的身份验证的标识
	private static int    code      = 0;  // 服务端认证反馈标识.1标识认证成功，-1标识认证失败
	private static String innerCode = ""; // 防止null异常

	/**
	 * 链接认证服务器
	 */
	public static void connect() { 
		loginCredentials(); 
	}

	/**
	 * 登入服务器，获取认证
	 */
	@SuppressWarnings("unchecked")
	private static void loginCredentials() {
		Logger.info("HTTPS login start");
		
		DefaultHttpClient client = new DefaultHttpClient();
		client.getParams().setParameter( CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT);
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, TIMEOUT);

		InputStream inStream = null;
		try {
			/* 证书文件信息读取到密钥库 */
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			inStream = vmContext.getAssets().open("card.bks");
			trustStore.load(inStream, "DEF!@#$%*123".toCharArray());
			SSLSocketFactory sockotFactory = new SSLSocketFactory(trustStore);
			Scheme sch = new Scheme("https", sockotFactory, PORT);
			client.getConnectionManager().getSchemeRegistry().register(sch);
			
			/* 组装登入信息 */
			Map<String, Object> message = new HashMap<String, Object>();
			message.put("vmId",       VMID);
			message.put("innerCode",  VMID);
			message.put("cpuId",      "BFEBFBFF00020655-0000000000000000");
			message.put("myUsername", USERNAME);
			message.put("myPassword", PASSWORD);

			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			qparams.add(new BasicNameValuePair("paramRequest", JSON.toJSONString(message)));
			//?vmId="+CardJson.vmId+"&appType="+CardJson.appType
			qparams.add(new BasicNameValuePair("vmId", CardJson.vmId));
			qparams.add(new BasicNameValuePair("appType", CardJson.appType+""));
			// 设置httpclient行为
			client.setHttpRequestRetryHandler(new HttpRequestRetryHandler() {
				@Override
				public boolean retryRequest(IOException exception, int executeCount, HttpContext context) {
					if (executeCount >= 5) { 
						return false; 
					}
					if (exception instanceof NoHttpResponseException) { 
						return true; 
					}
					if (exception instanceof SSLHandshakeException) { 
						return false; 
					}
					
					HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
					boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
					if (idempotent) { 
						return true;
					}
					
					return false;
				}
			});

			Logger.info("address=" + ADDRESS + ", httpsPort=" + PORT + ", HTTPS_ACTION_URL=" + HTTPS_ACTION_URL);

			/* 登入服务器 */
			URI uri = URIUtils.createURI("https", ADDRESS, PORT, HTTPS_ACTION_URL, URLEncodedUtils.format(qparams, "UTF-8"), null);
			HttpPost httpPost = new HttpPost(uri);
			Logger.info("URI = " + httpPost.getURI());

			HttpResponse response = null;
			try { response = client.execute(httpPost); } catch (Exception e) {
				Logger.error(e.getLocalizedMessage(), e);
				return ;
			}
			
			// https服务器反馈信息
			int statusCode  = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				httpPost.abort();
				Logger.warn("https:Server Error，statusCode:" + statusCode);
				return ;
			}

			HttpEntity entity = response.getEntity();
			if (entity == null) {
				Logger.warn("https:Server response entity == null");
			} else {
				String repsonseStr = EntityUtils.toString(entity, HTTP.UTF_8);
				Logger.info("https:Server-side response data=" + repsonseStr);
				
				// key,SSLSession赋值
				Map<String, Object> resp = (Map<String, Object>) JSON.parse(repsonseStr);
				key        = String.valueOf(resp.get("key"));
				sslSession = String.valueOf(resp.get("sslSession"));
				code       = ((Integer) resp.get("code")).intValue();
				innerCode  = String.valueOf(resp.get("innerCode"));
			}

			httpPost.abort();
		} catch (Exception e) {
			Logger.error("Read Certificate error.", e);
		} finally {
			/** 释放系统资源 */
			if (inStream != null) {// 关闭流
				try { 
					inStream.close(); 
				} catch (IOException e) { 
					Logger.error(e.getLocalizedMessage(), e); 
				}
			}
			
			client.getConnectionManager().shutdown();// 释放client实例占据的资源

			/** 记录反馈日志 */
			if (code == 1 && VMID.equals(innerCode)) {
				Logger.info("https:Login Success.vmId = " + VMID + ",innerCode = " + innerCode);
			} else {
				Logger.warn("Network connection exceptions.code = " + code + ",innerCode = " + innerCode);
			}

			/** 初始标识置空 */
			code      = 0;
			innerCode = "";
		}
	}

	public static String getKey() { 
		return key; 
	}

	public static String getSslSession() { 
		return sslSession; 
	}

	public static void setSslSession(String ssl) {
		sslSession = ssl;
	}
}
