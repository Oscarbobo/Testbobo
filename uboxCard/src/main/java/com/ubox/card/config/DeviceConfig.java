package com.ubox.card.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;

import com.ubox.card.util.device.DeviceUtils;
import com.ubox.card.util.logger.Logger;

public class DeviceConfig {
	
	private static final DeviceConfig INSTANCE = new DeviceConfig();
	
	public static DeviceConfig getInstance() { 
		return INSTANCE; 
	}
	
	private final String fsp            = File.separator;
	private final String configFileName = "config.ini";
	
	private HashMap<String, String> configKeyValue = new HashMap<String, String>();
	
	private DeviceConfig() {
		try {
			File configFile = DeviceUtils.holdFileRef(CardConst.DEVICE_WORK_PATH, configFileName);
			PropertyResourceBundle p = new PropertyResourceBundle(new FileInputStream(configFile));
			for(String key : p.keySet()) {
				configKeyValue.put(key, p.getString(key));
			}
		} catch (IOException e) {
			Logger.error("Init " + configFileName, e);
		}
	}
	
    /**
     * 配置文件添加 "键-值"对
     * @param kvs 键值对集合
     */
	public synchronized void addKeyValue(Map<String, String> kvs) {
        if(kvs == null) { 
        	throw new IllegalArgumentException("kvs is NULL");
        }
        
        configKeyValue.putAll(kvs); // 新增或则更新相同key数据
        persist();
	}
	
	/**
	 * 更新key-value的值,如果key不存在则添加
	 * @param key key值
	 * @param value value值
	 */
	public synchronized void updateKeyValue(String key, String value) {
		if(key == null) {
        	throw new IllegalArgumentException("key is NULL");
		}
		
		configKeyValue.put(key, value);
        persist();
	}
	
	/**
	 * 移除文件里面的 "键-值"对
	 * @param key 键
	 * @return value值
	 */
	public synchronized String removeKeyValue(String key) {
		if(key == null) {
			throw new IllegalArgumentException("argument is NULL");
		}
		
		String value = configKeyValue.remove(key);
		if(value != null) {
			persist();
		}
		
		return value;
	}
	
	/**
	 * 从设备配置文件里面取值
	 * 
	 * @param key
	 * @return null-失败
	 */
	public String getValue(String key) {
		return configKeyValue.get(key);
	}
	
	/**
	 * 持久化内存数据到文件
	 */
	private void persist() {
		File configFile = DeviceUtils.holdFileRef(CardConst.DEVICE_WORK_PATH, configFileName);
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(configFile));
			for(String key : configKeyValue.keySet()) { 
				bw.write(key + "=" + configKeyValue.get(key)); 
				bw.newLine(); 
			}

			bw.close();
		} catch (Exception e) {
			Logger.error("Persist ERROR: " + (CardConst.DEVICE_WORK_PATH + fsp + configFileName), e);
		}
	}
	
}