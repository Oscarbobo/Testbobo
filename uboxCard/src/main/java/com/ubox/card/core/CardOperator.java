package com.ubox.card.core;

import com.alibaba.fastjson.JSON;
import com.ubox.card.bean.external.ExtRequest;
import com.ubox.card.business.DeviceWorkProxy;
import com.ubox.card.business.DeviceWorkThread;
import com.ubox.card.config.CardConst;
import com.ubox.card.core.context.CardContext;
import com.ubox.card.core.context.CmnMessageBuffer;
import com.ubox.card.util.logger.Logger;

public class CardOperator {
    
    private static DeviceWorkThread workThread ; // 刷卡工作线程

    public static void init() {
        workThread = new DeviceWorkThread(); // 刷卡工作线程
        workThread.start();
        CardContext.getCardInstance().init();
    }
    
    public static void handle(String message) {
        Logger.info("from CVS message: " + message);
        
        if(message == null || "".equals(message)) return ; // 无效信息
        
        ExtRequest request = JSON.parseObject(message, ExtRequest.class);
        String msgType     = request.getMsgType();
        
        if(CardConst.CANCEL_CARD.equals(msgType)) {       // 缓存"撤销请求"
        	if(!"".equals(DeviceWorkProxy.SERNO)){
				Logger.warn("msgType = "+CardConst.CANCEL_CARD+"，serialNo = "+DeviceWorkProxy.SERNO);
				//设置可以取消的标志位
				DeviceWorkProxy.CANCELMAP.put(DeviceWorkProxy.SERNO, message);
				DeviceWorkProxy.SERNO = "";
			}
        } else if(CardConst.CARD_REQ.equals(msgType)) {  // 缓存"卡信息请求"
        	if(CmnMessageBuffer.putMsg(request))
        		Logger.debug("SUCCESS: Cache CARD_REQ");
        	else
        		Logger.debug("FAIL: Cache CARD_REQ");
        } else if(CardConst.COST_REQ.equals(msgType)) {  // 缓存"扣款请求"
        	if(false == DeviceWorkProxy.MAYBECANCEL){//判断刷卡设备是否支持撤销
        		Logger.info("maybe cancel false ");
        		if(true == DeviceWorkProxy.COSTING){//刷卡未处理完，新请求直接返回
        			Logger.warn(">>>>>>> before message is processing <<<<<<<<< ");
        			return;
        		}
        	}else{
        		DeviceWorkProxy.SERNO = request.getSerialNo()+"";
        		Logger.debug("maybe cancel true : "+DeviceWorkProxy.SERNO);
        	}
        	
        	if(CmnMessageBuffer.putMsg(request)) 
        		Logger.debug("SUCCESS: Cache COST_REQ. message:"+message);
        	else
        		Logger.debug("FAIL: Cache COST_REQ. message:"+message);
        } else {                                         // 未知处理信息 
            Logger.warn(">>>>ERROR: UNKNOWN MSG. message:"+message);
        }
    }
    
}
