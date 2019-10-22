/*
 * Copyright (c) 2011 友宝中国. 
 * All Rights Reserved. 保留所有权利.
 */
package com.ubox.card.vs.handler;

import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

import com.alibaba.fastjson.JSON;
import com.ubox.card.CardService;
import com.ubox.card.bean.db.VCardInfoObj;
import com.ubox.card.config.CardJson;
import com.ubox.card.util.TimeUtil;
import com.ubox.card.vs.VsConst;
import com.ubox.util.log.Logger;

/**
 * 刷卡软件信息
 * 
 * @author gaolei
 * @version 2015年6月4日
 *
 */
public class VCardInfoHandler implements Handler {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String, Object> readLoacalData(Map<String, Object> requestData) {
		Object contentsData = requestData.get(VsConst.MAP_KEY_CONTENTS);

		if (!(contentsData instanceof Map<?, ?>)) {
			contentsData = new TreeMap<String, Object>();
		}
		
		//vcardInfo
		VCardInfoObj info = new VCardInfoObj();
		info.setAppType(CardJson.appType+"");
		info.setClientTime(TimeUtil.getCurrentDate());
		Context context = CardService.service.getBaseContext();
		String clientVersion = "";
		try {
			clientVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Logger.error("versionName not found."+e.getMessage(), "");
		}
		info.setSoftVersion(clientVersion);
		info.setInnerCode(CardJson.vmId);
		info.setOs("Android");
		
		((Map) contentsData).put(VsConst.MAP_KEY_SOFT_INFO, JSON.toJSONString(info));

		requestData.put(VsConst.MAP_KEY_CONTENTS, contentsData);

		return requestData;
	}

	@Override
	public void deleteSynSuccessData(Map<String, Object> responseData) {
	}
	
}
