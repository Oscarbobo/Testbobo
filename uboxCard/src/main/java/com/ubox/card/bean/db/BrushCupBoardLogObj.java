package com.ubox.card.bean.db;

import com.alibaba.fastjson.JSON;
import com.ubox.card.db.DbConst;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrushCupBoardLogObj extends HashMap<String, Object> {
	private static final long serialVersionUID = 1123685465490786L;

    public static final String MAP_KEY_BRUSH_ID        = "id";
    public static final String MAP_KEY_VM_ID           = "vmId";
    public static final String MAP_KEY_MDSE_ID         = "mdseId";
    public static final String MAP_KEY_CARD_NO         = "cardNo";
    public static final String MAP_KEY_EMPLOYEE_NO     = "employeeNo";
    public static final String MAP_KEY_CARD_DESC       = "cardDesc";
    public static final String MAP_KEY_BRUSH_TIME      = "brushTime";
    public static final String MAP_KEY_CREATE_TIME     = "createTime";
    public static final String MAP_KEY_COST_SEQ        = "costSeq";
    public static final String MAP_KEY_BRUSH_SEQ       = "brushSeq";
    public static final String MAP_KEY_MDSE_NAME       = "mdseName";
    public static final String MAP_KEY_MDSE_PRICE      = "mdsePrice";
    public static final String MAP_KEY_COST_MONEY      = "costMoney";
    public static final String MAP_KEY_COST_STATUS     = "costStatus";
    public static final String MAP_KEY_IS_VALI_DATE    = "isValiDate";
    public static final String MAP_KEY_COST_TIME       = "costTime";
    public static final String MAP_KEY_CARD_TYPE       = "cardType";
    public static final String MAP_KEY_APP_TYPE        = "appType";
    public static final String MAP_KEY_POS_ID          = "posId";

	@SuppressWarnings("unchecked")
	public BrushCupBoardLogObj(List<Object> records) {
		for (int i = 0; i < records.size(); i++) {
			Map<String, Object> record = (Map<String, Object>) records.get(i);
			this.put(String.valueOf(record.get(MAP_KEY_BRUSH_ID)), record);
		}
	}

	public BrushCupBoardLogObj(Map<String, Map<String, Object>> records) {
		for (String key : records.keySet()) {
			this.put(key, records.get(key));
		}
	}

	public String tableName() {
		return DbConst.TABLE_BRUSHCUPBOARDLOG;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static class BrushCupBoardLog extends HashMap {
		private static final long serialVersionUID = 4275403643042469919L;

		private String id;
		private String vmId;        // 售货机ID
		private Integer mdseId;     // 商品ID
		private String cardNo;      // 卡号
		private String employeeNo;  // 工号
		private String cardDesc;    // 描述
		private String brushTime;   // 刷卡时间
		private String createTime;  // 创建时间
		private String costSeq;     // 扣款流水
		private String brushSeq;    // 刷卡流水号
		private String mdseName;    // 商品名字
		private Integer mdsePrice;  // 商品价格
		private Integer costMoney;  // 扣款金额
		private Integer costStatus; // 扣款状态 0扣款成功 1扣款失败 2未扣款 3正在扣款
		private Integer isValiDate; // 支持验卡 0不支持 1支持
		private String costTime;    // 消费时间
		private Integer cardType;   // 刷卡类型
		private Integer appType;    // 应用类型
		private String posId;       // 刷卡设备终端号

		public BrushCupBoardLog() {
		}

		public BrushCupBoardLog(String jsonRecord) {
			Map<String, Object> record = (Map<String, Object>) JSON.parse(jsonRecord);
			
			setId((String) record.get(MAP_KEY_BRUSH_ID));
			setVmId((String) record.get(MAP_KEY_VM_ID));
			setMdseId((Integer) record.get(MAP_KEY_MDSE_ID));
			setCardNo((String) record.get(MAP_KEY_CARD_NO));
			setEmployeeNo((String) record.get(MAP_KEY_EMPLOYEE_NO));
			setCardDesc((String) record.get(MAP_KEY_CARD_DESC));
			setBrushTime((String) record.get(MAP_KEY_BRUSH_TIME));
			setCreateTime((String) record.get(MAP_KEY_CREATE_TIME));
			setCostSeq((String) record.get(MAP_KEY_COST_SEQ));
			setBrushSeq((String) record.get(MAP_KEY_BRUSH_SEQ));
			setMdseName((String) record.get(MAP_KEY_MDSE_NAME));
			setMdsePrice((Integer) record.get(MAP_KEY_MDSE_PRICE));
			setCostMoney((Integer) record.get(MAP_KEY_COST_MONEY));
			setCostStatus((Integer) record.get(MAP_KEY_COST_STATUS));
			setIsValiDate((Integer) record.get(MAP_KEY_IS_VALI_DATE));
			setCostTime((String) record.get(MAP_KEY_COST_TIME));
			setCardType((Integer) record.get(MAP_KEY_CARD_TYPE));
			setAppType((Integer) record.get(MAP_KEY_APP_TYPE));
			setPosId((String) record.get(MAP_KEY_POS_ID));
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
			this.put(MAP_KEY_BRUSH_ID, id);
		}

		public String getVmId() {
			return vmId;
		}

		public void setVmId(String vmId) {
			this.vmId = vmId;
			this.put(MAP_KEY_VM_ID, vmId);
		}

		public Integer getMdseId() {
			return mdseId;
		}

		public void setMdseId(Integer mdseId) {
			this.mdseId = mdseId;
			this.put(MAP_KEY_MDSE_ID, mdseId);
		}

		public String getCardNo() {
			return cardNo;
		}

		public void setCardNo(String cardNo) {
			this.cardNo = cardNo;
			this.put(MAP_KEY_CARD_NO, cardNo);
		}

		public String getEmployeeNo() {
			return employeeNo;
		}

		public void setEmployeeNo(String employeeNo) {
			this.employeeNo = employeeNo;
			this.put(MAP_KEY_EMPLOYEE_NO, employeeNo);
		}

		public String getCardDesc() {
			return cardDesc;
		}

		public void setCardDesc(String cardDesc) {
			this.cardDesc = cardDesc;
			this.put(MAP_KEY_CARD_DESC, cardDesc);
		}

		public String getBrushTime() {
			return brushTime;
		}

		public void setBrushTime(String brushTime) {
			this.brushTime = brushTime;
			this.put(MAP_KEY_BRUSH_TIME, brushTime);
		}

		public String getCreateTime() {
			return createTime;
		}

		public void setCreateTime(String createTime) {
			this.createTime = createTime;
			this.put(MAP_KEY_CREATE_TIME, createTime);
		}

		public String getCostSeq() {
			return costSeq;
		}

		public void setCostSeq(String costSeq) {
			this.costSeq = costSeq;
			this.put(MAP_KEY_COST_SEQ, costSeq);
		}

		public String getMdseName() {
			return mdseName;
		}

		public void setMdseName(String mdseName) {
			this.mdseName = mdseName;
			this.put(MAP_KEY_MDSE_NAME, mdseName);
		}

		public Integer getMdsePrice() {
			return mdsePrice;
		}

		public void setMdsePrice(Integer mdsePrice) {
			this.mdsePrice = mdsePrice;
			this.put(MAP_KEY_MDSE_PRICE, mdsePrice);
		}

		public Integer getCostMoney() {
			return costMoney;
		}

		public void setCostMoney(Integer costMoney) {
			this.costMoney = costMoney;
			this.put(MAP_KEY_COST_MONEY, costMoney);
		}

		public Integer getCostStatus() {
			return costStatus;
		}

		public void setCostStatus(Integer costStatus) {
			this.costStatus = costStatus;
			this.put(MAP_KEY_COST_STATUS, costStatus);
		}

		public String getBrushSeq() {
			return brushSeq;
		}

		public void setBrushSeq(String brushSeq) {
			this.brushSeq = brushSeq;
			this.put(MAP_KEY_BRUSH_SEQ, brushSeq);
		}

		public Integer getIsValiDate() {
			return isValiDate;
		}

		public void setIsValiDate(Integer isValiDate) {
			this.isValiDate = isValiDate;
			this.put(MAP_KEY_IS_VALI_DATE, isValiDate);
		}


		public String getCostTime() {
			return costTime;
		}

		public void setCostTime(String costTime) {
			this.costTime = costTime;
			this.put(MAP_KEY_COST_TIME, costTime);
		}

		public Integer getCardType() {
			return cardType;
		}

		public void setCardType(Integer cardType) {
			this.cardType = cardType;
			this.put(MAP_KEY_CARD_TYPE, cardType);
		}

		public Integer getAppType() {
			return appType;
		}

		public void setAppType(Integer appType) {
			this.appType = appType;
			this.put(MAP_KEY_APP_TYPE, appType);
		}

		public String getPosId() {
			return posId;
		}

		public void setPosId(String posId) {
			this.posId = posId;
			this.put(MAP_KEY_POS_ID, posId);
		}

	}
}
