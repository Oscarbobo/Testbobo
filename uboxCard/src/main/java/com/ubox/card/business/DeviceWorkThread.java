package com.ubox.card.business;

import com.alibaba.fastjson.JSON;
import com.ubox.card.CardService;
import com.ubox.card.bean.external.CostRep;
import com.ubox.card.bean.external.ExtRequest;
import com.ubox.card.bean.external.ExtResponse;
import com.ubox.card.config.CardConst;
import com.ubox.card.core.context.CmnMessageBuffer;
import com.ubox.card.util.logger.Logger;

/**
 * 刷卡工作线程
 */
public class DeviceWorkThread extends Thread {
    
    private final DeviceWorkProxy workProxy = new DeviceWorkProxy();
    
    @Override
    public void run() {
        while(true) {
            try {
                ExtRequest msg = CmnMessageBuffer.takeMsg();
                
                if(msg == null) {
                	continue; //  获取工作信息失败
                }
                
                Logger.debug("SUCCESS: take msg: " + JSON.toJSONString(msg));
                
                if(CardConst.CANCEL_CARD.equals(msg.getMsgType())) {
                	continue;
                }
                
                Logger.debug("Proccess msgType=" + msg.getMsgType());
                
                deviceWork(msg.getMsgType(), JSON.toJSONString(msg));
            } catch(Exception e) {
                Logger.error("Work thread error"+e.getMessage(), e);
            }
        }
    }
    
    /**
     * 设备刷卡工作 
     * 
     * @param msgType 消息类型
     * @param msgJson 消息体
     */
    private void deviceWork(String msgType, String msgJson) {
    	String retMsg = msgJson;
    	Logger.debug("CVS request: " + retMsg);
    	try {
    		
    		if(CardConst.CARD_REQ.equals(msgType)){ 
//    			retMsg = workProxy.cardInfo(msgJson);
    			ExtResponse ext = workProxy.cardInfo(msgJson);
    			retMsg = JSON.toJSONString(ext);
    			Logger.info("to CVS message: " + retMsg);
    			CardService.sendMsg(retMsg);
    		}else if(CardConst.COST_REQ.equals(msgType)){
//    			retMsg = workProxy.cost(msgJson);
    			ExtResponse ext = workProxy.cost(msgJson);
    			retMsg = JSON.toJSONString(ext);
    			Logger.info("to CVS message: " + retMsg);
    			Logger.info("ResultCode(): " + ext.getResultCode());
    			if (ext.getResultCode() != 415) {
    				CardService.sendMsg(retMsg);
    			}
    		}else {
    			Logger.warn("ERROR msgType=" + msgType);
    			
    			ExtResponse ext = Converter.cJSON2ExtRep(msgJson);
    			CostRep     rep = Converter.costReq2costRep(Converter.cJSON2CostReq(msgJson));
    			
    			ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
    			ext.setResultMsg("扣款失败");
    			
    			ext = Converter.genCostRepJSON(rep, ext);
    			retMsg = JSON.toJSONString(ext);
//    			retMsg = Converter.genCostRepJSON(rep, ext);
    			Logger.info("to CVS message: " + retMsg);
    			Logger.info("ResultCode(): " + ext.getResultCode());
    			if (ext.getResultCode() != 415) {
    				CardService.sendMsg(retMsg);
    			}
    		}
    	} catch(Exception e) {
    		Logger.error("worProxy error."+e.getMessage(), e);
    		retMsg = workError(msgJson);
    	}
    }

    /**
     * 处理错误反馈
     * @param msgJson CVS请求
     * @return 错误反馈数据
     */
    private String workError(String msgJson) {
    	ExtResponse ext = Converter.cJSON2ExtRep(msgJson);
    	CostRep     rep = Converter.costReq2costRep(Converter.cJSON2CostReq(msgJson));

    	ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
    	ext.setResultMsg("扣款失败");

    	ExtResponse extr = Converter.genCostRepJSON(rep, ext);
		String retMsg = JSON.toJSONString(extr);
//    	return Converter.genCostRepJSON(rep, ext);
		return retMsg;
    }
}
