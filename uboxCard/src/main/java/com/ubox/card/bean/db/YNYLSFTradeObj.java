package com.ubox.card.bean.db;

import com.alibaba.fastjson.JSON;
import com.ubox.card.db.DbConst;

import java.util.HashMap;
import java.util.Map;

public class YNYLSFTradeObj extends HashMap<String, Object> {

	private static final long serialVersionUID = 5260357939322680236L;

	public static final String MAP_KEY_BRUSH_ID  = "id";

    public static final String MAP_KEY_BRUSH_SEQ = "brushSeq";

    public static final String MAP_KEY_VM_ID     = "vmId";

    public static final String MAP_KEY_DEVNO     = "devNO";

    public static final String MAP_KEY_TRADEDATA = "tradeData";

    public static final String MAP_KEY_CARDNO    = "cardNO";

    public static final String MAP_KEY_TRADETIME = "tradeTime";

    public static final String MAP_KEY_TRADEFEE  = "tradeFee";

    public static final String MAP_KEY_MERCHANT  = "merchant";
    
    public static final String MAP_KEY_APPTYPE  = "appNO";


    public String tableName() {
        return DbConst.TABLE_YNYLSFTRADE;
    }

	@SuppressWarnings({ "unchecked", "rawtypes" })
    public static class UnionpayTrade extends HashMap {

		private static final long serialVersionUID = -3595454702853409539L;

		private String id;

        private String brushSeq;   // vcard流水号

        private String vmId;       // 售货机ID

        private String devNO;      // POS终端号

        private String tradeData;  // 脱机交易数据

        private String cardNO;     // 交易卡号

        private String tradeTime;  // 交易日期

        private String tradeFee;   // 交易金额

        private String merchant;   // 商户号
        
        private String appNO;  	   // 售货机编号

		public UnionpayTrade() {
        }

        public UnionpayTrade(String json) {
            Map<String, Object> record = (Map<String, Object>) JSON.parse(json);

            setId((String)record.get(MAP_KEY_BRUSH_ID));
            setBrushSeq((String)record.get(MAP_KEY_BRUSH_SEQ));
            setVmId((String)record.get(MAP_KEY_VM_ID));
            setCardNO((String)record.get(MAP_KEY_CARDNO));
            setDevNO((String)record.get(MAP_KEY_DEVNO));
            setTradeData((String)record.get(MAP_KEY_TRADEDATA));
            setTradeTime((String)record.get(MAP_KEY_TRADETIME));
            setTradeFee((String)record.get(MAP_KEY_TRADEFEE));
            setMerchant((String)record.get(MAP_KEY_MERCHANT));
            setAppNO((String)record.get(MAP_KEY_APPTYPE));
        }

        public String getAppNO() {
			return appNO;
		}

		public void setAppNO(String appNO) {
			this.appNO = appNO;
			this.put(MAP_KEY_APPTYPE, appNO);
		}
		
        public String getId() {
            return id;
        }

		public void setId(String id) {
            this.id = id;
            this.put(MAP_KEY_BRUSH_ID, id);
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

        public String getDevNO() {
            return devNO;
        }

        public void setDevNO(String devNO) {
            this.devNO = devNO;
            this.put(MAP_KEY_DEVNO, devNO);
        }

        public String getTradeData() {
            return tradeData;
        }

        public void setTradeData(String tradeData) {
            this.tradeData = tradeData;
            this.put(MAP_KEY_TRADEDATA, tradeData);
        }

        public String getCardNO() {
            return cardNO;
        }

        public void setCardNO(String cardNO) {
            this.cardNO = cardNO;
            this.put(MAP_KEY_CARDNO, cardNO);
        }

        public String getTradeTime() {
            return tradeTime;
        }

        public void setTradeTime(String tradeTime) {
            this.tradeTime = tradeTime;
            this.put(MAP_KEY_TRADETIME, tradeTime);
        }

        public String getTradeFee() {
            return tradeFee;
        }

        public void setTradeFee(String tradeFee) {
            this.tradeFee = tradeFee;
            this.put(MAP_KEY_TRADEFEE, tradeFee);
        }

        public String getMerchant() {
            return merchant;
        }

        public void setMerchant(String merchant) {
            this.merchant = merchant;
            this.put(MAP_KEY_MERCHANT, merchant);
        }
    }
}
