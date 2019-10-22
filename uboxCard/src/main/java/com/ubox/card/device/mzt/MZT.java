package com.ubox.card.device.mzt;

import com.ubox.card.bean.db.MZTTradeObj.MZTTrade;
import com.ubox.card.bean.external.Card;
import com.ubox.card.bean.external.CostRep;
import com.ubox.card.bean.external.ExtResponse;
import com.ubox.card.business.CancelProcesser;
import com.ubox.card.business.Converter;
import com.ubox.card.business.DeviceWorkProxy;
import com.ubox.card.config.CardConst;
import com.ubox.card.config.CardJson;
import com.ubox.card.config.DeviceConfig;
import com.ubox.card.core.WorkPool;
import com.ubox.card.db.dao.MZTTradeDao;
import com.ubox.card.device.Device;
import com.ubox.card.util.logger.Logger;

public class MZT extends Device {
	private final String KEY_NO 		= "serialNO";
	private final String KEY_DEV_NO 	= "serialDevNO";
	private final int    FIND_TIME_OUT 	= 10000;
	
	private String serialNO;
	private String serialDevNO;

	@Override
	public void init() {
		BlacklistManager.cacheLocalBlacklist(); // 本地黑名单读取到内存
		BlacklistManager.startSynTask(); // 终端黑名单定时与服务器进行同步
	}

	@Override
	public ExtResponse cardInfo(String json) {
		/** * * * * * * * * * * * * * * * * * *
		 * 绵州一卡通没有使用此接口.下面的代码只示例,不使用
		 * * * * * * * * * * * * * * * * * * */
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
//      return Converter.genCardRepJSON(new Card[]{card, card1}, ext);
      return ext;
	}

