package com.ubox.card.device.zjylsf;

import com.ubox.card.bean.db.UnionpayTradeObj;
import com.ubox.card.bean.external.Card;
import com.ubox.card.bean.external.CostRep;
import com.ubox.card.bean.external.ExtResponse;
import com.ubox.card.business.CancelProcesser;
import com.ubox.card.business.Converter;
import com.ubox.card.business.DeviceWorkProxy;
import com.ubox.card.config.CardConst;
import com.ubox.card.db.dao.UnionpayTradeDao;
import com.ubox.card.device.Device;
import com.ubox.card.device.zjylsf.compos.BusyPass;
import com.ubox.card.util.Utils;
import com.ubox.card.util.logger.Logger;

public class ZJYLSF extends Device {
	
	@Override
	public void init() { 
	}

	@Override
	public ExtResponse cardInfo(String json) {
        ExtResponse ext  = Converter.cJSON2ExtRep(json);
        Card        card = new Card();
        card.setCardType(1);
	
		try {
			//[0]-解析状态,[1]-商户代码,[2]-终端号,[3]-卡号,[4]-卡余额,[5]-交易流水号,[6]-交易批次号
			String[] query = BusyPass.query();
			if(!BusyPass.SUCCESS.equals(query[0])) {
				ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
				ext.setResultMsg("卡信息查询失败");
				ext.getData().put("cards", new Card[]{card});
//		        return Converter.genCardRepJSON(new Card[]{card, card1}, ext);
		        return ext;
			}
			
            int balance  = Integer.parseInt(query[4].replace(".", ""));				
			card.setCardBalance(balance);
			card.setCardDesc(query[1]);
			card.setCardName(query[1]);
			card.setCardNo(query[3]);
			card.setCardStatus(200);
			card.setPosId(query[2]);
			
			ext.setResultCode(CardConst.EXT_SUCCESS);
			ext.setResultMsg("卡信息查询成功");
		} catch(Exception e) {
			Logger.error(">>>>ERROR: Query balance", e);
			ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
			ext.setResultMsg("卡信息查询失败");
		}
		
		ext.getData().put("cards", new Card[]{card});
//      return Converter.genCardRepJSON(new Card[]{card, card1}, ext);
		return ext;
	}

	@Override
	public ExtResponse cost(String json) {
        ExtResponse ext = Converter.cJSON2ExtRep(json);
        CostRep     rep = Converter.costReq2costRep(Converter.cJSON2CostReq(json));
		
		try {
			//[0]-解析状态,[1]-商户代码,[2]-终端号,[3]-卡号,[4]-卡余额,[5]-交易流水号,[6]-交易授权号
			String[] query = BusyPass.query();
			if(!BusyPass.SUCCESS.equals(query[0])) {
				ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
				ext.setResultMsg("卡信息查询失败");
				return Converter.genCostRepJSON(rep, ext);
			}
			
            int    balance   = Integer.parseInt(query[4].replace(".", ""));				
            int    price     = rep.getProduct().getSalePrice();
//            String userNO    = query[3];
            
            /**
             * 浙江6分活动
             */
//            Six six = null;
//            RuleResponse response = null;
//            six = new Six();
//            boolean recordFlag = false;
//            int todayCount = 0;
//            response = six.rule(ext.getVmId(), rep.getProduct().getProId()+"", query[3], price);
//            
//            if(true == response.isDiscount()){
//            	recordFlag = response.isRecord();
//            	todayCount = response.getCount();
//            	
//            	price = response.getRuleMoney();
//            }
            
            if(balance < price) {
				ext.setResultCode(CardConst.EXT_BALANCE_LESS); // 余额不足
                ext.setResultMsg("余额不足");
                return Converter.genCostRepJSON(rep, ext);
            }
            

            if(Utils.isCancel(ext.getSerialNo()+"") == true ) {
            	return CancelProcesser.cancelProcess(json); // 撤销交易
            }
            

//            if(CmnMessageBuffer.clearCancelMsg() != null) {
//            	return CancelProcesser.cancelProcess(json); // 撤销交易
//            }
//            
            // [0]-解析状态, [1]-商户代码, [2]-终端号, [3]-卡号, [4]-交易金额, [5]-交易流水号, [6]-脱机上送交易记录, [7]-交易日期时间
            String[] consume = BusyPass.consumeOffline(price);
            
            if(!BusyPass.SUCCESS.equals(consume[0])) { // 消费失败
            	ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
            	ext.setResultMsg("扣款失败");
                return Converter.genCostRepJSON(rep, ext);
            }
            
            /**
             * 浙江6分活动
             */
//            if(true == recordFlag){
//            	six.record(query[3]+":"+todayCount);
//            }
            
            Card card = rep.getCards()[0];
            
//            if(true == response.isDiscount()){
//            	card.setCardType(99);//特殊标记活动的卡类型，方便后续数据库的统计
//            }
            
            card.setCardDesc(consume[1]);                                       // 商户代码
            card.setPosId(consume[2]);                                          // POS机终端号
            card.setCardNo(consume[3]);                                         // 卡号
            card.setCardBalance(balance - Integer.parseInt(consume[4]));                               // 卡余额
            rep.setThirdOrderNo(consume[5]);                                    // 交易流水号
            rep.getProduct().setSalePrice(Integer.parseInt(consume[4]));        // 实际扣款金额
        	rep.setOrderNo(DeviceWorkProxy.ORDER_NO);
            
            ext.setResultCode(CardConst.EXT_SUCCESS);
            ext.setResultMsg("扣款成功");
            
            // 记录离线消费记录
            String brushSeq  = DeviceWorkProxy.ORDER_NO;
            String vmId      = ext.getVmId();
            String devNO     = consume[2];
            String tradeData = consume[6];
            String cardNO    = consume[3];
            String tradeTime = consume[7];
            String tradeFee  = consume[4];
            String merchant  = consume[1];

            writeConsumeRecord(brushSeq, vmId, devNO, tradeData, cardNO, tradeTime, tradeFee, merchant); 
            
		} catch(Exception e) {
			Logger.error(">>>>ERROR: Query balance", e);
			ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
			ext.setResultMsg("扣款失败");
		}
        
		return Converter.genCostRepJSON(rep, ext);
	}
	
    /**
     * 记录脱机消费记录
     * @param brushSeq vcard流水号
     * @param vmId 售货机ID
     * @param devNO POS终端号
     * @param tradeData 脱机交易数据
     * @param cardNO 交易卡号
     * @param tradeTime 交易日期
     * @param tradeFee 交易金额
     * @param merchant 商户号
     */
    private static void writeConsumeRecord(final String brushSeq,  final String vmId,   final String devNO,
                                           final String tradeData, final String cardNO, final String tradeTime,
                                           final String tradeFee,  final String merchant) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                UnionpayTradeObj.UnionpayTrade unionpayTrade = new UnionpayTradeObj.UnionpayTrade();
                unionpayTrade.setId(brushSeq);
                unionpayTrade.setBrushSeq(brushSeq);
                unionpayTrade.setVmId(vmId);
                unionpayTrade.setDevNO(devNO);
                unionpayTrade.setTradeData(tradeData);
                unionpayTrade.setCardNO(cardNO);
                unionpayTrade.setTradeTime(tradeTime);
                unionpayTrade.setTradeFee(tradeFee);
                unionpayTrade.setMerchant(merchant);

                UnionpayTradeDao.getInstance().insertOne(unionpayTrade);
                Logger.info(">>>>Unionpay: write offline record end");
            }
        }).start();
    }
}
