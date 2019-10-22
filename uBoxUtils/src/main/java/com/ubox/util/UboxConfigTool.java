package com.ubox.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import android.annotation.SuppressLint;
import android.text.TextUtils;

public class UboxConfigTool {

	private static final String UBOX_DIR = "/mnt/external1/Ubox";
	@SuppressLint("SdCardPath")
	public static final String EMMC_UBOX = "/mnt/sdcard/Ubox/";
	public static final String UBOX_TMP_DIR = EMMC_UBOX + "tmp/";
	public static final String UDISK_PATH = "/mnt/usbdisk_1.1.1/";
	public static final String U_UBOX_DIR = "/mnt/usbdisk_1.1.1/Ubox";
	/**
	 * UBOX路径
	 * @return
	 */
	public static File getUboxDir(){
		File dir = new File(EMMC_UBOX);
		if(!dir.exists()){
			dir.mkdirs();
		}
		return dir;
	}
	
	/**
	 * UBOX临时路径
	 * @return
	 */
	public static File getUboxTmpDir(){
		File dir = new File(UBOX_TMP_DIR);
		if(!dir.exists()){
			dir.mkdirs();
		}
		return dir;
	}
	
	/**
	 * 注意，此方法不会创建文件
	 * @param fileName
	 * @return
	 */
	public static File getUboxDir(String fileName){
		return new File(getUboxDir(), fileName);
	}
	
	/**
	 * 获取相应文件相应关键字的数据
	 * @param fileName 文件地址     这是个相对地址 且不应该包含“/”  
	 * @param key 关键字
	 *     正确范例：  getConfigValue("webapp.config", "navi");
	 *     错误范例：  getConfigValue("/mnt/webapp.config", "navi");
	 * @return
	 */
	public static String getConfigValue(String fileName, String key) {
		String value = null;
		
		if(TextUtils.isEmpty(fileName) || TextUtils.isEmpty(key)){
			throw new RuntimeException("getConfigValue(String fileName, String key) parm can not null");
		}
		
		if(fileName.contains("\\") || fileName.contains("/")){
			throw new RuntimeException("fileName can not contain \\ or /");
		}
		
		File file = new File(EMMC_UBOX + "Config/" + fileName);
		if (!file.exists() || !file.isFile()) {
			file = new File(UBOX_DIR + "/Config/" + fileName);
		}
		
		if (file.exists() && file.isFile()) {
			FileInputStream fis;
			try {
				fis = new FileInputStream(file);
				Properties prop = new Properties();
				prop.load(fis);
				if (prop.containsKey(key)) {
					value = prop.getProperty(key);
				}
				fis.close();// 关闭资源
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return value;
	}
	
	/**
	 * /**
	 * 获取相应文件相应关键字的数据
	 * @param fileName 文件地址     这是个相对地址 且不应该包含“/”  
	 *     正确范例：  setConfigValue("webapp.config", "navi", "value");
	 *     错误范例：  setConfigValue("/mnt/webapp.config", "navi", "value");
	 * @param key
	 * @param value
	 */
	public static synchronized void setConfigValue(String fileName, String key, String value) {

		if(TextUtils.isEmpty(fileName) || TextUtils.isEmpty(key) || null == value){
			throw new RuntimeException("setConfigValue(String fileName, String key, value) parm can not null");
		}
		
		if(fileName.contains("\\") || fileName.contains("/")){
			throw new RuntimeException("fileName can not contain \\ or /");
		}
		
		File file = new File(EMMC_UBOX + "Config/" + fileName);
		Properties prop = new Properties();
		try {
			if (file.exists() && file.isFile()) {
				FileInputStream fis;
				fis = new FileInputStream(file);
				prop.load(fis);
				fis.close();// 关闭资源
			} else {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			prop.setProperty(key, value);
			FileOutputStream os;
			os = new FileOutputStream(file);
			prop.store(os, "");
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getConfigValue(String key) {
		return getConfigValue("config.properties", key);
	}

	public static String getLocalConfigValue(String key) {
		return getConfigValue("local.properties", key);
	}

	public static String getWebAppConfigValue(String key) {
		return getConfigValue("webapp.config", key);
	}

	public static String getSetCabinetConfigValue(String key) {
		return getConfigValue("SentCabinet.config", key);
	}
}
