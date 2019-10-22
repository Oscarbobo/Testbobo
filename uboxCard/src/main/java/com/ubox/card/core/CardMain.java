package com.ubox.card.core;

import com.ubox.card.config.CardJson;
import com.ubox.card.db.DbMain;
import com.ubox.card.util.logger.Logger;
import com.ubox.card.vs.VsMain;

public class CardMain {

	public static void startUp() {
		try {
			Logger.info(">>>> [ VCARD START ] <<<< ");
			
			if(!envCheck()) return ;     //刷卡环境检测
			
			CardOperator.init();         // 刷卡操作环境初始化
			
			DbMain.getInstance().init(); // 刷卡数据操作环境初始化
			
			VsMain.getInstance().init(); // 与服务器通信环境初始化
            
			Logger.info(">>>> [ VCARD END ] <<<< ");
		} catch(Exception e) {
			Logger.error(">>>> UboxCard startup fail.", e);
		}
	}
	
	private static boolean envCheck() {
        String  vmId = CardJson.vmId;
        
        if("".equals(vmId) || Integer.parseInt(vmId) == 0 || CardJson.isCardDevice != 1) {
        	Logger.warn(">>>>WARN: Check environment fail.");
        	return false;
        }
        
        return true;
	}
}

