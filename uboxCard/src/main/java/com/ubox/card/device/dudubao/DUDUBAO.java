package com.ubox.card.device.dudubao;

import cn.ubox.vm.vcard.api.CardAPI;
import cn.ubox.vm.vcard.api.model.Pay;
import cn.ubox.vm.vcard.api.model.Query;

import com.alibaba.fastjson.JSON;
import com.dodopay.coustom.BindService;
import com.ubox.card.CardService;
import com.ubox.card.bean.external.Card;
import com.ubox.card.bean.external.CostRep;
import com.ubox.card.bean.external.ExtResponse;
import com.ubox.card.business.CancelProcesser;
import com.ubox.card.business.Converter;
import com.ubox.card.config.CardConst;
import com.ubox.card.config.CardJson;
import com.ubox.card.device.Device;
import com.ubox.card.util.Utils;
import com.ubox.card.util.logger.Logger;

public class DUDUBAO extends Device{

	private CardAPI cardAPI = null;
	
	@Override
	public void init() {

	}

	public CardAPI getCardAPI() {
		if(cardAPI == null){
			cardAPI = new BindService(CardService.service.getBaseContext());//toFix:初始化嘟嘟宝实现类
		}
		return cardAPI;
	}

	@Override
	public ExtResponse cardInfo(String json) {
		return null;
	}

	@Override
	public ExtResponse cost(String json) {
		
		ExtResponse ext = Converter.cJSON2ExtRep(json);
        CostRep     rep = Converter.costReq2costRep(Converter.cJSON2CostReq(json));
        
        try {
			CardAPI cardAPI = getCardAPI();
        	Thread.sleep(1000*1);
        	//读卡
            Query query = null;
            long startTime = System.currentTimeMillis();
            int timeOut = 20;
            while(true){
            	long endTime = System.currentTimeMillis();
            	if(endTime - startTime >= timeOut*1000){
            		Logger.warn("find card timeout. "+json);
            		ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
                    ext.setResultMsg("读卡超时");
            		return Converter.genCostRepJSON(rep, ext);
            	}
            	
            	if(true == Utils.isCancel(ext.getSerialNo()+"")){
            		return CancelProcesser.cancelProcess(json);
            	}
            	
            	Logger.info(">>>> query:");
            	query = cardAPI.query();
            	Logger.info("<<<< query:"+JSON.toJSONString(query));
            	if(query.getResultCode() == 0){
            		break;
            	}else{
    	        	if(query.getResultCode()!=1){
    	        		Logger.error("读卡失败");
    	        		ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
    	                ext.setResultMsg(CardConst.EXT_READ_CARD_FAIL_MSG);
    	        		return Converter.genCostRepJSON(rep, ext);
    	        	}
            	}
            }
            
            //扣款
            int consume = rep.getProduct().getSalePrice();
            String orderNo = CardJson.vmId+"-"+rep.getOrderNo();
            Logger.info(">>>>> pay:"+orderNo+","+consume);
            Pay pay = cardAPI.pay(orderNo, consume);
            Logger.info("<<<<< pay:"+JSON.toJSONString(pay));
            if(pay.getResultCode()==0){
            	Card card = rep.getCards()[0];

                card.setPosId(pay.getPosId());                               
                card.setCardNo(pay.getCardNo());                                        
                card.setCardBalance(pay.getCardBalacne()); 
                card.setCardDesc(pay.getData()+":"+pay.getResultMsg());
                
                rep.setThirdOrderNo(pay.getSerNo());
                rep.getProduct().setSalePrice(consume);        
                
                ext.setResultCode(CardConst.EXT_SUCCESS);
                ext.setResultMsg("扣款成功");
            } else {
                ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
                ext.setResultMsg("扣款失败");
            }
		} catch (Exception e) {
			Logger.error("cost is exception. "+e.getMessage(), e);
			ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
			ext.setResultMsg("扣款失败");
		}
        
		return Converter.genCostRepJSON(rep, ext);
		
	}

}
