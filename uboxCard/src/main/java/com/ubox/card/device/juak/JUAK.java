package com.ubox.card.device.juak;

import com.ubox.card.bean.external.Card;
import com.ubox.card.bean.external.CostRep;
import com.ubox.card.bean.external.ExtResponse;
import com.ubox.card.business.CancelProcesser;
import com.ubox.card.business.Converter;
import com.ubox.card.business.DeviceWorkProxy;
import com.ubox.card.config.CardConst;
import com.ubox.card.device.Device;
import com.ubox.card.util.Utils;

public class JUAK extends Device {

	@Override
	public void init() {  }

	@Override
	public ExtResponse cardInfo(String json) {
        ExtResponse ext = Converter.cJSON2ExtRep(json);

        ext.setResultCode(CardConst.SUCCESS_CODE);
        ext.setResultMsg("SUCCESS");

        Card card = new Card();
        card.setCardType(1);
        card.setCardName("会员卡");
        card.setCardBalance(6000);
        card.setCardNo("567890");
        card.setCardDesc("devssd");
        card.setCardStatus(200);

        Card card1 = new Card();
        card1.setCardType(1);
        card1.setCardName("会员卡");
        card1.setCardBalance(6000);
        card1.setCardNo("567890");
        card1.setCardDesc("devssd");
        card1.setCardStatus(200);

        ext.getData().put("cards", new Card[]{card, card1});
//        return Converter.genCardRepJSON(new Card[]{card, card1}, ext);
        return ext;
	}

	@Override
	public ExtResponse cost(String json) {
        ExtResponse ext  = Converter.cJSON2ExtRep(json);
        CostRep     rep  = Converter.costReq2costRep(Converter.cJSON2CostReq(json));
 
        if(!check()) { // 自检失败
        	return Converter.genCostRepJSON(rep, ext);
        }
        
        /* * * 取消处理 * * */
        if(Utils.isCancel(ext.getSerialNo()+"")) { 
        	return CancelProcesser.cancelProcess(json); // 撤销交易
        }
        
        /* * * 卡余额判断 * * * */
        int balance = 32552;
        if(balance >= rep.getProduct().getSalePrice()) {
        	ext.setResultCode(CardConst.EXT_BALANCE_LESS);
        	ext.setResultMsg("卡余额不足");
        	return Converter.genCostRepJSON(rep, ext);
        }
            
        /** '取消' 慎用，已经扣款则不能取消.所以，此代码以后不能使用撤销**/
        
        Card card = rep.getCards()[0];

        card.setCardDesc("");               // 商户代码
        card.setPosId("");                  // POS机终端号
        card.setCardNo("");                 // 卡号
        card.setCardBalance(0); 			// 卡余额
        rep.setThirdOrderNo("");    		// 交易流水号
        
        rep.getProduct().setSalePrice(0);   // 实际扣款金额 -- 一般情况可忽略,打折活动的时候需要
        rep.setOrderNo(DeviceWorkProxy.ORDER_NO);   

        ext.setResultCode(CardConst.EXT_SUCCESS);
        ext.setResultMsg("扣款成功");
        
        return Converter.genCostRepJSON(rep, ext);
	}

	/**
	 * 约束条件监测 
	 * @return true-监测成功,false-监测失败
	 */
	private boolean check() {
		return false;
	}
	
}
