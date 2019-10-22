package com.ubox.card.device.quickpass;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

import android.annotation.SuppressLint;

import com.alibaba.fastjson.JSON;
import com.onecomm.serialport.AnalyzeUtils;
import com.ubox.card.bean.db.QuickPassTradeObj.QuickPassTrade;
import com.ubox.card.bean.external.Card;
import com.ubox.card.bean.external.CostRep;
import com.ubox.card.bean.external.ExtResponse;
import com.ubox.card.business.CancelProcesser;
import com.ubox.card.business.Converter;
import com.ubox.card.business.DeviceWorkProxy;
import com.ubox.card.config.CardConst;
import com.ubox.card.config.DeviceConfig;
import com.ubox.card.core.WorkPool;
import com.ubox.card.db.dao.QuickPassTradeDao;
import com.ubox.card.device.Device;
import com.ubox.card.util.SysUtil;
import com.ubox.card.util.Utils;
import com.ubox.card.util.logger.Logger;

public class QUICKPASS extends Device {
	
	public static String SUCCESS  = "00"; // 成功
  
	public static String ERROR    = "01"; // 出错
  
	public static String LESS     = "02"; // 余额不足
  
	public static String TIME_OUT = "03"; // 超时
	public QUICKPASSWorker worker = new QUICKPASSWorker();;
	
	@Override
	public void init() {
		Timer timer = new Timer("QPUploadThread",false);
		long delay = 1000L*10;
		long period = 1000L* 60 * 3;
		timer.schedule(new QUICKPASSUpload(), delay, period);
//		DeviceWorkProxy.MAYBECANCEL = false;
		Logger.info("-----------------------------------work,QUICKPASS-init()>>>>>");
	}
	
	@Override
	public ExtResponse cardInfo(String json) {
		ExtResponse ext = Converter.cJSON2ExtRep(json);
		return ext;
	}

