package com.ubox.card.device.bbjtyh;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.ubox.card.bean.external.Card;
import com.ubox.card.bean.external.CostRep;
import com.ubox.card.bean.external.ExtResponse;
import com.ubox.card.business.CancelProcesser;
import com.ubox.card.business.Converter;
import com.ubox.card.business.DeviceWorkProxy;
import com.ubox.card.config.CardConst;
import com.ubox.card.device.Device;
import com.ubox.card.device.bbjtyh.BBJTYHER.RESULT;
import com.ubox.card.util.logger.Logger;

public class BBJTYH extends Device{
	
	private BBJTYHER worker = new BBJTYHER();
	
	@Override
	public void init() {
		DeviceWorkProxy.MAYBECANCEL = false;
	}

	@Override
	public ExtResponse cardInfo(String json) {
		// do nothing
		return null;
	}

	@Override
	public ExtResponse cost(String json) {
		Logger.info(">>>>> cost:"+json);
        ExtResponse ext = Converter.cJSON2ExtRep(json);
        CostRep     rep = Converter.costReq2costRep(Converter.cJSON2CostReq(json));
        
        String resultJson = "";
        //打开串口
        try {
			worker.open();
		} catch (Exception e) {
			Logger.error("open is exception. "+e.getMessage(), e);
			ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
            ext.setResultMsg("打开串口异常");
//            resultJson = Converter.genCostRepJSON(rep, ext);
            Logger.info("<<<<< cost:"+resultJson);
            return Converter.genCostRepJSON(rep, ext);
		}
        
        /*
         * 指令交互
         */
        try {
        	//读卡
			int timeout = 20;//寻卡超时时间（秒）
			long curTime = System.currentTimeMillis();
			while(true){
				//处理中断功能
				if (com.ubox.card.util.Utils.isCancel(ext.getSerialNo()+"") == true) {
					return CancelProcesser.cancelProcess(json);
				}
				
				//处理超时
				if (System.currentTimeMillis() - curTime >= (1000*timeout)) {
					ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
					ext.setResultMsg("读卡超时");
					return Converter.genCostRepJSON(rep, ext);
				}
				
				//休息会
				Thread.sleep(100);
				
				//调用底层寻卡
				RESULT status  = worker.readCard();
				if(status == RESULT.SUCCESS){
					Logger.info("find card successful.");
					break;
				}else{
					if(status != RESULT.TIMEOUT){
						Logger.error("find card exception. code:"+status);
						ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
						ext.setResultMsg("读卡失败");					
						return Converter.genCostRepJSON(rep, ext);
					}
				}
			}
			
			//扣款
			String tmp = "0000000000000000000000000000000000000000";
			String vcseq = rep.getOrderNo(); 
			if(StringUtils.isNumeric(vcseq)){
				tmp = tmp+vcseq;
				tmp = tmp.substring(tmp.length()-20, tmp.length());
			}
			//休息会
			Thread.sleep(100);
			
			Map<String,Object> costMap =worker.cost(rep.getProduct().getSalePrice(),tmp);
			if(costMap.get("result").equals("00")){
				Logger.info("offline cost successful. "+costMap.toString());
				
	            Card card = rep.getCards()[0];

	            card.setPosId(String.valueOf(costMap.get("cardDev")));                                          
	            card.setCardNo(String.valueOf(costMap.get("cardNO")));                                        
	            card.setCardBalance((Integer)costMap.get("cardBalance"));
	            card.setCardDesc(card.getCardBalance()+"|"+String.valueOf(costMap.get("costDate"))+"|"+String.valueOf(costMap.get("costTime")));
	            rep.setThirdOrderNo(String.valueOf(costMap.get("costSeq")));
	            
	            ext.setResultCode(CardConst.EXT_SUCCESS);
	            ext.setResultMsg("扣款成功");

			}else{
				Logger.warn("cost fail. "+costMap.toString());
				ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
				int resultCode = Integer.parseInt(costMap.get("result").toString());
				if(resultCode == 2){
					ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
					ext.setResultMsg("余额不足");	
					
					Card card = rep.getCards()[0];
		            card.setCardBalance((Integer)costMap.get("cardBalance")); 
				}else if(resultCode == 3){
					ext.setResultMsg("黑名单卡");
				}else if(resultCode == 4){
					ext.setResultMsg("无效卡");
				}else if(resultCode == 5){
					ext.setResultMsg("扣款失败");
				}else if(resultCode == 6){
					ext.setResultMsg("扣款失败，其他错误");
				}else {
					ext.setResultMsg("扣款失败");
				}
			}
		} catch (Exception e) {
			Logger.error("cost is exception. "+e.getMessage(), e);
			ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
            ext.setResultMsg("扣款失败");
//            resultJson = Converter.genCostRepJSON(rep, ext);
            Logger.info("<<<<< cost:"+resultJson);
            return Converter.genCostRepJSON(rep, ext);
		} finally{
			//关闭串口
			worker.close();
		}
        
//        resultJson = Converter.genCostRepJSON(rep, ext);
        Logger.info("<<<<< cost:"+resultJson);
        return Converter.genCostRepJSON(rep, ext);
        
	}
}
