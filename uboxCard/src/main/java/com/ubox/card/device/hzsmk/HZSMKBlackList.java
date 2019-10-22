package com.ubox.card.device.hzsmk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

import com.ubox.card.config.CardConst;
import com.ubox.card.util.device.DeviceUtils;
import com.ubox.card.util.logger.Logger;

public class HZSMKBlackList {
	private  static Map<String, String> m1BlackMap = new HashMap<String, String>();
	private  static Map<String, String> cpuBlackMap = new HashMap<String, String>();
	
	private static boolean getServerBlackList() {
		try {
			byte[] b = HZSMKBlackListClient.getBlack();
			if (b == null) {
				Logger.error("从服务器端，获取黑名单列表失败");
				return false;
			}
//			byte[] b2 = HZSMKBlackListClient.unzipStream(b1);
//			if (b2 == null) {
//				Logger.error("从服务器端，解压缩黑名单列表失败");
//				return false;
//			}
			//写入到文件
			HZSMKBlackListClient.stream2File(b);
			
		} catch (Exception e) {
			Logger.error("从服务器端得到黑名单异常。"+e.getMessage(),e);
			return false;
		}
		
		return true;
	}
	
	public static void fromLocalGetM1BlackMap(){
		File filem1 = DeviceUtils.holdFileRef(CardConst.DEVICE_WORK_PATH, "m1blackList.txt");
		
		if (filem1 == null || !filem1.exists()) {
			Logger.error("本地没有找到黑名单文件");
		}else{
			BufferedReader m1bw = null;
			try {
				m1bw = new BufferedReader(new FileReader(filem1));
				String s = null;
				while((s = m1bw.readLine()) != null){
					if (!"".equals(s.trim())) {
						JSONArray array = new JSONArray(s);
						int length = array.length();
						for (int i = 0; i < length; i++) {
							m1BlackMap.put(array.getString(i), array.getString(i));
						}
					}
				}
			} catch (Exception e) {
				Logger.error(e.getLocalizedMessage());
			} finally{
				if(m1bw != null){
					try {
						m1bw.close();
					} catch (Exception e) {
						Logger.error(e.getMessage(), e);
					}
				}
			}
		}
	}
	
	public static void fromLocalGetCPUBlackMap(){
		File filecpu = DeviceUtils.holdFileRef(CardConst.DEVICE_WORK_PATH, "cpublackList.txt");
		
		if (filecpu == null || !filecpu.exists()) {
			Logger.error("本地没有找到黑名单文件");
		}else{
			BufferedReader cpubw = null;
			try {
				cpubw = new BufferedReader(new FileReader(filecpu));
				String s1 = null;
				while((s1 = cpubw.readLine()) != null){
					if (!"".equals(s1.trim())) {
						JSONArray array = new JSONArray(s1);
						int length = array.length();
						for (int i = 0; i < length; i++) {
							cpuBlackMap.put(array.getString(i), array.getString(i));
						}
					}
				}
			} catch (Exception e) {
				Logger.error(e.getLocalizedMessage());
			} finally{
				if(cpubw != null){
					try {
						cpubw.close();
					} catch (Exception e) {
						Logger.error(e.getMessage(), e);
					}
				}
			}
		}
	}
	
	public static void initBlackList() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					//1、从服务器下载
					getServerBlackList();
				} catch (Exception e) {
					Logger.error(e.getMessage(), e);
				}
				
				try {
					//加载本地黑名单到缓存
					fromLocalGetM1BlackMap();
					fromLocalGetCPUBlackMap();
				} catch (Exception e) {
					Logger.error(e.getMessage(), e);
				}
				
			}
		}).start();
		
	}
	
	public static boolean isM1Black(String cardId) {
		if (m1BlackMap == null) {
			return false;
		}
		try {
			String value = m1BlackMap.get(cardId);
			if (value != null) {
				return true;
			}
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
		return false;
	}
	public static boolean isCpuBlack(String cardId) {
		if (cpuBlackMap == null) {
			return false;
		}
		try {
			String value = cpuBlackMap.get(cardId);
			if (value != null) {
				return true;
			}
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
		return false;
	}
}
