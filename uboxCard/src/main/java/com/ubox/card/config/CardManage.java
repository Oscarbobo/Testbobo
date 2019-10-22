package com.ubox.card.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.ubox.card.util.logger.Logger;
import com.ubox.util.UboxConfigTool;

/**
 * manage.conf 
 * 管理vcard系统的配置文件读取类
 */
public class CardManage {
	
	private static final String filePath = UboxConfigTool.getUboxDir().getAbsolutePath() + "/Config";
	private static final String fileName = "cardmanage.conf";
	
	private static final CardManage INSTANCE = new CardManage();
	
	private CardManage() {
		try {
			File p = new File(filePath);
			if(!p.exists()) Logger.info(p.mkdirs() ? ">>>>SUCCESS: Create " + filePath : ">>>>FAIL: Create " + filePath);

			File n = new File(filePath + File.separator + fileName);
			if(!n.exists()) Logger.info(n.createNewFile() ? ">>>>SUCCESS: Create manage.conf" : ">>>>FAIL: Create manage.conf");
			
			prop.load(new FileInputStream(n));
		} catch (IOException e) {
			Logger.error(">>>>ERROR: Init manage.conf", e);
		}
	}
	
	public static CardManage getInstance() { return INSTANCE; }	
	
	private static final Properties prop = new Properties();
	
	public static String getValue(String key) {
		if(key == null) return null;
		
		return prop.getProperty(key);
	}
}