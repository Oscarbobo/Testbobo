package com.ubox.card.bean.db;

import com.alibaba.fastjson.JSON;
import com.ubox.card.db.DbConst;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MZTTradeObj extends HashMap<String, Object> {
	private static final long serialVersionUID = 1123685465490786L;
    
	public static final String MAP_KEY_BRUSH_ID           = "id";
	public static final String MAP_KEY_VM_ID              = "vmId";
	public static final String MAP_KEY_BRUSH_SEQ          = "brushSeq";
	public static final String MAP_KEY_TAC                = "tac";
	public static final String MAP_KEY_CARD_NO            = "cardNo";
	public static final String MAP_KEY_CITY_CODE_CARD     = "cityCodeCard";
	public static final String MAP_KEY_PASM_CARD_NO       = "pasmCardNO";
	public static final String MAP_KEY_BUSINESS_CODE      = "businessCode";
	public static final String MAP_KEY_CARD_COUNT 		  = "cardCount";
	public static final String MAP_KEY_MAIN_CARD_TYPE 	  = "mainCardType";
	public static final String MAP_KEY_SUB_CARD_TYPE 	  = "subCardType";
	public static final String MAP_KEY_B_CONSUME_BALANCE  = "bConsumeBalance";
	public static final String MAP_KEY_TRADE_FEE 	      = "tradeFee";
	public static final String MAP_KEY_COST_MONEY 		  = "costMoney";
	public static final String MAP_KEY_TRADE_DATE 		  = "tradeDate";
	public static final String MAP_KEY_TRADE_TIME 	      = "tradeTime";
	public static final String MAP_KEY_CARD_VERSION 	  = "cardVersion";
	public static final String MAP_KEY_SAM_CARD_SERIAL_NO = "samCardSerialNO";
	public static final String MAP_KEY_DEV_NO 		      = "devNO";
	public static final String MAP_KEY_POS_SERIALNO 	  = "posSerialNO";
	public static final String MAP_KEY_POS_DEVNO 		  = "posDevNO";
	public static final String MAP_KEY_CSN_NO 			  = "csnNO";
    
	@SuppressWarnings("unchecked")
	public MZTTradeObj(List<Object> records) {
		for (int i = 0; i < records.size(); i++) {
			Map<String, Object> record = (Map<String, Object>) records.get(i);
			this.put(String.valueOf(record.get(MAP_KEY_BRUSH_ID)), record);
		}
	}

	public MZTTradeObj(Map<String, Map<String, Object>> records) {
		for (String key : records.keySet()) {
			this.put(key, records.get(key));
		}
	}

	public String tableName() {
		return DbConst.TABLE_MZTTRADE;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static class MZTTrade extends HashMap {
		private static final long serialVersionUID = 4275403643042469919L;
		
		private String  id;
		private String  vmId;              // 售货机编号
		private String  brushSeq;          // 本刷卡流水号
		private String  cardNo;            // 用户卡号
		private String  cityCodeCard;      // 城市代码
		private String  pasmCardNO;        // Psam卡号
		private String  businessCode;      // 行业代码（卡片）
		private String  cardCount;         // 卡消费计数器
		private String  cardVersion;       // 卡版本号
		private String  mainCardType;      // 主卡类型
		private String  subCardType;       // 子卡类型
		private Integer bConsumeBalance;   // 消费前卡余额
		private Integer tradeFee;          // 交易金额
		private Integer costMoney;         // 实扣金额（分）
		private String  tradeDate;         // 交易日期（yyyyyMMdd）
		private String  tradeTime;         // 交易时间（HHMMSS）
		private String  tac;               // 交易产生的Tac校验码
		private String  samCardSerialNO;   // sam卡交易计数器（流水）
		private String  devNO;             // 终端编号
		private String  posSerialNO;       // pos终端流水
		private String  posDevNO;          // pos终端流水号
		private String  csnNO;             // 芯片号
		
		public MZTTrade() {
		}

		public MZTTrade(String jsonRecord) {
			Map<String, Object> record = (Map<String, Object>) JSON.parse(jsonRecord);
			
			setId(               (String) record.get(MAP_KEY_BRUSH_ID));
			setVmId(             (String) record.get(MAP_KEY_VM_ID));
			setBrushSeq(         (String) record.get(MAP_KEY_BRUSH_SEQ));
			setCardNo(           (String) record.get(MAP_KEY_CARD_NO));
			setCityCodeCard(     (String) record.get(MAP_KEY_CITY_CODE_CARD));
			setPasmCardNO(       (String) record.get(MAP_KEY_PASM_CARD_NO));
			setBusinessCode(     (String) record.get(MAP_KEY_BUSINESS_CODE));
			setCardCount(        (String) record.get(MAP_KEY_CARD_COUNT));
			setMainCardType(     (String) record.get(MAP_KEY_MAIN_CARD_TYPE));
			setSubCardType(      (String) record.get(MAP_KEY_SUB_CARD_TYPE));
			setBConsumeBalance(  (Integer) record.get(MAP_KEY_B_CONSUME_BALANCE));
			setTradeFee(         (Integer) record.get(MAP_KEY_TRADE_FEE));
			setCostMoney(        (Integer) record.get(MAP_KEY_COST_MONEY));
			setTradeDate(        (String) record.get(MAP_KEY_TRADE_DATE));
			setTradeTime(        (String) record.get(MAP_KEY_TRADE_TIME));
			setTac(              (String) record.get(MAP_KEY_TAC));
			setSamCardSerialNO(  (String) record.get(MAP_KEY_SAM_CARD_SERIAL_NO));
			setCardVersion(      (String)record.get(MAP_KEY_CARD_VERSION));
			setDevNO(            (String)record.get(MAP_KEY_DEV_NO));
			setPosSerialNO(      (String)record.get(MAP_KEY_POS_SERIALNO));
			setPosDevNO(         (String)record.get(MAP_KEY_POS_DEVNO));
			setCsnNO(            (String)record.get(MAP_KEY_CSN_NO));
		}

		public String getId() {
			return id;
		}

		public String getVmId() {
			return vmId;
		}

		public String getBrushSeq() {
			return brushSeq;
		}

		public String getCardNo() {
			return cardNo;
		}

		public String getCityCodeCard() {
			return cityCodeCard;
		}

		public String getPasmCardNO() {
			return pasmCardNO;
		}

		public String getBusinessCode() {
			return businessCode;
		}

		public String getCardCount() {
			return cardCount;
		}

		public String getCardVersion() {
			return cardVersion;
		}

		public String getMainCardType() {
			return mainCardType;
		}

		public String getSubCardType() {
			return subCardType;
		}

		public Integer getBConsumeBalance() {
			return bConsumeBalance;
		}

		public Integer getTradeFee() {
			return tradeFee;
		}

		public Integer getCostMoney() {
			return costMoney;
		}

		public String getTradeDate() {
			return tradeDate;
		}

		public String getTradeTime() {
			return tradeTime;
		}

		public String getTac() {
			return tac;
		}

		public String getSamCardSerialNO() {
			return samCardSerialNO;
		}

		public String getDevNO() {
			return devNO;
		}

		public String getPosSerialNO() {
			return posSerialNO;
		}

		public String getPosDevNO() {
			return posDevNO;
		}

		public String getCsnNO() {
			return csnNO;
		}

		public void setId(String id) {
			this.id = id;
			this.put(MAP_KEY_BRUSH_ID, id);
		}

		public void setVmId(String vmId) {
			this.vmId = vmId;
			this.put(MAP_KEY_VM_ID, vmId);
		}

		public void setBrushSeq(String brushSeq) {
			this.brushSeq = brushSeq;
			this.put(MAP_KEY_BRUSH_SEQ, brushSeq);
		}

		public void setCardNo(String cardNo) {
			this.cardNo = cardNo;
			this.put(MAP_KEY_CARD_NO, cardNo);
		}

		public void setCityCodeCard(String cityCodeCard) {
			this.cityCodeCard = cityCodeCard;
			this.put(MAP_KEY_CITY_CODE_CARD, cityCodeCard);
		}

		public void setPasmCardNO(String pasmCardNO) {
			this.pasmCardNO = pasmCardNO;
			this.put(MAP_KEY_PASM_CARD_NO, pasmCardNO);
		}

		public void setBusinessCode(String businessCode) {
			this.businessCode = businessCode;
			this.put(MAP_KEY_BUSINESS_CODE, businessCode);
		}

		public void setCardCount(String cardCount) {
			this.cardCount = cardCount;
			this.put(MAP_KEY_CARD_COUNT, cardCount);
		}

		public void setCardVersion(String cardVersion) {
			this.cardVersion = cardVersion;
			this.put(MAP_KEY_CARD_VERSION, cardVersion);
		}

		public void setMainCardType(String mainCardType) {
			this.mainCardType = mainCardType;
			this.put(MAP_KEY_MAIN_CARD_TYPE, mainCardType);
		}

		public void setSubCardType(String subCardType) {
			this.subCardType = subCardType;
			this.put(MAP_KEY_SUB_CARD_TYPE, subCardType);
		}

		public void setBConsumeBalance(Integer bConsumeBalance) {
			this.bConsumeBalance = bConsumeBalance;
			this.put(MAP_KEY_B_CONSUME_BALANCE, bConsumeBalance);
		}

		public void setTradeFee(Integer tradeFee) {
			this.tradeFee = tradeFee;
			this.put(MAP_KEY_TRADE_FEE, tradeFee);
		}

		public void setCostMoney(Integer costMoney) {
			this.costMoney = costMoney;
			this.put(MAP_KEY_COST_MONEY, costMoney);
		}

		public void setTradeDate(String tradeDate) {
			this.tradeDate = tradeDate;
			this.put(MAP_KEY_TRADE_FEE, tradeFee);
		}

		public void setTradeTime(String tradeTime) {
			this.tradeTime = tradeTime;
			this.put(MAP_KEY_TRADE_TIME, tradeTime);
		}

		public void setTac(String tac) {
			this.tac = tac;
			this.put(MAP_KEY_TAC, tac);
		}

		public void setSamCardSerialNO(String samCardSerialNO) {
			this.samCardSerialNO = samCardSerialNO;
			this.put(MAP_KEY_SAM_CARD_SERIAL_NO, samCardSerialNO);
		}

		public void setDevNO(String devNO) {
			this.devNO = devNO;
			this.put(MAP_KEY_DEV_NO, devNO);
		}

		public void setPosSerialNO(String posSerialNO) {
			this.posSerialNO = posSerialNO;
			this.put(MAP_KEY_POS_SERIALNO, posSerialNO);
		}

		public void setPosDevNO(String posDevNO) {
			this.posDevNO = posDevNO;
			this.put(MAP_KEY_POS_DEVNO, posDevNO);
		}

		public void setCsnNO(String csnNO) {
			this.csnNO = csnNO;
			this.put(MAP_KEY_CSN_NO, csnNO);
		}
		
	}
}
