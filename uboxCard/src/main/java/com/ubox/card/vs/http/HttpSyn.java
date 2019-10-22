/*
 * Copyright (c) 2011 友宝中国. 
 * All Rights Reserved. 保留所有权利.
 */
package com.ubox.card.vs.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.ubox.card.config.CardJson;
import com.ubox.card.util.GZIPUtil;
import com.ubox.card.util.logger.Logger;

public class HttpSyn {
	
	private static final int    TIMEOUT 			= 30 * 1000;
	private static final String ADDRESS 			= CardJson.address;
	private static final String SYN_URL 			= "/vcardServer/client/poll?vmId="+CardJson.vmId+"&appType="+CardJson.appType;
	private static final int    PORT    			= CardJson.httpPort;
	private static final String FIELD_NAME  		= "filedata";
	private static final String HEAD_NAME 			= "Content-Type";
	private static final String HEAD_RESPONSE_VALUE = "application/gzip";

	public static String postData(Map<String, Object> reqData)
			throws URISyntaxException, ClientProtocolException, IOException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT); // 等待建立Socket连接的最长时间
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, TIMEOUT); 		   // Socket建立连接后,等待接收数据的最大长度

		URI 	 uri      = URIUtils.createURI("http", ADDRESS, PORT, SYN_URL, null, null);
		HttpPost httpPost = new HttpPost(uri);
		httpPost.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
		
		Logger.info("VcardServer URI = " + httpPost.getURI());

		try {
			ByteArrayBody byteArrBody = new ByteArrayBody(
					GZIPUtil.compress(JSON.toJSONString(reqData).getBytes("UTF-8")), 
					"poll-" + System.currentTimeMillis() + ".gz");

			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart(FIELD_NAME, byteArrBody);
			
			httpPost.setEntity(reqEntity);
		} catch (Exception e) {
			Logger.error(e.getLocalizedMessage(), e);
		}

		/* 发送数据到服务器,并初步解析response */
		HttpResponse httpResponse = httpClient.execute(httpPost);
		
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		if (statusCode != HttpStatus.SC_OK) {
			httpPost.abort();
			Logger.warn("Server Error,statusCode=" + statusCode);
			return null;
		}

		/* 获取response的HTTP协议的消息头 */
		Header header = httpResponse.getFirstHeader(HEAD_NAME);

		/* 获取response的HTTP协议的消息体 */
		byte[] respBytes;
		HttpEntity httpEntity = httpResponse.getEntity();
		if (httpEntity == null) {
			Logger.warn("Server response entity == null");
			return null;
		} else {
			respBytes = EntityUtils.toByteArray(httpEntity);
		}

		/* 进一步解析response,获取反馈的字符串 */
		InputStream isStream ;
		if (header != null && 
			header.getValue() != null && 
			header.getValue().startsWith(HEAD_RESPONSE_VALUE)) { // gz压缩数据格式
			
			isStream = new GZIPInputStream(new ByteArrayInputStream(respBytes));
		} else {// 纯文本格式
			isStream = new ByteArrayInputStream(respBytes);
		}
		String respStr = IOUtils.toString(isStream, HTTP.UTF_8);

		isStream.close();
		httpPost.abort();
		httpClient.getConnectionManager().shutdown();

		return respStr;
	}

}
