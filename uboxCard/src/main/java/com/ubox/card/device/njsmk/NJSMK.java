package com.ubox.card.device.njsmk;

import com.ubox.card.bean.external.Card;
import com.ubox.card.bean.external.CostRep;
import com.ubox.card.bean.external.ExtResponse;
import com.ubox.card.business.CancelProcesser;
import com.ubox.card.business.Converter;
import com.ubox.card.business.DeviceWorkProxy;
import com.ubox.card.config.CardConst;
import com.ubox.card.device.Device;
import com.ubox.card.util.logger.Logger;

public class NJSMK extends Device{
	
	private NJSMKer worker = new NJSMKer();
	
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
		Logger.info(">>>> cost : "+json);
        ExtResponse ext = Converter.cJSON2ExtRep(json);
        CostRep     rep = Converter.costReq2costRep(Converter.cJSON2CostReq(json));
        String resultJson = "";
        try {
			
        	int           consume = rep.getProduct().getSalePrice();
        	NJSMKer.RESULT ret     = worker.cost(consume,ext.getSerialNo()+"");
        	
        	if(NJSMKer.RESULT.CANCEL == ret) { // 刷卡取消
        		return CancelProcesser.cancelProcess(json);
        	}
        	
        	if(NJSMKer.RESULT.SUCCESS == ret) { // 扣款成功
        		Card card = rep.getCards()[0];
        		
        		card.setPosId(worker.getTermNO());                                          
        		card.setCardNo(worker.getCardNO());                                        
        		card.setCardBalance(Integer.parseInt(worker.getBalance())); 
        		card.setCardDesc(worker.getCardSer()+"|"+worker.getCode()+"|"+worker.getWebNode()+"|"+worker.getType()+"|"+worker.getTime()+"|"+worker.getCompany());
        		rep.setThirdOrderNo(worker.getSeq());
        		rep.setOrderNo(DeviceWorkProxy.ORDER_NO);
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
        
//        resultJson = Converter.genCostRepJSON(rep, ext);
        Logger.info("<<< cost : "+resultJson);
		return Converter.genCostRepJSON(rep, ext);
	}
}