	@SuppressLint("DefaultLocale")
	@Override
	public ExtResponse cost(String json) {
		Logger.info("-----------------------------------json>>>>>"+json);
        ExtResponse ext = Converter.cJSON2ExtRep(json);
        CostRep     rep = Converter.costReq2costRep(Converter.cJSON2CostReq(json));
//        QUICKPASSWorker worker = new QUICKPASSWorker();
        try {
        	if(false == check()) { // 自检失败
        		ext.setResultCode(CardConst.EXT_INIT_FAIL);
        		ext.setResultMsg("Config.ini参数配置错误");
				Logger.info("-----------------------------------check>>>>>自检失败,Config.ini参数配置错误");
        		return Converter.genCostRepJSON(rep, ext);
        	}
        	
        	/* * * 取消处理 * * */
        	if(true == Utils.isCancel(ext.getSerialNo()+"")) {
				Logger.info("-----------------------------------isCancel>>>>>取消处理");
        		return CancelProcesser.cancelProcess(json); // 撤销交易
        	}
        	//打开串口
//        	worker.openSerialport();
        	long startTime = System.currentTimeMillis();
        	int timeout = 10;
        	String[] gb;
        	String result;
        	while(true){
        		//超时判断
        		long endTime = System.currentTimeMillis();
        		if(endTime-startTime>=timeout*1000){
        			ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
        			ext.setResultMsg("读卡超时");
					Logger.info("-----------------------------------读卡超时了>>>>>");
        			return Converter.genCostRepJSON(rep, ext);
        		}
				SysUtil.sleep(100);
        		//判断撤销
        		if(true == Utils.isCancel(ext.getSerialNo()+"")){
					Logger.info("-----------------------------------撤销交易了>>>>>");
        			return CancelProcesser.cancelProcess(json); // 撤销交易
        		}
        		
        		/** 查余额 **/
        		Logger.info(">>>> getBalance request:");
        		result = worker.getBalance(2);
        		Logger.info("<<<< getBalance response:"+result);
        		gb = AnalyzeUtils.analyzeGetBalance(result);
        		Logger.info("analyzeGetBalance:"+JSON.toJSONString(gb));

        		//无卡
        		if(SUCCESS.equals(gb[0])){
        			break;
        		}
        		
        		// 查询失败
        		if(ERROR.equals(gb[0])) {
        			ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
        			ext.setResultMsg("读卡失败");
        			return Converter.genCostRepJSON(rep, ext);
        		}
        		
        	}
        	// 查询成功
        	int   balance = Integer.parseInt(gb[3]);
        	String cardNo = gb[2];
        	
        	Card card = rep.getCards()[0];
        	card.setCardBalance(balance); // 卡余额
        	int money = rep.getProduct().getSalePrice();
        	
        	//撤销判断
        	if(true == Utils.isCancel(ext.getSerialNo()+"")){
        		return CancelProcesser.cancelProcess(json);
        	}
        	
        	SysUtil.sleep(1000);

        	//撤销判断
        	if(true == Utils.isCancel(ext.getSerialNo()+"")){
        		return CancelProcesser.cancelProcess(json);
        	}
        	
//        	/**
//        	 * 活动开始时间：5月22日－11月22日
//        	 * 活动-->活动规则：
//        	 * 活动1：持银联金融IC卡客户及绑定银行卡的NFC手机客户，通过闪付支付时，享受每瓶0.01元价格购买售价3元及以下商品。每日每台限20瓶，每张IC卡每日限参与活动一次，先到先得。
//        	 * 活动2：持银联金融IC卡客户及绑定银行卡的NFC手机客户，通过闪付方式支付时，享受以9折价格购买参与机器中所有商品。每张IC卡每日限享受一次优惠。
//        	 * ps：同一IC卡每日可同时参与上述专享优惠活动1和活动2各一次。
//        	 */
//        	QUICKPASSRule quickpassRule = new QUICKPASSRule();
//        	QUICKPASSRuleResponse response = quickpassRule.rule("", "", cardNo, money);
//			Logger.info(">>>>>> quickpassRule.rule : "+JSON.toJSONString(response));
//			money = response.getRuleMoney();
        	
        	/**
        	 * 活动开始时间：8月17日－11月17日
        	 * 活动-->活动规则：
        	 * 活动1：北京银行持卡人在参与机具上，通过闪付方式支付时，可以享受以每瓶饮品人民币1分的价格购买。活动期间，每月50台机具前优惠名额1万名，
        	 * 		3个月共计3万名。每月超出1名后的持卡人享受优惠活动2。每IC借记卡每日限参与一次，先到先得，用完即止。
        	 * 活动2：北京银行持卡人在参与机具上，通过闪付方式支付时，可以享受以9折价格购买参与机具中的所有商品。每IC借记卡每日限参与一次。 同一IC借记卡可同时参与上述优惠活动1、2。
        	 */
//        	QUICKPASSRuleTwo quickpassRuleTwo = new QUICKPASSRuleTwo();
//			if (cardNo.substring(0, 6).equals("623111")|| cardNo.substring(0, 6).equals("621468")
//				|| cardNo.substring(0, 6).equals("621420")) {// 判断卡号为北京银行卡
//				Logger.info("<<<< quickpassRuleTwo : " + cardNo);
//				money = quickpassRuleTwo.rule(cardNo,money);
//			}
        	
        	/**
        	 * 活动开始时间：2016年  3月1日-9月30日
        	 * 活动-->活动规则：
        	 * 活动1：每日每台不限制数量，每张IC卡每日每台限享受5次1分购任何饮料的机会，先到先得，用完为止。
        	 */
//        	QUICKPASSRuleThree RuleThree = new QUICKPASSRuleThree();
//        	Logger.info("<<<<<<<<<<<<<<<  money" + money);
//        	if(rep.getVendoutType().equals("drink") && money <= 300){
//        		Logger.info("<<<< quickpassRuleThree : " + cardNo);
//        		money = RuleThree.rule(cardNo,money);
//        	}
			if(balance < money) {
        		ext.setResultCode(CardConst.EXT_BALANCE_LESS);
        		ext.setResultMsg(CardConst.EXT_BALANCE_LESS_MSG);
        		return Converter.genCostRepJSON(rep, ext);
        	}
			
			/** 扣款 **/
        	Logger.info(">>>> icPay request:" + money);
        	result = worker.icPay(money,5);
        	Logger.info("<<<< icPay response:"+result);
        	String[] icpay = AnalyzeUtils.analyzeIcPay(result);
        	Logger.info("<<<< icPay analyzeIcPay:"+JSON.toJSONString(icpay));
        	
        	// 扣款失败
        	if(!SUCCESS.equals(icpay[0])) {
        		ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
        		
        		if(TIME_OUT.equals(icpay[0]))
        			ext.setResultMsg("读卡超时");
        		else if(LESS.equals(icpay[0]))
        			ext.setResultMsg("余额不足");
        		else
        			ext.setResultMsg(CardConst.EXT_CONSUEM_FAIL_MSG);
        		
        		return Converter.genCostRepJSON(rep, ext);
        	}
        	
//        	/*
//        	 * 北京2015活动
//        	 */
//			if(response.isRecord() == true){
//				quickpassRule.record(cardNo, response.getRuleType());
//			}
//           北京20150817-20151117
//        	if(QUICKPASSRuleTwo.isrecord){
//        		if(quickpassRuleTwo.record(cardNo)){
//        			QUICKPASSRuleTwo.isrecord = false;
//        		}
//			}
//        	if(QUICKPASSRuleThree.isrecord){
//        		if(RuleThree.record(cardNo)){
//        			QUICKPASSRuleThree.isrecord = false;
//        		}
//			}
			
        	String orderNo = DeviceWorkProxy.ORDER_NO;// card交易流水号

        	/** 扣款成功 **/
        	cardNo       = icpay[3];
        	String cardSerialNo = icpay[4];
        	/* fixbug:len 补足4为*/
        	String len = "0000"+(Integer.parseInt(icpay[5]) * 2);
        	String icData       = len.substring(len.length()-4,len.length()) + icpay[6].toUpperCase();
        	String tradeMoney = icpay[2];

        	// 记录上送参数
        	QuickPassTrade passTrade = new QuickPassTrade();
        	passTrade.setId(orderNo);
        	passTrade.setPCardNo(cardNo);
        	passTrade.setPCardSerNo(cardSerialNo);
        	passTrade.setPInputData(icData);
        	passTrade.setPSerialNo(orderNo);
        	passTrade.setPTradeMoney(tradeMoney);
        	passTrade.setUploadTime(DATE_FMT.format(new Date()));
        	
        	writeIcData(passTrade);
        	
        	card.setCardDesc(cardSerialNo); // 商户代码
        	card.setPosId(DeviceConfig.getInstance().getValue("TERMNO"));        // POS机终端号
        	card.setCardNo(cardNo);       // 卡号
        	card.setCardBalance(balance-Integer.parseInt(tradeMoney)); // 卡余额
        	rep.setThirdOrderNo(cardSerialNo);   // 交易流水号
        	rep.getProduct().setSalePrice(Integer.parseInt(tradeMoney)); // 实际扣款金额
        	rep.setOrderNo(orderNo);       // card交易流水号 
        	
            ext.setResultCode(CardConst.EXT_SUCCESS);
            ext.setResultMsg("扣款成功");
        } catch(Exception e) {
			Logger.error("Quickpass >>> cost exception "+e.getMessage(), e);
			e.printStackTrace();
			ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
			ext.setResultMsg("扣款失败");
        } finally{
//        	worker.closeSerialport();
        }
        
		return Converter.genCostRepJSON(rep, ext);
	}

