package com.ubox.card.core.context;

import android.annotation.SuppressLint;
import com.ubox.card.device.Device;
import com.ubox.card.util.logger.Logger;
import com.ubox.card.config.CardJson;

@SuppressLint("DefaultLocale")
public class CardContext {
	
    /** 工作设备实例 **/
    private static Device cardInstance;
    
    static {
        String cn = CardJson.cardName;
        String fn = "com.ubox.card.device." + cn + "." + cn.toUpperCase();
        try {
        	Logger.info("<<<<<<<<<<<<<<<<  CardJson.cardName : " +fn);
            cardInstance = (Device)Class.forName(fn).newInstance();
        } catch (InstantiationException e) {
            Logger.error(">>>> card instance init error", e);
        } catch (IllegalAccessException e) {
            Logger.error(">>>> card instance init error", e);
        } catch (ClassNotFoundException e) {
            Logger.error(">>>> card instance init error", e);
        }
    }
    
    public static Device getCardInstance() { return cardInstance; }
    
}