	@Override
	public ExtResponse cost(String json) {
        ExtResponse ext = Converter.cJSON2ExtRep(json);
        CostRep     rep = Converter.costReq2costRep(Converter.cJSON2CostReq(json));
 
        if(!selfCheck()) { // 刷卡环境自检失败
        	ext.setResultCode(CardConst.EXT_INIT_FAIL);
        	ext.setResultMsg("配置错误");
        	return Converter.genCostRepJSON(rep, ext);
        }
        
        /* * * * * * * * * * * * 寻卡操作  * * * * * * * * * * */
        String[] fc = MZTManager.findCard(FIND_TIME_OUT,ext.getSerialNo()+"");
        if(fc[0].equals(MZTManager.CANCEL)) { // 撤销交易
        	return CancelProcesser.cancelProcess(json); 
       	}
        
        if(!fc[0].equals(MZTManager.SUCCESS)) { // 寻卡失败
        	if(fc[0].equals(MZTManager.TIME_OUT)) {
        		ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
        		ext.setResultMsg("寻卡超时");
        	} else if(fc[0].equals(MZTManager.FALSE)) {
        		ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
        		ext.setResultMsg("寻卡错误");
        	} else {
        		ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
        		ext.setResultMsg("寻卡未知错误");
        	}
        	
        	return Converter.genCostRepJSON(rep, ext);
        }
        
        String physicalNO = fc[2]; // 卡物理号
        
        /* * * * * * * * * * * * 读取卡信息  * * * * * * * * * * */
        String[] rc = MZTManager.readCard();
        if(!rc[0].equals(MZTManager.SUCCESS)) { // 读卡失败
        	ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
        	ext.setResultMsg(CardConst.EXT_READ_CARD_FAIL_MSG);
        	return Converter.genCostRepJSON(rep, ext);
        }
        String cardNO 		= rc[2];  // 卡号
        String cityCodeCard = rc[3];  // 城市代码
        String industryCode = rc[4];  // 行业代码
        String mainCardType = rc[5];  // 卡主类型
        String subCardType  = rc[6];  // 卡子类型
        String cardVersion  = rc[7];  // 卡版本
        String cardBalance  = rc[8];  // 卡余额
        String cardUseState = rc[9];  // 卡启用状态
        String useDate      = rc[10]; // 卡启用日期
        String effectDate   = rc[11]; // 卡有效日期
        
        /* * * * * * * * * * * * 卡余额处理 * * * * * * * * */
        int balance = Integer.parseInt(cardBalance);
        int consume = rep.getProduct().getSalePrice();
        if(balance < consume) { // 卡余额不足
        	ext.setResultCode(CardConst.EXT_BALANCE_LESS);
        	ext.setResultMsg(CardConst.EXT_BALANCE_LESS_MSG);
        	return Converter.genCostRepJSON(rep, ext);
        }
        	
        /* * * * * * * * * * * * 黑名单处理 * * * * * * * * */
        if(BlacklistManager.isInBlacklist(cardNO)) {
        	ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
        	ext.setResultMsg("黑名单卡片");
        	return Converter.genCostRepJSON(rep, ext);
        }
        
        /* * * * * * * * * * * * 脱机消费  * * * * * * * * * */
        String[] oc = MZTManager.offlineConsume(consume, cardNO);
        if(!oc[0].equals(MZTManager.SUCCESS)) { // 扣款失败
        	ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
        	ext.setResultMsg(CardConst.EXT_CONSUEM_FAIL_MSG);
        	return Converter.genCostRepJSON(rep, ext);
        }
        String ccount = oc[2]; // 卡交易计数器
        String pcount = oc[3]; // PSAM卡交易计数器
        String psam   = oc[4]; // PASM卡号
        String tac    = oc[5]; // 交易TAC
        
        /* * * * * * * * * * * 数据同步 与上送 * * * * * * * * * */
        String seq = DeviceWorkProxy.ORDER_NO; 
        updateSrialNO();
        
		MZTTrade trade = new MZTTrade();
		trade.setId(seq);
		trade.setVmId(CardJson.vmId);
		trade.setBrushSeq(seq);
		trade.setCardNo(cardNO);
		trade.setCityCodeCard(cityCodeCard);
		trade.setPasmCardNO(psam);
		trade.setBusinessCode(industryCode);
		trade.setCardCount(ccount);
		trade.setMainCardType(mainCardType);
		trade.setSubCardType(subCardType);
		trade.setBConsumeBalance(Integer.valueOf(cardBalance));
		trade.setTradeFee(consume);
		trade.setCostMoney(consume);
		trade.setTradeDate(MZTUtils.getDate());
		trade.setTradeTime(MZTUtils.getTime());
		trade.setTac(tac);
		trade.setSamCardSerialNO(pcount);
		trade.setCardVersion(cardVersion);
		trade.setDevNO(psam);
		trade.setPosSerialNO(serialNO);
		trade.setPosDevNO(serialDevNO);
		trade.setCsnNO(physicalNO);
		
        asynUpload(trade);
        
        /* * * * * * * * * * * 刷卡结果反馈给cvs * * * * * * */
        Card card = rep.getCards()[0];
        int  cb   = balance - consume;
        
        card.setPosId(psam);     // 机具终端号
        card.setCardNo(cardNO);  // 卡号
        card.setCardBalance(cb); // 刷卡后的余额
        card.setCardDesc(tac + "|" + physicalNO + "|" + cardUseState + "|" + useDate + "|" + effectDate); 
        
        rep.setOrderNo(seq);					 // card交易流水号
        rep.setThirdOrderNo(pcount);    		 // 交易流水号
        rep.getProduct().setSalePrice(consume);  // 实际扣款金额
        
        ext.setResultCode(CardConst.EXT_SUCCESS);
        ext.setResultMsg("扣款成功");
        
        return Converter.genCostRepJSON(rep, ext);
	}

	/**
	 * 刷卡环境自检测
	 * @return true-自检成功,false-自检失败
	 */
	private boolean selfCheck() {
		String config = "config.ini";
		serialNO      = DeviceConfig.getInstance().getValue(KEY_NO);
		serialDevNO   = DeviceConfig.getInstance().getValue(KEY_DEV_NO);
		
		if(serialNO == null) {
			Logger.error(config + " is missing item: serialNO");
			return false;
		}
		if(serialDevNO == null) {
			Logger.error(config + " is missing item: serialDevNO");
			return false;
		}
		
		Logger.info(config + " items: { serialNO=" + serialNO + ", serialDevNO=" + serialDevNO + "}");
		
		return true;
	}
	
	/**
	 * 更新serialNO到本地文件
	 */
	private void updateSrialNO() {
		serialNO = String.valueOf(Integer.valueOf(serialNO) + 1); 
		DeviceConfig.getInstance().updateKeyValue(KEY_NO, serialNO);
	}
	
	/**
	 * 绵阳通清算数据上送
	 */
	private void asynUpload(final MZTTrade trade) {
		WorkPool.executeTask(new Runnable() {
			@Override
			public void run() {
				Logger.info("[START] MZT upload data");
				MZTTradeDao.getInstance().insertOne(trade);
				Logger.info("[END] MZT upload data");
			}
		});
	}
	
}
