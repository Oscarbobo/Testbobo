package com.ubox.card.device.mzt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

import com.ubox.card.config.CardConst;
import com.ubox.card.config.CardJson;
import com.ubox.card.device.mzt.quartz.QuartzBuilder;
import com.ubox.card.device.mzt.quartz.QuartzTask;
import com.ubox.card.util.device.DeviceUtils;
import com.ubox.card.util.logger.Logger;

public class BlacklistManager {
	
	private static final HashMap<String, String> blacklistCache = new HashMap<String, String>(512);
	
	private static final String blacklistFileName = "blacklist.txt";

	/**
	 * 判断卡号是否在黑名单里面
	 * @param cardNo 卡号
	 * @return true-黑名单卡,false-正常卡
	 */
	public static boolean isInBlacklist(String cardNo) {
		synchronized(blacklistCache) {
			return blacklistCache.containsKey(cardNo);
		}
	}
	
	/**
	 * 缓存本地黑名单
	 */
	public static void cacheLocalBlacklist() {
		readBlacklistFile();
	}
	
	/**
	 * 启动黑名单同步任务
	 */
	public static void startSynTask() {
		QuartzTask task1 = QuartzBuilder.builde(1, new DownloadTask());
		QuartzTask task2 = QuartzBuilder.builde(2, new DownloadTask());
		
		task1.start();
		task2.start();
	}
	
	/**
	 * 读取本地黑名单到缓存
	 */
	private static void readBlacklistFile() {
		File bfn = DeviceUtils.holdFileRef(CardConst.DEVICE_WORK_PATH, blacklistFileName);
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(bfn), "utf-8"));
			String line;
			while((line = br.readLine()) != null) {
				blacklistCache.put(line, "");
			}
			br.close();
		} catch (Exception e) {
			Logger.error("Read fail: " + blacklistFileName, e);
		}
	}
	
	/**
	 * 缓存写到本地文件
	 */
	private static void writeBlacklistFile() {
		File bfn = DeviceUtils.holdFileRef(CardConst.DEVICE_WORK_PATH, blacklistFileName);
		
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(bfn), "utf-8"));
			for(String str : blacklistCache.keySet()) {
				bw.append(str);
				bw.newLine();
			}
			bw.close();
		} catch (Exception e) {
			Logger.error("Write fail: " + blacklistFileName, e);
		}
	}
	
	/**
	 * 更新黑名单
	 * @param updata 更新的黑名单
	 */
	private static void updateBlackList(String[] updata) {
		synchronized(blacklistCache) {
			blacklistCache.clear();
			
			for(String str : updata) {
				blacklistCache.put(str, "");
			}
		}
		writeBlacklistFile();
	}
	
	
	/**
	 * 黑名单下载任务
	 */
	public static final class DownloadTask implements Runnable {
		
		private final int timeout = 3000;
		
		// test   : 192.168.11.79
		// formal : mzt.vm-pay.uboxol.com
		private final String HOST = "mzt.vm-pay.uboxol.com";
		private final String PATH = "/vcardServer/myyktblackList?vmId="+CardJson.vmId+"&appType="+CardJson.appType;
		private final int    PORT = 7080;
		
		@Override
		public void run() {
			try {
				DefaultHttpClient httpClient = new DefaultHttpClient();
				httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
				httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,timeout);
				
				URI      uri      = URIUtils.createURI("http", HOST, PORT, PATH, null, null);
				HttpPost httpPost = new HttpPost(uri);
				httpPost.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
				
				Logger.debug("Download URI = " + httpPost.getURI()); // DEBUG 日志
				
				HttpResponse httpResponse = httpClient.execute(httpPost);
				int          status       = httpResponse.getStatusLine().getStatusCode();
				if(status != HttpStatus.SC_OK) {
					Logger.warn("Download fail, status code " + status);
					return;
				}
				
				HttpEntity entity = httpResponse.getEntity();
				if(entity == null) {
					Logger.warn("Download fail, http entity is NULL.");
					return;
				}
				
				byte[]   downloadData = EntityUtils.toByteArray(entity);	
				byte[]   unZipData    = MZTUtils.uncompressZIP(downloadData);
				String[] blacklist    = new String(unZipData).split("\r\n");
				
				updateBlackList(blacklist); // 更新黑名单
				
				httpPost.abort();
				httpClient.getConnectionManager().shutdown();
			} catch(Exception e) {
				Logger.error("Download ERROR", e);
			}
		}
	}
}
