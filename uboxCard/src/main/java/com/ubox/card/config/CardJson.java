package com.ubox.card.config;

import com.alibaba.fastjson.JSON;
import com.ubox.card.util.logger.Logger;
import com.ubox.util.UboxConfigTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;

/**
 * Config.json 配置文件读取类
 */
public class CardJson {
	
	private static final String CONFIG_FILE_NAME = UboxConfigTool.getUboxDir().getAbsolutePath() + "/Config/card.json";
	
	public static final String  vmId         = Builder.vmId;
	public static final String  cardName     = Builder.cardName;
	public static final String  userName     = Builder.userName;
	public static final String  password     = Builder.password;
	public static final String  address      = Builder.address;
	public static final Integer httpPort     = Builder.httpPort;
	public static final Integer httpsPort    = Builder.httpsPort;
	public static final Integer isBeforeCost = Builder.isBeforeCost;
	public static final Integer isCardDevice = Builder.isCardDevice;
	public static final Integer appType      = Builder.appType;
	//public static final String  terminalId    = Builder.terminalId;
	public static final String swipCardOrder =Builder.swipCardOrder;
	public static final String UpShipping    =Builder.UpShipping;
	private static class Builder {
		private static final String  userName     = "test_one";
		private static final String  password     = "test_one";
		private static final String  address      = "vms.uboxol.com";
		private static final Integer httpPort     = 7080;
		private static final Integer httpsPort    = 7443;
		private static final Integer isBeforeCost = 1;
		
		private static String   vmId;
		private static Integer  isCardDevice;
		private static String   cardName;
		private static Integer  appType;
		private static String terminalId;

		private static String swipCardOrder;
		private static String UpShipping;
		
		static{
			try {
				File file                      = new File(CONFIG_FILE_NAME);
				BufferedReader bufferedReader  = new BufferedReader(new FileReader(file));
				StringBuilder stringBuilder    = new StringBuilder();

				String line; 
				while((line = bufferedReader.readLine()) != null) { stringBuilder.append(line); }

				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>)JSON.parse(stringBuilder.toString());

				vmId           =  UboxConfigTool.getLocalConfigValue("vmId");
				isCardDevice   = (Integer)map.get("isCardDevice");
				appType        = (Integer)map.get("appType");
				cardName       = (String)map.get("cardName");
				//terminalId     = (String) map.get("terminalId");
				swipCardOrder  =(String)map.get("swipCardOrder");
				UpShipping     =(String)map.get("UpShipping");
				Logger.info(
						String.format(
								"\n>>>> ======== card.json ========\n" +
										">>>> vmId         = %s;\n" +
										">>>> userName     = %s;\n" + 
										">>>> password     = %s;\n" + 
										">>>> address      = %s;\n" + 
										">>>> httpPort     = %s;\n" + 
										">>>> httpsPort    = %s;\n" + 
										">>>> isCardDevice = %s;\n" +
										">>>> isBeforeCost = %s;\n" + 
										">>>> appType      = %s;\n" + 
										">>>> cardName     = %s",
										vmId, userName, password, address, httpPort, httpsPort, 
										isCardDevice, isBeforeCost, appType,cardName
								)
						);

				if(bufferedReader != null) bufferedReader.close();
			} catch (Exception e) {
				Logger.error("Parse card.json error", e);
			}
		}
	}
}
