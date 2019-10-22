package com.ubox.card.bean.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.ubox.card.db.DbConst;

public class QCQTradeObj extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;
	
	public static final String MAP_KEY_BRUSH_SEQ 	= 	"brushSeq";
	public static final String MAP_KEY_VM_ID		= 	"vmId";
	public static final String MAP_KEY_CLIENT_TIME 	= 	"clientTime";
	public static final String MAP_KEY_CARD_NO		= 	"cardNo";
	public static final String MAP_KEY_VALID_DATE 	=	"validDate";
	public static final String MAP_KEY_CARD_BALANCE = 	"cardBalance";
	public static final String MAP_KEY_ORDER_NO 	= 	"orderNo";
	public static final String MAP_KEY_ORDER_DATE 	= 	"orderDate";
	public static final String MAP_KEY_ORDER_TIME 	= 	"orderTime";
	public static final String MAP_KEY_ORDER_AMT 	= 	"orderAmt";
	public static final String MAP_KEY_CODE 		= 	"code";
	public static final String MAP_KEY_MSG 			= 	"msg";
	
	@SuppressWarnings("unchecked")
	public QCQTradeObj(List<Object> records) {
		for (int i = 0; i < records.size(); i++) {
			Map<String, Object> record = (Map<String, Object>) records.get(i);
			this.put(String.valueOf(record.get(MAP_KEY_CARD_NO)), record);
		}
	}

	public QCQTradeObj(Map<String, Map<String, Object>> records) {
		for (String key : records.keySet()) {
			this.put(key, records.get(key));
		}
	}
	
	public String tableName() {
		return DbConst.TABLE_QCQTRADE;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static class QCQTrade extends HashMap {
		private static final long serialVersionUID = -4080636282092275425L;
		
		private String brushSeq;
		private String vmId;
		private String clientTime;
		private String cardNo;
		private String validDate;
		private String cardBalance;
		private String orderNo;
		private String orderDate;
		private String orderTime;
		private String orderAmt;
		private String code;
		private String msg;
		
		public QCQTrade(){
			
		}
		
		public QCQTrade(String jsonRecord) {
			Map<String, Object> record = (Map<String, Object>) JSON.parse(jsonRecord);
			
			setBrushSeq((String) record.get(MAP_KEY_BRUSH_SEQ));
			setVmId((String) record.get(MAP_KEY_VM_ID));
			setClientTime((String) record.get(MAP_KEY_CLIENT_TIME));
			setCardNo((String) record.get(MAP_KEY_CARD_NO));
			setValidDate((String) record.get(MAP_KEY_VALID_DATE));
			setCardBalance((String) record.get(MAP_KEY_CARD_BALANCE));
			setOrderNo((String) record.get(MAP_KEY_ORDER_NO));
			setOrderDate((String) record.get(MAP_KEY_ORDER_DATE));
			setOrderTime((String) record.get(MAP_KEY_ORDER_TIME));
			setOrderAmt((String) record.get(MAP_KEY_ORDER_AMT));
			setCode((String) record.get(MAP_KEY_CODE));
			setMsg((String) record.get(MAP_KEY_MSG));
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

		public String getCardNo() {
			return cardNo;
		}

		public void setCardNo(String cardNo) {
			this.cardNo = cardNo;
			this.put(MAP_KEY_CARD_NO, cardNo);
		}

		public String getValidDate() {
			return validDate;
		}

		public void setValidDate(String validDate) {
			this.validDate = validDate;
			this.put(MAP_KEY_VALID_DATE, validDate);
		}

		public String getCardBalance() {
			return cardBalance;
		}

		public void setCardBalance(String cardBalance) {
			this.cardBalance = cardBalance;
			this.put(MAP_KEY_CARD_BALANCE, cardBalance);
		}

		public String getOrderNo() {
			return orderNo;
		}

		public void setOrderNo(String orderNo) {
			this.orderNo = orderNo;
			this.put(MAP_KEY_ORDER_NO, orderNo);
		}

		public String getOrderDate() {
			return orderDate;
		}

		public void setOrderDate(String orderDate) {
			this.orderDate = orderDate;
			this.put(MAP_KEY_ORDER_DATE, orderDate);
		}

		public String getOrderTime() {
			return orderTime;
		}

		public void setOrderTime(String orderTime) {
			this.orderTime = orderTime;
			this.put(MAP_KEY_ORDER_TIME, orderTime);
		}

		public String getOrderAmt() {
			return orderAmt;
		}

		public void setOrderAmt(String orderAmt) {
			this.orderAmt = orderAmt;
			this.put(MAP_KEY_ORDER_AMT, orderAmt);
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
			this.put(MAP_KEY_CODE, code);
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
			this.put(MAP_KEY_MSG, msg);
		}
		
	}
}
