package com.ubox.card.bean;

/**
 * 
 * VCMsgConst
 * 
 * @author gaolei
 * @version 1.0
 * @date 2011-9-13
 */
public class BeanConst {

	/** 错误编码：200-标识成功 */
	public static final int ERROR_CODE_SUCCESS = 200;
	public static final String ERROR_MSG_SUCCESS = "success";

	/** 错误编码: 202-刷卡正在进行中  */
	public static final int ERROR_CODE_STATUS_FAILED = 202;
	
	/** 错误编码: 203-没有刷卡设备 */
	public static final int ERROR_CODE_NOCARDDEVICE = 203;
	
	/** 错误编码：205-消息处理异常 */
	public static final int ERROR_CODE_NAK = 205;
	public static final String ERROR_MSG_NAK = "exception when process message";
	
	/**错误编码:302-刷卡超时 */
	public static final int ERROR_CODE_TIMEOUT = 302;
	public static final String ERROR_MSG_TIMEOUT = "交易超时";
	//302
	/** 错误编码：309-余额不足 */
	public static final int ERROR_CODD_BALANCE_LESS = 309;
	public static final String ERROR_MSG_BALANCE_LESS = "余额不足";
	
	/** 错误编码：401-标识扣款失败 */
	public static final int ERROR_CODE_COST_FAILED = 401;
	public static final String ERROR_MSG_COST_FAILED = "扣款失败";

	/** 错误编码：402-验卡失败 */
	public static final int ERROR_CODE_VALID_FAILED = 402;
	public static final String ERROR_MSG_VALID_FAILED = "卡验证失败";

	/** 错误编码: 403-UVP反馈付账方式错误 */
	public static final int ERROR_CODE_PAYMENTERROR = 403;
	public static final String ERROR_MSG_PAYMENTERROR = "payment error";
	
	/** 错误编码：404-验卡等待超时 */
	public static final int ERROR_CODE_VALID_TIMEOUT = 404;
	public static final String ERROR_MSG_VALID_TIMEOUT = "cardInfo time out.";
	
	/** 错误编码: 405-扣费等待超时*/
	public static final int ERROR_CODE_COST_TIMEOUT = 405;
	public static final String ERROR_MSG_COST_TIMEOUT = "cost time out.";
	
	/** 错误编码：406-出货等待超时 */
	public static final int ERROR_CODE_VENDOUT_TIMEOUT = 406;
	public static final String ERROR_MSG_VENDOUT_TIMEOUT = "vendout time out.";
	
	/** 错误编码：410-反馈card底层信息  **/
	public static final int ERROR_CODE_UNDERLYING = 410;
	
	/** 错误编码：415-撤销交易 **/
	public static final int ERROR_CODE_CANCEL = 415;
    public static final String ERROR_MSG_CANCEL = "cancel trade";
    
    /** 错误编码：416-设备不能刷卡 **/
    public static final int ERROR_CODE_WORK_EXCEPTION = 416;
    public static final String ERROR_MSG_WORK_EXCEPTION = "Device can not work.";
    
	/** 错误编码：msg == null */
	public static final int ERROR_CODE_MSG_NULL = 503;

	public static final String TEXT_STYLE_MARQUEE = "marquee";
	public static final String TEXT_POSITION_BOTTOM = "bottom";
	public static final String TEXT_STYLE_SHOW = "show";

	/** 协议版本 */
	public static final String VERSION = "1.3";

	/** 协议关键字 */
	public static final String ACK = "Ack";
	public static final String NAK = "Nak";

	public static final String VC2CARD_STATUSREQ = "StatusReq";
	public static final String VC2CARD_CARDSTARTREQ = "CardStartReq";
	public static final String VC2CARD_PAYMENTTYPERESULTRPT = "PaymentTypeResultRpt";
	public static final String VC2CARD_VENDOUTRESULTRPT = "VendoutResultRpt";
	public static final String VC2CARD_CARDINFOREQ = "CardInfoReq";//卡信息结果报考

	public static final String CARD2VC_STATUSRPT = "StatusRpt";
	public static final String CARD2VC_PAYMENTTYPEREQ = "PaymentTypeReq";
	public static final String CARD2VC_VENDOUTREQ = "VendoutReq";
	public static final String CARD2VC_TEXTSHOWREQ = "TextShowReq";
	public static final String CARD2VC_CARDINFORPT = "CardInfoRpt";//卡信息请求
	
	//======新协议==========//
	/** 错误编码：200-标识成功 */
	public static final int CODE_SUCCESS = 200;
	public static final String MSG_SUCCESS = "操作成功";

	/** 错误编码: 201-标识失败 */
	public static final int CODE_FAIL = 201;
	public static final String MSG_FAIL = "操作失败";
	
}
