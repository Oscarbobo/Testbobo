package com.ubox.card.device.wht.blacklist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import com.ubox.card.config.CardConst;
import com.ubox.card.util.device.DeviceUtils;
import com.ubox.card.util.logger.Logger;

public class BlackList{
	
	private  static Map<String, String> blackMap = new HashMap<String, String>();
	
	/**
	 * 从服务器端下载黑名单，并且写入到文件
	 * @return
	 */
	private static boolean getServerBlackList() {
		try {
			byte[] b1 = BlackListClient.getBlack();
			if (b1 == null) {
				Logger.error("从服务器端，获取黑名单列表失败");
				return false;
			}
			byte[] b2 = BlackListClient.unzipStream(b1);
			if (b2 == null) {
				Logger.error("从服务器端，解压缩黑名单列表失败");
				return false;
			}
			//写入到文件
			BlackListClient.stream2File(b2);
			
		} catch (Exception e) {
			Logger.error("从服务器端得到黑名单异常。"+e.getMessage(),e);
			return false;
		}
		
		return true;
	}
	
	public static void fromLocalGetBlackMap(){
		File file = DeviceUtils.holdFileRef(CardConst.DEVICE_WORK_PATH, "blackList.txt");
		
		if (file == null || !file.exists()) {
			Logger.error("本地没有找到黑名单文件");
		}else{
			BufferedReader bw = null;
			try {
				bw = new BufferedReader(new FileReader(file));
				String s = null;
				while((s = bw.readLine()) != null){
					if (!"".equals(s.trim())) {
						String[] temp = s.split("\t");
						long long1 = Long.valueOf(temp[0]);
						int length = Integer.parseInt(temp[1].trim());
						for (int i = 0; i < length; i++) {
							blackMap.put(String.valueOf(long1), temp.length == 2 ? temp[1] : "");
							long1++;
						}
					}
				}
			} catch (Exception e) {
				Logger.error(e.getLocalizedMessage());
			} finally{
				if(bw != null){
					try {
						bw.close();
					} catch (Exception e) {
						Logger.error(e.getMessage(), e);
					}
				}
			}
		}
	}
	
	/**
	 * 更新黑名单文件（从新下载、缓存）
	 */
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
					fromLocalGetBlackMap();
				} catch (Exception e) {
					Logger.error(e.getMessage(), e);
				}
				
			}
		}).start();
		
	}
	
	/**
	 * 是否是黑名单用户
	 * @param cardId
	 * @return true 是, false 否
	 */
	public static boolean isBlack(String cardId) {
		if (blackMap == null) {
			return false;
		}
		try {
			String value = blackMap.get(cardId);
			if (value != null) {
				return true;
			}
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
		return false;
	}

	public static void main(String[] args) {
		System.out.println(isBlack("8027110110000311"));
	}
	
}
