package com.ubox.card.bean.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.ubox.card.db.DbConst;

public class HZSMKTradeObj extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;

	public static final String MAP_KEY_BRUSH_SEQ = "brushSeq";
	public static final String MAP_KEY_VM_ID = "vmId";
	public static final String MAP_KEY_CLIENT_TIME = "clientTime";
	public static final String MAP_KEY_CARD_DESC = "cardDesc";
	public static final String MAP_KEY_PRODUCT_ID = "productId";
	public static final String MAP_KEY_PRODUCT_NAME = "productName";
	public static final String MAP_KEY_TRADE_TYPE = "tradeType";
	public static final String MAP_KEY_CARD_NO = "cardNo";
	public static final String MAP_KEY_RELEASE_NO = "releaseNo";
	public static final String MAP_KEY_CERTIFY_CODE = "certifyCode";
	public static final String MAP_KEY_CITY_CODE = "cityCode";
	public static final String MAP_KEY_INDUSTRY_CODE = "industryCode";
	public static final String MAP_KEY_M1USE_FLAG = "m1UseFlag";
	public static final String MAP_KEY_CARD_TYPE = "cardType";
	public static final String MAP_KEY_VALID_DATE = "validDate";
	public static final String MAP_KEY_USE_DATE = "useDate";
	public static final String MAP_KEY_ADDMONEY_DATE = "addMoneyDate";
	public static final String MAP_KEY_M1ADDMONEY_BALANCE = "m1AddMoneyBalance";
	public static final String MAP_KEY_WALLET_BALANCE = "walletBalance";
	public static final String MAP_KEY_WALLET_TRADE_NO = "walletTradeNo";
	public static final String MAP_KEY_M1BLACK_FLAG = "m1BlackFlag";
	public static final String MAP_KEY_CHECK_DATE = "checkDate";
	public static final String MAP_KEY_NOW_OPERATOR_NO = "nowOperatorNo";
	public static final String MAP_KEY_SAK_VALUE = "sakValue";
	public static final String MAP_KEY_APP_NO = "appNo";
	public static final String MAP_KEY_SUB_CARD_TYPE = "subCardType";
	public static final String MAP_KEY_CPU_WALLET_USEFLAG = "cpuWalletUseFlag";
	public static final String MAP_KEY_PSAM_CARD_NO = "psamCardNo";
	public static final String MAP_KEY_TRADE_TIME = "tradeTime";
	public static final String MAP_KEY_TRADE_MONEY = "tradeMoney";
	public static final String MAP_KEY_BALANCE = "balance";
	public static final String MAP_KEY_TAC = "tac";
	public static final String MAP_KEY_PSAM_OFFLINE_TRADENO = "psamOfflineTradeNo";
	public static final String MAP_KEY_POS_TRADE_NO = "posTradeNo";
	public static final String MAP_KEY_PRODUCT_PRICE = "productPrice";
	
	@SuppressWarnings("unchecked")
	public HZSMKTradeObj(List<Object> records) {
		for (int i = 0; i < records.size(); i++) {
			Map<String, Object> record = (Map<String, Object>) records.get(i);
			this.put(String.valueOf(record.get(MAP_KEY_CARD_NO)), record);
		}
	}

	public HZSMKTradeObj(Map<String, Map<String, Object>> records) {
		for (String key : records.keySet()) {
			this.put(key, records.get(key));
		}
	}
	
	public String tableName() {
		return DbConst.TABLE_HZSMKTRADE;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static class HZSMKTrade extends HashMap {
		private static final long serialVersionUID = -3763160417155398678L;
		
		private String brushSeq;
		private String vmId;//售货机号
		private String clientTime;//售货机时间
		private String cardDesc;//备注描述字段
		private String productId;//商品ID
		private String productName;//商品名称
		private String tradeType;
		private String cardNo;//卡号
		private String releaseNo;//发行流水号
		private String certifyCode;//认证码
		private String cityCode;//城市代码
		private String industryCode;//行业代码
		private String m1UseFlag;//m1启用标志
		private String cardType;//卡类型
		private String validDate;//有效日期
		private String useDate;//启用日期
		private String addMoneyDate;//加款日期
		private String m1AddMoneyBalance;//M1上次充值后余额
		private String walletBalance;//钱包区金额（逆）
		private String walletTradeNo;//钱包交易序列号
		private String m1BlackFlag;//M1黑名单标志
		private String checkDate;//年检日期
		private String nowOperatorNo;//本次操作员编号
		private String sakValue;//sak值
		private String appNo;//应用序列号
		private String subCardType;//卡子类型
		private String cpuWalletUseFlag;
		private String psamCardNo;//住建部PSAM卡号
		private String tradeTime;//交易时间
		private String tradeMoney;//交易金额（逆）
		private String balance;//原额
		private String tac;//TAC
		private String psamOfflineTradeNo;//PSAM脱机交易序号
		private String posTradeNo;//pos机交易流水号
		private String productPrice;//商品金额
		
		public HZSMKTrade() {
		}
		
		public HZSMKTrade(String jsonRecord) {
			Map<String, Object> record = (Map<String, Object>) JSON.parse(jsonRecord);
			
			setBrushSeq((String) record.get(MAP_KEY_BRUSH_SEQ));
			setVmId((String) record.get(MAP_KEY_VM_ID));
			setClientTime((String) record.get(MAP_KEY_CLIENT_TIME));
			setCardDesc((String) record.get(MAP_KEY_CARD_DESC));
			setProductId((String) record.get(MAP_KEY_PRODUCT_ID));
			setProductName((String) record.get(MAP_KEY_PRODUCT_NAME));
			setTradeType((String) record.get(MAP_KEY_TRADE_TYPE));
			setCardNo((String) record.get(MAP_KEY_CARD_NO));
			setReleaseNo((String) record.get(MAP_KEY_RELEASE_NO));
			setCertifyCode((String) record.get(MAP_KEY_CERTIFY_CODE));
			setCityCode((String) record.get(MAP_KEY_CITY_CODE));
			setIndustryCode((String) record.get(MAP_KEY_INDUSTRY_CODE));
			setM1UseFlag((String) record.get(MAP_KEY_M1USE_FLAG));
			setCardType((String) record.get(MAP_KEY_CARD_TYPE));
			setValidDate((String) record.get(MAP_KEY_VALID_DATE));
			setUseDate((String) record.get(MAP_KEY_USE_DATE));
			setAddMoneyDate((String) record.get(MAP_KEY_ADDMONEY_DATE ));
			setM1AddMoneyBalance((String) record.get(MAP_KEY_M1ADDMONEY_BALANCE ));
			setWalletBalance((String) record.get(MAP_KEY_WALLET_BALANCE ));
			setWalletTradeNo((String) record.get(MAP_KEY_WALLET_TRADE_NO ));
			setM1BlackFlag((String) record.get(MAP_KEY_M1BLACK_FLAG ));
			setCheckDate((String) record.get(MAP_KEY_CHECK_DATE ));
			setNowOperatorNo((String) record.get(MAP_KEY_NOW_OPERATOR_NO ));
			setSakValue((String) record.get(MAP_KEY_SAK_VALUE ));
			setAppNo((String) record.get(MAP_KEY_APP_NO ));
			setSubCardType((String) record.get(MAP_KEY_SUB_CARD_TYPE ));
			setCpuWalletUseFlag((String) record.get(MAP_KEY_CPU_WALLET_USEFLAG ));
			setPsamCardNo((String) record.get(MAP_KEY_PSAM_CARD_NO ));
			setTradeTime((String) record.get(MAP_KEY_TRADE_TIME ));
			setTradeMoney((String) record.get(MAP_KEY_TRADE_MONEY ));
			setBalance((String) record.get(MAP_KEY_BALANCE ));
			setTac((String) record.get(MAP_KEY_TAC ));
			setPsamOfflineTradeNo((String) record.get(MAP_KEY_PSAM_OFFLINE_TRADENO ));
			setPosTradeNo((String) record.get(MAP_KEY_POS_TRADE_NO ));
			setProductPrice((String) record.get(MAP_KEY_PRODUCT_PRICE ));
		}

		public String getBrushSeq() {
			return brushSeq;
		}

		public void setBrushSeq(String brushSeq) {
			this.brushSeq = brushSeq;
			this.put(MAP_KEY_BRUSH_SEQ, brushSeq);
		}

		public String getVmId() {
			return vmId;
		}

		public void setVmId(String vmId) {
			this.vmId = vmId;
			this.put(MAP_KEY_VM_ID, vmId);
		}

		public String getClientTime() {
			return clientTime;
		}

		public void setClientTime(String clientTime) {
			this.clientTime = clientTime;
			this.put(MAP_KEY_CLIENT_TIME, clientTime);
		}

		public String getCardDesc() {
			return cardDesc;
		}

		public void setCardDesc(String cardDesc) {
			this.cardDesc = cardDesc;
			this.put(MAP_KEY_CARD_DESC, cardDesc);
		}

		public String getProductId() {
			return productId;
		}

		public void setProductId(String productId) {
			this.productId = productId;
			this.put(MAP_KEY_PRODUCT_ID, productId);
		}

		public String getProductName() {
			return productName;
		}

		public void setProductName(String productName) {
			this.productName = productName;
			this.put(MAP_KEY_PRODUCT_NAME, productName);
		}

		public String getTradeType() {
			return tradeType;
		}

		public void setTradeType(String tradeType) {
			this.tradeType = tradeType;
			this.put(MAP_KEY_TRADE_TYPE, tradeType);
		}

		public String getCardNo() {
			return cardNo;
		}

		public void setCardNo(String cardNo) {
			this.cardNo = cardNo;
			this.put(MAP_KEY_CARD_NO, cardNo);
		}

		public String getReleaseNo() {
			return releaseNo;
		}

		public void setReleaseNo(String releaseNo) {
			this.releaseNo = releaseNo;
			this.put(MAP_KEY_RELEASE_NO, releaseNo);
		}

		public String getCertifyCode() {
			return certifyCode;
		}

		public void setCertifyCode(String certifyCode) {
			this.certifyCode = certifyCode;
			this.put(MAP_KEY_CERTIFY_CODE, certifyCode);
		}

		public String getCityCode() {
			return cityCode;
		}

		public void setCityCode(String cityCode) {
			this.cityCode = cityCode;
			this.put(MAP_KEY_CITY_CODE, cityCode);
		}

		public String getIndustryCode() {
			return industryCode;
		}

		public void setIndustryCode(String industryCode) {
			this.industryCode = industryCode;
			this.put(MAP_KEY_INDUSTRY_CODE, industryCode);
		}

		public String getM1UseFlag() {
			return m1UseFlag;
		}

		public void setM1UseFlag(String m1UseFlag) {
			this.m1UseFlag = m1UseFlag;
			this.put(MAP_KEY_M1USE_FLAG, m1UseFlag);
		}

		public String getCardType() {
			return cardType;
		}

		public void setCardType(String cardType) {
			this.cardType = cardType;
			this.put(MAP_KEY_CARD_TYPE, cardType);
		}

		public String getValidDate() {
			return validDate;
		}

		public void setValidDate(String validDate) {
			this.validDate = validDate;
			this.put(MAP_KEY_VALID_DATE, validDate);
		}

		public String getUseDate() {
			return useDate;
		}

		public void setUseDate(String useDate) {
			this.useDate = useDate;
			this.put(MAP_KEY_USE_DATE, useDate);
		}

		public String getAddMoneyDate() {
			return addMoneyDate;
		}

		public void setAddMoneyDate(String addMoneyDate) {
			this.addMoneyDate = addMoneyDate;
			this.put(MAP_KEY_ADDMONEY_DATE, addMoneyDate);
		}

		public String getM1AddMoneyBalance() {
			return m1AddMoneyBalance;
		}

		public void setM1AddMoneyBalance(String m1AddMoneyBalance) {
			this.m1AddMoneyBalance = m1AddMoneyBalance;
			this.put(MAP_KEY_M1ADDMONEY_BALANCE, m1AddMoneyBalance);
		}

		public String getWalletBalance() {
			return walletBalance;
		}

		public void setWalletBalance(String walletBalance) {
			this.walletBalance = walletBalance;
			this.put(MAP_KEY_WALLET_BALANCE, walletBalance);
		}

		public String getWalletTradeNo() {
			return walletTradeNo;
		}

		public void setWalletTradeNo(String walletTradeNo) {
			this.walletTradeNo = walletTradeNo;
			this.put(MAP_KEY_WALLET_TRADE_NO, walletTradeNo);
		}

		public String getM1BlackFlag() {
			return m1BlackFlag;
		}

		public void setM1BlackFlag(String m1BlackFlag) {
			this.m1BlackFlag = m1BlackFlag;
			this.put(MAP_KEY_M1BLACK_FLAG, m1BlackFlag);
		}

		public String getCheckDate() {
			return checkDate;
		}

		public void setCheckDate(String checkDate) {
			this.checkDate = checkDate;
			this.put(MAP_KEY_CHECK_DATE, checkDate);
		}

		public String getNowOperatorNo() {
			return nowOperatorNo;
		}

		public void setNowOperatorNo(String nowOperatorNo) {
			this.nowOperatorNo = nowOperatorNo;
			this.put(MAP_KEY_NOW_OPERATOR_NO, nowOperatorNo);
		}

		public String getSakValue() {
			return sakValue;
		}

		public void setSakValue(String sakValue) {
			this.sakValue = sakValue;
			this.put(MAP_KEY_SAK_VALUE, sakValue);
		}

		public String getAppNo() {
			return appNo;
		}

		public void setAppNo(String appNo) {
			this.appNo = appNo;
			this.put(MAP_KEY_APP_NO, appNo);
		}

		public String getSubCardType() {
			return subCardType;
		}

		public void setSubCardType(String subCardType) {
			this.subCardType = subCardType;
			this.put(MAP_KEY_SUB_CARD_TYPE, subCardType);
		}

		public String getCpuWalletUseFlag() {
			return cpuWalletUseFlag;
		}

		public void setCpuWalletUseFlag(String cpuWalletUseFlag) {
			this.cpuWalletUseFlag = cpuWalletUseFlag;
			this.put(MAP_KEY_CPU_WALLET_USEFLAG, cpuWalletUseFlag);
		}

		public String getPsamCardNo() {
			return psamCardNo;
		}

		public void setPsamCardNo(String psamCardNo) {
			this.psamCardNo = psamCardNo;
			this.put(MAP_KEY_PSAM_CARD_NO, psamCardNo);
		}

		public String getTradeTime() {
			return tradeTime;
		}

		public void setTradeTime(String tradeTime) {
			this.tradeTime = tradeTime;
			this.put(MAP_KEY_TRADE_TIME, tradeTime);
		}

		public String getTradeMoney() {
			return tradeMoney;
		}

		public void setTradeMoney(String tradeMoney) {
			this.tradeMoney = tradeMoney;
			this.put(MAP_KEY_TRADE_MONEY, tradeMoney);
		}

		public String getBalance() {
			return balance;
		}

		public void setBalance(String balance) {
			this.balance = balance;
			this.put(MAP_KEY_BALANCE, balance);
		}

		public String getTac() {
			return tac;
		}

		public void setTac(String tac) {
			this.tac = tac;
			this.put(MAP_KEY_TAC, tac);
		}

		public String getPsamOfflineTradeNo() {
			return psamOfflineTradeNo;
		}

		public void setPsamOfflineTradeNo(String psamOfflineTradeNo) {
			this.psamOfflineTradeNo = psamOfflineTradeNo;
			this.put(MAP_KEY_PSAM_OFFLINE_TRADENO, psamOfflineTradeNo);
		}

		public String getPosTradeNo() {
			return posTradeNo;
		}

		public void setPosTradeNo(String posTradeNo) {
			this.posTradeNo = posTradeNo;
			this.put(MAP_KEY_POS_TRADE_NO, posTradeNo);
		}

		public String getProductPrice() {
			return productPrice;
		}

		public void setProductPrice(String productPrice) {
			this.productPrice = productPrice;
			this.put(MAP_KEY_PRODUCT_PRICE, productPrice);
		}
		
	}

}
