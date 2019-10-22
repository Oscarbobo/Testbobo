package com.ubox.card.vs;

public class VsConst {
	public static final String CHARACTER_SET = "GBK";

	/* VCardServer接口协议中所有的key */
	public static final String MAP_KEY_SESSION_EFFECTIVE = "sessionEffective";
	public static final String MAP_KEY_IS_ENCRYPT        = "isEncrypt";
	public static final String MAP_KEY_FORCE_ALL         = "forceAll";
	public static final String MAP_KEY_INNER_CODE        = "innerCode";
	public static final String MAP_KEY_DATA_PROCESS      = "dataProcess";
	public static final String MAP_KEY_SSLSESSION        = "sslSession";
	public static final String MAP_KEY_CONTENTS          = "contents";

	/* 刷卡信息同步接口 */
	public static final String MAP_KEY_BRUSHCUPBOARDLOG         = "brushCupboardLog";
	public static final String MAP_KEY_BRUSHCUPBOARDLOG_SUCCESS = "brushCupboardLogSuccess";
	
	/* 绵州通清算接口*/
	public static final String MAP_KEY_MZT_TRADE         = "mztTrade";
	public static final String MAP_KEY_MZT_TRADE_SUCCESS = "mztTradeSuccess";
	
    /* 银联刷卡脱机消费*/
    public static final String MAP_KEY_UNIONPAY_TRADE = "unionpayTrade";
    public static final String MAP_KEY_UNIONPAY_TRADE_SUCCESS = "unionpayTradeSuccess";
    
    /* 云南银联闪付同步接口*/
    public static final String MAP_KEY_YNYLSF_TRADE = "ynylsfTrade";
    public static final String MAP_KEY_YNYLSF_TRADE_SUCCESS = "ynylsfTradeSuccess";

	/* 武汉通同步接口*/
	public static final String MAP_KEY_WHTDT_TRADE = "whtdtTrade";
	public static final String MAP_KEY_WHTDT_TRADE_SUCCESS = "whtdtTradeSuccess";
	
	/* 杭州市民卡同步接口*/
	public static final String MAP_KEY_HZSMK_TRADE = "hzsmkTrade";
	public static final String MAP_KEY_HZSMK_TRADE_SUCCESS = "hzsmkTradeSuccess";
	
	/* 北京市政一卡通同步接口*/
	public static final String MAP_KEY_KCQ_TRADE = "kcqTrade";
	public static final String MAP_KEY_KCQ_TRADE_SUCCESS = "kcqTradeSuccess";
	
	/* 售货机刷卡软件信息*/
	public static final String MAP_KEY_SOFT_INFO = "vcardInfo";

}
