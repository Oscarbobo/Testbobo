/*
 * Copyright (c) 2011 友宝中国. 
 * All Rights Reserved. 保留所有权利.
 */
package com.ubox.card.vs.runner;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.alibaba.fastjson.JSON;
import com.ubox.card.config.CardJson;
import com.ubox.card.util.encrypt.DESCoder;
import com.ubox.card.util.encrypt.MD5Coder;
import com.ubox.card.util.logger.Logger;
import com.ubox.card.vs.VsConst;
import com.ubox.card.vs.VsContext;
import com.ubox.card.vs.handler.Handler;
import com.ubox.card.vs.http.HttpSyn;
import com.ubox.card.vs.http.HttpsLogin;

/**
 * @author miral.gu
 * @date 2012-5-11 上午11:29:50
 * 
 */
public abstract class BaseSyn {// Base类不能实例化,所以用abstract
	/** Vcard存储查询的本地db数据 ,发送到服务器VcardServer */
	private Map<String, Object> reqData = new TreeMap<String, Object>();
	
	/** Vcard存储VcardServer同步成功的数据 */
	private Map<String, Object> responseData = new TreeMap<String, Object>();
	
	/** 售货机ID */
	protected static String vmId = CardJson.vmId;
	
	/** 数据是否进行加密 */
	protected static boolean isEncrypt = true;// 默认是加密.如果在config.json中配置就更好

	/** 不能使用static修饰,否则所有的子类都会共享此handlers,会导致严重的问题 */
	protected final List<Handler> handlers = new ArrayList<Handler>();

	/**
	 * 策略模式,不同子类添加不同的平行算法
	 * 
	 * 算法的执行环境是子类的doSyn方法中
	 * */
	public void addHandler(Handler h) {
		handlers.add(h);
	}

	/**
	 * 数据同步
	 * 
	 */
	protected void doSyn() {
		if (null == HttpsLogin.getSslSession()) {// HTTPS登入
			doHttpsLogin();
			return;
		}
		try {// HTTP数据同步
			doHttpSynRequest();
			doHttpSynResponse();
			boolean sessionEffective = VsContext.getInstance().isSessionEffective();
			if (sessionEffective) { // SSLSession有效,才会删除数据
				doLocalDataSyn();
			} else {// SSLSession无效
				HttpsLogin.setSslSession(null);// SSLSessoin设置成无效
			}
		} catch (Exception e) {
			Logger.error(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * 客户端HTTPS登入，获取登入服务器权限
	 * 
	 * 具体流程，见《Vcard与VcardServer通信流程图》
	 */
	private void doHttpsLogin() { HttpsLogin.connect(); }

	/**
	 * 查询数据，组装HTTP
	 * 
	 * @throws Exception
	 */
	private void doHttpSynRequest() throws Exception {
		reqData.put(VsConst.MAP_KEY_CONTENTS, new TreeMap<String, Object>());
		
		// 策略模式,不同的handler处理，查询数据
		for (Handler h : handlers) { 
			h.readLoacalData(reqData); 
		}

		/* 添加协议头信息 */
		reqData.put(VsConst.MAP_KEY_SSLSESSION, MD5Coder.getMD5Str(HttpsLogin.getSslSession()));
		reqData.put(VsConst.MAP_KEY_INNER_CODE, vmId);
		reqData.put(VsConst.MAP_KEY_IS_ENCRYPT, isEncrypt);
		reqData.put(VsConst.MAP_KEY_SESSION_EFFECTIVE, VsContext.getInstance().isSessionEffective());
		
	}

	/**
	 * 发送数据到服务器,解析SSLSession并缓存
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void doHttpSynResponse() throws Exception {
		Logger.info("Before encrypt to VcardServer:" + JSON.toJSONString(reqData));
		// 是否进行加密处理
		if (isEncrypt) { 
			encrypt(reqData); 
		}

		// 发送数据到服务端,获取服务端反馈的数据
		String respStr = HttpSyn.postData(reqData);

		// 解析respStr,缓存sessionEffective
		if (respStr == null) {
			Logger.warn("From VcardServer response is null");
			return;
		}

		responseData = (Map) JSON.parse(respStr);

		// 是否进行加密处理
		if (isEncrypt) { 
			decrypt(responseData); 
		}
		Logger.info("After decrypt from VcardServer:" + JSON.toJSONString(responseData));

		boolean sessionEffective = (Boolean) responseData.get(VsConst.MAP_KEY_SESSION_EFFECTIVE);
		VsContext.getInstance().setSessionEffective(sessionEffective);
	}

	/**
	 * Vcard删除同步成功的数据
	 */
	private void doLocalDataSyn() {
		// 策略模式,不同的handler处理，删除数据
		for (Handler h : handlers) {
			
			h.deleteSynSuccessData(responseData); 
		}
	}

	/**
	 * 解密反馈数据
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void decrypt(Map<String, Object> responseData)
			throws UnsupportedEncodingException, Exception {
		String contents = responseData.get(VsConst.MAP_KEY_CONTENTS).toString();
		if(contents.equals("")){//contents为空的话直接返回
			return;
		}
		byte[] byteContents = contents.getBytes("UTF-8");

		byte[] encryptByteArray = DESCoder.decrypt(
				DESCoder.decryptBASE64(new String(byteContents, "utf-8")), 
				HttpsLogin.getKey());

		contents = new String(encryptByteArray, "UTF-8");

		responseData.put(VsConst.MAP_KEY_CONTENTS, (Map<String, Object>) JSON.parse(contents));
	}

	/**
	 * 加密发送数据
	 * 
	 * @param str
	 * @return
	 * @throws Exception
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	private void encrypt(Map<String, Object> reqData)
			throws UnsupportedEncodingException, Exception {
		Map<String, Object> contents = (Map<String, Object>) reqData.get(VsConst.MAP_KEY_CONTENTS);
		if (contents != null) {
			byte[] outputData = DESCoder.encrypt(
					JSON.toJSONString(contents).getBytes("UTF-8"), 
					HttpsLogin.getKey());
			
			String encryptBase64 = DESCoder.encryptBASE64(outputData);
			reqData.put(VsConst.MAP_KEY_CONTENTS, encryptBase64);
		}
	}
}
