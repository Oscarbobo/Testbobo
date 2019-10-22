package com.ubox.card.bean.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.fastjson.JSON;
import com.ubox.card.db.DbConst;

public class WHTTradeObj extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;
	
	public static final String MAP_KEY_WHTDT_ID = "id";
	public static final String MAP_KEY_DEV_NO ="devNo";
	public static final String MAP_KEY_DEV_FLAG="devFlag";
	public static final String MAP_KEY_TRADE_TIME="tradeTime";
	public static final String MAP_KEY_DEV_COUNT = "devCount";
	public static final String MAP_KEY_CARD_NO = "cardNo";
	public static final String MAP_KEY_PHY_NO="phyNo";
	public static final String MAP_KEY_C_MAIN ="cMain";
	public static final String MAP_KEY_S_MAIN="sMain";
	public static final String MAP_KEY_BDEV_NO="bdevNo";
	public static final String MAP_KEY_B_TIME="bTime";
	public static final String MAP_KEY_TRADE_FEE="tradeFee";
	public static final String MAP_KEY_BALANCE ="balance";
	public static final String MAP_KEY_TRADE_TYPE="tradeType";
	public static final String MAP_KEY_TDEV_NO="tdevNo";
	public static final String MAP_KEY_THIS_TIME ="thisTime";
	public static final String MAP_KEY_TICKET_ON_COUNT="ticketOnCount";
	public static final String MAP_KEY_TICKET_OFF_COUNT="ticketOffCount";
	public static final String MAP_KEY_TAC="tac";
	public static final String MAP_KEY_IS_TEST="isTest";
	public static final String MAP_KEY_BRUSH_SEQ="brushSeq";
	public static final String MAP_KEY_VM_ID="vmId";
	
	@SuppressWarnings("unchecked")
	public WHTTradeObj(List<Object> records) {
		for (int i = 0; i < records.size(); i++) {
			Map<String, Object> record = (Map<String, Object>) records.get(i);
			this.put(String.valueOf(record.get(MAP_KEY_WHTDT_ID)), record);
		}
	}

	public WHTTradeObj(Map<String, Map<String, Object>> records) {
		for (String key : records.keySet()) {
			this.put(key, records.get(key));
		}
	}

	public String tableName() {
		return DbConst.TABLE_WHTTRADE;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static class WHTTrade extends HashMap {
		private static final long serialVersionUID = 4275403643042469919L;

		private String id ;// 武汉通地铁ID
		private String devNo;//终端编号
		private String devFlag;//终端标志
		private String tradeTime;//交易时间
		private String devCount;//终端交易流水
		private String cardNo;//票卡的逻辑卡号
		private String phyNo;//票卡物理卡号
		private String cMain;//主类型票卡
		private String sMain;//子类型票卡
		private String bdevNo;//上次交易终端编号
		private String bTime;//上次交易日期时间
		private Integer tradeFee;//交易金额（分）
		private Integer balance;//本次余额（fen）
		private String tradeType;//交易类型
		private String tdevNo;//本次入口终端编号
		private String thisTime;//本次入口日期时间
		private String ticketOnCount;//票卡联机交易计数
		private String ticketOffCount;//票卡脱机交易计数
		private String tac;//交易认证码
		private String isTest;//是否测试卡
		private String brushSeq;//刷卡seq
		private String vmId;//售货机编号
		

		public WHTTrade() {
		}

		public WHTTrade(String jsonRecord) {
			Map<String, Object> record = (Map<String, Object>) JSON.parse(jsonRecord);
			
			setId((String) record.get(MAP_KEY_WHTDT_ID));
			setDevNo((String) record.get(MAP_KEY_DEV_NO));
			setDevFlag((String) record.get(MAP_KEY_DEV_FLAG));
			setTradeTime((String) record.get(MAP_KEY_TRADE_TIME));
			setDevCount((String)record.get(MAP_KEY_DEV_COUNT));
			setPhyNo((String) record.get(MAP_KEY_PHY_NO));
			setcMain((String) record.get(MAP_KEY_C_MAIN));
			setsMain((String) record.get(MAP_KEY_S_MAIN));
			setCardNo((String)record.get(MAP_KEY_CARD_NO));
			setBdevNo((String) record.get(MAP_KEY_BDEV_NO));
			setbTime((String) record.get(MAP_KEY_B_TIME));
			setTradeFee((Integer) record.get(MAP_KEY_TRADE_FEE));
			setBalance((Integer) record.get(MAP_KEY_BALANCE));
			setTradeType((String) record.get(MAP_KEY_TRADE_TYPE));
			setTdevNo((String) record.get(MAP_KEY_TDEV_NO));
			setThisTime((String) record.get(MAP_KEY_THIS_TIME));
			setTicketOnCount((String) record.get(MAP_KEY_TICKET_ON_COUNT));
			setTicketOffCount((String) record.get(MAP_KEY_TICKET_OFF_COUNT));
			setTac((String) record.get(MAP_KEY_TAC));
			setIsTest((String) record.get(MAP_KEY_IS_TEST));
			setBrushSeq((String) record.get(MAP_KEY_BRUSH_SEQ));
			setVmId((String) record.get(MAP_KEY_VM_ID));
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
			this.put(MAP_KEY_WHTDT_ID, id);
		}

		public String getDevNo() {
			return devNo;
		}

		public void setDevNo(String devNo) {
			this.devNo = devNo;
			this.put(MAP_KEY_DEV_NO, devNo);
		}

		public String getDevFlag() {
			return devFlag;
		}

		public void setDevFlag(String devFlag) {
			this.devFlag = devFlag;
			this.put(MAP_KEY_DEV_FLAG, devFlag);
		}

		public String getTradeTime() {
			return tradeTime;
		}

		public void setTradeTime(String tradeTime) {
			this.tradeTime = tradeTime;
			this.put(MAP_KEY_TRADE_TIME, tradeTime);
		}

		public String getDevCount() {
			return devCount;
		}

		public void setDevCount(String devCount) {
			this.devCount = devCount;
			this.put(MAP_KEY_DEV_COUNT, devCount);
		}

		public String getCardNo() {
			return cardNo;
		}

		public void setCardNo(String cardNo) {
			this.cardNo = cardNo;
			this.put(MAP_KEY_CARD_NO, cardNo);
		}

		public String getPhyNo() {
			return phyNo;
		}

		public void setPhyNo(String phyNo) {
			this.phyNo = phyNo;
			this.put(MAP_KEY_PHY_NO, phyNo);
		}

		public String getcMain() {
			return cMain;
		}

		public void setcMain(String cMain) {
			this.cMain = cMain;
			this.put(MAP_KEY_C_MAIN, cMain);
		}

		public String getsMain() {
			return sMain;
		}

		public void setsMain(String sMain) {
			this.sMain = sMain;
			this.put(MAP_KEY_S_MAIN, sMain);
		}

		public String getBdevNo() {
			return bdevNo;
		}

		public void setBdevNo(String bdevNo) {
			this.bdevNo = bdevNo;
			this.put(MAP_KEY_BDEV_NO, bdevNo);
		}

		public String getbTime() {
			return bTime;
		}

		public void setbTime(String bTime) {
			this.bTime = bTime;
			this.put(MAP_KEY_B_TIME, bTime);
		}

		public Integer getTradeFee() {
			return tradeFee;
		}

		public void setTradeFee(Integer tradeFee) {
			this.tradeFee = tradeFee;
			this.put(MAP_KEY_TRADE_FEE, tradeFee);
		}

		public Integer getBalance() {
			return balance;
		}

		public void setBalance(Integer balance) {
			this.balance = balance;
			this.put(MAP_KEY_BALANCE, balance);
		}

		public String getTradeType() {
			return tradeType;
		}

		public void setTradeType(String tradeType) {
			this.tradeType = tradeType;
			this.put(MAP_KEY_TRADE_TYPE, tradeType);
		}

		public String getTdevNo() {
			return tdevNo;
		}

		public void setTdevNo(String tdevNo) {
			this.tdevNo = tdevNo;
			this.put(MAP_KEY_TDEV_NO, tdevNo);
		}

		public String getThisTime() {
			return thisTime;
		}

		public void setThisTime(String thisTime) {
			this.thisTime = thisTime;
			this.put(MAP_KEY_THIS_TIME, thisTime); 
		}

		public String getTicketOnCount() {
			return ticketOnCount;
		}

		public void setTicketOnCount(String ticketOnCount) {
			this.ticketOnCount = ticketOnCount;
			this.put(MAP_KEY_TICKET_ON_COUNT, ticketOnCount);
		}

		public String getTicketOffCount() {
			return ticketOffCount;
		}

		public void setTicketOffCount(String ticketOffCount) {
			this.ticketOffCount = ticketOffCount;
			this.put(MAP_KEY_TICKET_OFF_COUNT, ticketOffCount);
		}

		public String getTac() {
			return tac;
		}

		public void setTac(String tac) {
			this.tac = tac;
			this.put(MAP_KEY_TAC, tac);
		}

		public String getIsTest() {
			return isTest;
		}

		public void setIsTest(String isTest) {
			this.isTest = isTest;
			this.put(MAP_KEY_IS_TEST, isTest);
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
		
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
		
	}

	
}