	/**
	 * Config.ini配置参数检测
	 * @return true-监测成功,false-监测失败
	 */
	private boolean check() {
		if(DeviceConfig.getInstance().getValue("TERMNO") == null || DeviceConfig.getInstance().getValue("TERMNO").equals("")) {
			Logger.error("Quickpass >>> TERMNO is NULL");
			return false;
		}
		
		return true;
	}
	
    
    /**
	 * 记录macStr
     * @param fen			// 交易金额
     * @param cardNo		// 卡号
     * @param cardSerialNo	// 卡序列号
     * @param serNO			// 流水号
     * @param icData		// ic卡数据域
     */
	private void writeIcData(QuickPassTrade passTrade) {
		WorkPool.executeTask(new IcDataSecretary(passTrade));
	}
	
	
    @SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyyMMddHHmmss");
    
	/**
	 * 记录需要上送的消费数据
	 */
	private static class IcDataSecretary implements Runnable {
		
		private QuickPassTrade passTrade;
		
		IcDataSecretary(QuickPassTrade passTrade) {
			this.passTrade = passTrade;
		}
		
		@Override
		public void run() {
			//写文件
			try {
				QuickPassTradeDao.getInstance().insertOne(passTrade);
			} catch (Exception e) {
				Logger.error(e.getMessage(), e);
			}
		}
	}

}
