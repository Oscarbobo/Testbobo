/*
 * Copyright (c) 2012 友宝在线. 
 * All Rights Reserved. 保留所有权利.
 */
package com.ubox.card.bean.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.fastjson.JSON;
import com.ubox.card.db.DbConst;

/**
 * quickpass银联闪付
 * @author gaolei
 * @version 2015-4-28
 */

public class QuickPassTradeObj extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;

	public static final String MAP_KEY_QUICK_PASS_ID = "id";
	public static final String MAP_KEY_PINPUT_DATA = "pInputData";
	public static final String MAP_KEY_PCARD_NO = "pCardNo";
	public static final String MAP_KEY_PTRADE_MONEY = "pTradeMoney";
	public static final String MAP_KEY_PCARD_SER_NO = "pCardSerNo";
	public static final String MAP_KEY_PSERIAL_NO = "pSeialNo";
	public static final String MAP_KEY_UPLOAD_TIME = "uploadTime";
	public static final String MAP_KEY_PICDATA_LEN = "pICDataLen";

	@SuppressWarnings("unchecked")
	public QuickPassTradeObj(List<Object> records) {
		for (int i = 0; i < records.size(); i++) {
			Map<String, Object> record = (Map<String, Object>) records.get(i);
			this.put(String.valueOf(record.get(MAP_KEY_QUICK_PASS_ID)), record);
		}
	}

	public QuickPassTradeObj(Map<String, Map<String, Object>> records) {
		for (String key : records.keySet()) {
			this.put(key, records.get(key));
		}
	}

	public String tableName() {
		return DbConst.TABLE_QUICKPASSTRADE;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static class QuickPassTrade extends HashMap {
		private static final long serialVersionUID = 4275403643042469919L;

		private String id;//主键
		private String pInputData;//55域数据
		private String pCardNo;// 卡号
		private String pTradeMoney;// 金额
		private String pCardSerNo;// 卡序列号
		private String pSerialNo;//流水号
		private String pIcDataLen;//55数据长度
		/**
		 * @return the pIcDataLen
		 */
		public String getPIcDataLen() {
			return pIcDataLen;
		}

		/**
		 * @param pIcDataLen the pIcDataLen to set
		 */
		public void setPIcDataLen(String pIcDataLen) {
			this.put(MAP_KEY_PICDATA_LEN, pIcDataLen);
			this.pIcDataLen = pIcDataLen;
		}

		private String uploadTime;//时间写入quickpass.db

		public QuickPassTrade() {
		}

		public QuickPassTrade(String jsonRecord) {
			Map<String, Object> record = (Map<String, Object>) JSON.parse(jsonRecord);
			
			setId((String) record.get(MAP_KEY_QUICK_PASS_ID));
			setPInputData((String) record.get(MAP_KEY_PINPUT_DATA));
			setPCardNo((String) record.get(MAP_KEY_PCARD_NO));
			setPTradeMoney((String)(record.get(MAP_KEY_PTRADE_MONEY)));
			setPCardSerNo((String) record.get(MAP_KEY_PCARD_SER_NO));
			setPSerialNo((String)record.get(MAP_KEY_PSERIAL_NO));
			setUploadTime((String)record.get(MAP_KEY_UPLOAD_TIME));
			setPIcDataLen((String)record.get(MAP_KEY_PICDATA_LEN));
		}

		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}

		/**
		 * @param id the id to set
		 */
		public void setId(String id) {
			this.id = id;
			this.put(MAP_KEY_QUICK_PASS_ID, id);
		}

		public String getPInputData() {
			return pInputData;
		}

		public void setPInputData(String pInputData) {
			this.pInputData = pInputData;
			this.put(MAP_KEY_PINPUT_DATA, pInputData);
		}

		public String getPCardNo() {
			return pCardNo;
		}

		public void setPCardNo(String pCardNo) {
			this.pCardNo = pCardNo;
			this.put(MAP_KEY_PCARD_NO, pCardNo);
		}

		public String getPTradeMoney() {
			return pTradeMoney;
		}

		public void setPTradeMoney(String pTradeMoney) {
			this.pTradeMoney = pTradeMoney;
			this.put(MAP_KEY_PTRADE_MONEY, pTradeMoney);
		}

		public String getPCardSerNo() {
			return pCardSerNo;
		}

		public void setPCardSerNo(String pCardSerNo) {
			this.pCardSerNo = pCardSerNo;
			this.put(MAP_KEY_PCARD_SER_NO, pCardSerNo);
		}
		
		
		public String getPSerialNo() {
			return pSerialNo;
		}

		public void setPSerialNo(String pSerialNo) {
			this.pSerialNo = pSerialNo;
			this.put(MAP_KEY_PSERIAL_NO, pSerialNo);
		}
		
		public String getUploadTime() {
			return uploadTime;
		}

		public void setUploadTime(String uploadTime) {
			this.uploadTime = uploadTime;
		}
		
		public String toString(){
			return ToStringBuilder.reflectionToString(this);
		}

	}

}
