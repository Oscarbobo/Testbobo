/*
 * Copyright (c) 2011 友宝中国. 
 * All Rights Reserved. 保留所有权利.
 */
package com.ubox.card.vs.http;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.ubox.card.config.CardJson;
import com.ubox.card.util.logger.Logger;

/**
 * @author miral.gu
 * @date 2012-5-14 上午11:51:03
 */
public class HttpDownloadCertificate {
	/** 下载验证信息 */
	private static final String INNERCODE = CardJson.vmId;
	private static final String USERNAME = CardJson.userName;
	private static final String PASSWORD = CardJson.password;
	private static final String CPUID = "BFEBFBFF00020655-0000000000000000";// TODO:暂时写死

	private static final String ADDRESS = CardJson.address;
	private static final int PORT = CardJson.httpPort;
	private static final String DOWNLOAD_URL = "/vcardServer/client/cert";
	private static final String VERIFCATION_FILE_NAME = "ubox.kst";

	/**
	 * 访问VcardServer证书下载接口
	 */
	public static void downCertificate() {
		Logger.info(">>>>>>>>> downCertificate start");

		HttpsLogin.setSslSession(null);// SSLSessoin设置成无效
		Logger.info(">>>>>>>>> 下载证书,设置SSLSessoin==null.");

		DefaultHttpClient httpClient = new DefaultHttpClient();
		try {
			/* 组装验证信息 */
			Map<String, Object> message = new HashMap<String, Object>();
			message.put("innerCode", INNERCODE);
			message.put("cpuId", CPUID);
			message.put("myUsername", USERNAME);
			message.put("myPassword", PASSWORD);

			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			qparams.add(new BasicNameValuePair("paramRequest", JSON.toJSONString(message)));
			//?vmId="+CardJson.vmId+"&appType="+CardJson.appType
			qparams.add(new BasicNameValuePair("vmId", CardJson.vmId));
			qparams.add(new BasicNameValuePair("appType", CardJson.appType+""));
			
			URI uri = URIUtils.createURI("http", ADDRESS, PORT, DOWNLOAD_URL,
					URLEncodedUtils.format(qparams, HTTP.UTF_8), null);

			HttpGet httpGet = new HttpGet(uri);
			Logger.info(">>>>>>>>> downCertificate:executing request "
					+ httpGet.getURI());

			HttpResponse httpResponse = httpClient.execute(httpGet);

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				httpGet.abort();
				Logger.warn(">>>>>>>>> downCertificate:Server Error." + "StatueCode = " + statusCode);
				return;
			}
			HttpEntity entity = httpResponse.getEntity();
			if (entity == null) {
				Logger.warn(">>>>>>>>> downCertificate:" + "Server response entity == null");
			} else {
				byte[] CerContents = EntityUtils.toByteArray(entity);
				// 存储证书文件到本地
				BufferedOutputStream buffOS = new BufferedOutputStream(
						new FileOutputStream(new File(VERIFCATION_FILE_NAME)));
				buffOS.write(CerContents);
				buffOS.close();

				Logger.info(">>>>>>>>> downCertificate: down certificate success.");
			}

			httpGet.abort();
		} catch (ClientProtocolException e) {
			Logger.error(e.getLocalizedMessage(), e);
		} catch (IOException e) {
			Logger.error(e.getLocalizedMessage(), e);
		} catch (URISyntaxException e) {
			Logger.error(e.getLocalizedMessage(), e);
		} finally {// deallocation all of the system resources
			httpClient.getConnectionManager().shutdown();
		}
	}
}
