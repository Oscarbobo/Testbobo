package com.ubox.card.config;

import java.io.File;

import android.annotation.SuppressLint;

@SuppressLint("SdCardPath")
public class CardConst {
	
	/* 警告: 路径尾不能出现'/'*/
    public static final String VCARD_PATH = "/mnt/sdcard/Ubox/dbFile/Card";
	
	public static final String DEVICE_WORK_PATH = VCARD_PATH + File.separator + CardJson.cardName;
	
	/* 消息编码: 200-标识成功 */
	public static final int SUCCESS_CODE = 200;

	/* 协议关键字 */
	public static final String COST_REQ = "costReq";       // 扣款请求
	public static final String COST_REP = "costRep";       // 扣款响应
	public static final String CARD_REQ = "cardReq";       // 卡信息请求
	public static final String CARD_REP = "cardRep";       // 卡信息响应
	public static final String TXT_SHOW = "txtShow";       // 文本信息
	public static final String CANCEL_CARD = "cancelCard"; // 撤销交易请求 
	
	/* cvs通信接口错误码 */
	public static final int EXT_SUCCESS        = 200;
	public static final int EXT_BALANCE_LESS   = 325;  // 余额不足
	public static final int EXT_INIT_FAIL      = 333;  // 设备初始化失败
	public static final int EXT_READ_CARD_FAIL = 400;  // 读卡失败
	public static final int EXT_CONSUEM_FAIL   = 420;  // 扣款失败
	
	/* cvs通信接口错误信息 */
	public static final String EXT_SUCCESS_MSG        = "刷卡成功";
	public static final String EXT_BALANCE_LESS_MSG   = "余额不足";
	public static final String EXT_INIT_FAIL_MSG      = "设备初始化失败";
	public static final String EXT_READ_CARD_FAIL_MSG = "读卡失败";
	public static final String EXT_CONSUEM_FAIL_MSG   = "扣款失败";
}
