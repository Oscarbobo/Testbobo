package com.ubox.card.device.amt;

import com.ubox.card.bean.external.Card;
import com.ubox.card.bean.external.CostRep;
import com.ubox.card.bean.external.ExtResponse;
import com.ubox.card.business.CancelProcesser;
import com.ubox.card.business.Converter;
import com.ubox.card.business.DeviceWorkProxy;
import com.ubox.card.config.CardConst;
import com.ubox.card.device.Device;
import com.ubox.card.util.logger.Logger;

public class AMT extends Device {
	
	private AMTWorker worker = new AMTWorker();

	@Override
	public void init() {
		DeviceWorkProxy.MAYBECANCEL = true;
	}

	@Override
	public ExtResponse cardInfo(String json) {
        ExtResponse ext = Converter.cJSON2ExtRep(json);

        ext.setResultCode(CardConst.SUCCESS_CODE);
        ext.setResultMsg("SUCCESS");

        Card card = new Card();
        card.setCardType(1);
        card.setCardName("default");
        card.setCardBalance(0);
        card.setCardNo("000000");
        card.setCardDesc("default");
        card.setCardStatus(200);

        Card card1 = new Card();
        card1.setCardType(1);
        card1.setCardName("default");
        card1.setCardBalance(0);
        card1.setCardNo("000000");
        card1.setCardDesc("default");
        card1.setCardStatus(200);
        ext.getData().put("cards", new Card[]{card, card1});
//        return Converter.genCardRepJSON(new Card[]{card, card1}, ext);
        return ext;
	}

	@Override
	public ExtResponse cost(String json) {
		ExtResponse ext = new ExtResponse();
		CostRep     rep = new CostRep();
		try {
			ext = Converter.cJSON2ExtRep(json);
	        rep = Converter.costReq2costRep(Converter.cJSON2CostReq(json));
	        
	        int consume = rep.getProduct().getSalePrice();
	        int ret     = worker.cost(consume,ext.getSerialNo()+"");
	        
	        if(AMTWorker.CANCEL == ret) { // 刷卡取消
	        	return CancelProcesser.cancelProcess(json);
	        }
	        
	        if(ret == AMTWorker.SUCCESS) { // 扣款成功
	            Card card = rep.getCards()[0];

	            card.setPosId("");                                          
	            card.setCardNo("");                                        
	            card.setCardBalance(null); 
	            rep.setThirdOrderNo("");
	        	rep.setOrderNo(DeviceWorkProxy.ORDER_NO);
	            rep.getProduct().setSalePrice(consume);        
	            
	            ext.setResultCode(CardConst.EXT_SUCCESS);
	            ext.setResultMsg("扣款成功");
	        } else {
	            ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
	            ext.setResultMsg("扣款失败");
	        }
	        
			return Converter.genCostRepJSON(rep, ext);
		} catch (Exception e) {
			Logger.error("Exception. "+e.getMessage(), e);
			ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
            ext.setResultMsg("扣款失败");
            return Converter.genCostRepJSON(rep, ext);
		}
        
	}

}
