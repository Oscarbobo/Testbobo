package com.ubox.card.device.bjszykt.recharge;

import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;

import com.ubox.card.bean.db.QCQTradeObj.QCQTrade;
import com.ubox.card.config.CardJson;
import com.ubox.card.db.dao.QCQDao;
import com.ubox.card.device.bjszykt.BJSZYKT;
import com.ubox.card.device.bjszykt.localwork.LocWorker;
import com.ubox.card.device.bjszykt.network.paramdownload.OperationWorker;
import com.ubox.card.device.bjszykt.pubwork.LocalContext;
import com.ubox.card.device.bjszykt.pubwork.PubUtils;
import com.ubox.card.device.bjszykt.pubwork.PubWorker;
import com.ubox.card.device.bjszykt.pubwork.Result;
import com.ubox.card.util.Utils;
import com.ubox.card.util.logger.Logger;

@SuppressLint("DefaultLocale")
public class recharge {

	private static recharge charge = new recharge();

	private recharge() {
	}

	public static recharge getInstance() {
		return charge;
	}
	
	private static String[] cardInfo;//		卡信息
	private static String[] cardRp; //		            响应卡信息
	private static String[] applyInfo;//		联机充值申请信息
	private static String[] applyBackInfo;//	联机充值申请反馈信息
	private static String[] applyconfirmInfo;//		联机充值确认信息
	private static String[] applyconfirmBackInfo;//	联机充值确认反馈信息
	
//	private static String[] delayInfo = new String[3];//	延期申请信息
//	private static String[] delayBackInfo = new String[3];//	延期申请反馈反馈信息
//	private static String[] delayconfirmInfo = new String[3];//	延期确认信息
//	private static String[] delayconfirmBackInfo = new String[3];//	延期确认反馈信息
	private static String Money; //充值金额
	private static String maxBalance;//最大卡内余额
	private static String balan;//最大卡内余额
	public static String newDate;//新有效日期
	public static String CSN;
	public static String Trading;
	public static String afterMoney;//充值后余额
	private static int nowDate;
	public static int newBalance;//充值前余额
	public static String cardtype;//卡类型
	
	/**
	 * 读卡查询响应信息，根据卡号查询快充券信息
	 * 
	 * @return vmid  如果返回错误：“错误：错误信息。”
	 * @return 流水号
	 * @return 创建时间
	 * @return 发送时间-到秒
	 * @return LocalContext.oprId 操作员ID
	 * @return POS机号
	 * @return SAM卡号
	 * @return 单位代码
	 * @return 商户代码
	 * @return 批次号
	 * @return 卡号
	 * @return 卡类型
	 * @return 卡物理类型
	 * @return 交易前卡内余额
	 */
	@SuppressLint("SimpleDateFormat")
	public String[] ReadCard() {
		cardRp = new String[14];
		Logger.info("<<<<   read card id is start. ");
		try {
			/** 判断市政一卡通的工作状态 **/
			if (LocalContext.VCARD_STATUS != LocalContext.VCRAD_READY) { // 正在初始化
				Logger.info("ykt is initing. ");
				cardRp[0] = "错误:设备正在初始化";
				return cardRp;
			}

			/** 寻卡 **/
			long start = System.currentTimeMillis();
			long findTime = 10000L;
			while (true) {
				if (System.currentTimeMillis() - start > findTime) {
					Logger.info("serch card time out. ");
					cardRp[0] = "错误:寻卡超时";
					return cardRp; // 寻卡超时,返回
				}
				int codeType = LocWorker.commandWork("AA00").codeType;
				if (codeType == 0) {
					Logger.info("serch card success. ");
					break;
				} else if (codeType == Result.HARDWARE) {
					Logger.info("serch card failse. ");
					cardRp[0] = "错误:寻卡失败";
					return cardRp;
				}
			}

			/** 读取卡信息 */
			cardInfo = LocWorker.readCard1();
			if (cardInfo == null || !cardInfo[0].equals("AA00")) { // 读卡失败
				Logger.info("read card failse. response code is : " + cardInfo[0].toLowerCase());
				String err = BJSZYKT.error2.get("AA"+(cardInfo[0].substring(2,4).toLowerCase()));
				cardRp[0] = "错误:"+ err;
				return cardRp;
			}
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
			String Time=formatter.format(new java.util.Date());
			nowDate = Integer.parseInt(Time);
			
			if(Integer.parseInt(cardInfo[6]) > nowDate){//新的有效期小于当前日期
				Logger.info("<<<<<<<<<<< fa xing ri qi cuo wu. " +cardInfo[6]);
				cardRp[0] = "错误:未到启用日期";
				return cardRp;
			}
			
			if(PubWorker.batchNB == null){
				Logger.info("sign in is false. ");
				
				cardRp[0] = "错误：设备没有签到";
				return cardRp;
			}
			
			/** 卡信息验证,黑名单和可消费卡类型 */
			int verifyCode = LocWorker.verifiCardInfo(cardInfo);
			if (verifyCode != LocWorker.SUCCESS) { // 验卡失败
				if (verifyCode == LocWorker.IN_BLACK_LIST) {
					Logger.info("<<<<<<<<<<<<<<<<< blackcard lockcard s. ");
					LocWorker.lockCard(cardInfo[2]);// 锁卡
					cardRp[0] = "错误:黑名单卡";
					return cardRp;
				}
				if (verifyCode == LocWorker.NO_CONSUME_CARD) {
					
					cardRp[0] = "错误:不是可消费卡";
					return cardRp;
				}
				if (verifyCode == LocWorker.IN_GREY_LIST) {
					
					cardRp[0] = "错误:灰名单卡";
					return cardRp;
				}
			}
			
			/**检验卡属性参数*/
			int attr = cardAttr();
			if(attr == -1){//无卡属性参数
				Logger.info("no card attr. ");
				cardRp[0] = "错误:卡属性参数错误";
				return cardRp;
			}
			String cardAtt = LocalContext.CARD_ATTR_LIST.get(attr);//得到此卡的卡片属性参数
			Logger.info("---------------  cardAtt is : "+ cardAtt);
			String att1 = cardAtt.substring(36, 38);
			cardtype = cardAtt.substring(4, 36);
			String att = Utils.hexString2binaryString(att1);
			Logger.info("---------------  att is : "+ att);
			int ATT = Integer.valueOf(att.substring(1, 2));
			if (ATT != 1) {//判断卡属性第1个字节的第2位是否是1，是1则允许充值，是0，则不允许充值
				Logger.info("attr is not 1, is:: "+ cardAtt.substring(21, 22));
				cardRp[0] = "错误:此卡不允许充值";
				return cardRp;
			}
			
			/**检查充值卡类型参数*/
			int type = cardType();
			if(type == -1){//无匹配充值卡参数
				Logger.info("no card type. ");
				cardRp[0] = "错误:充值卡类型错误";
				return cardRp;
			}
			String cardTyp = LocalContext.SOR_CARD_TYPE.get(type);//得到此卡的充值类型参数
			if(cardTyp.substring(60, 68).equals("00000000")){//新的有效期
				newDate = cardInfo[7];
				Logger.info("new date is :: "+ newDate);
			} else {
				
				byte[] date = Utils.decodeHex(cardTyp.substring(60, 68));
				byte[] date1 = { date[3], date[2], date[1], date[0] };
				String date2 = Utils.toHex(date1);
				int date3 = Integer.parseInt(date2, 16);
				newDate = getDate(date3);
				Logger.info("adddate is : "+ date3+ ". new date is : " + newDate);
			}
			
			if(Integer.parseInt(newDate) < nowDate){//新的有效期小于当前日期
				Logger.info("the new date is error. ");
				cardRp[0] = "错误:有效期错误";
				return cardRp;
			}
			maxBalance = cardTyp.substring(52, 60);
			balan = maxBalance.substring(6,8) + maxBalance.substring(4,6) + maxBalance.substring(2,4) + maxBalance.substring(0,2);
			Logger.info("zui da ka nei yu e : " + balan);
			cardRp[0] = CardJson.vmId;
			cardRp[1] = PubUtils.getFromConfigs("POS_IC_SEQ");
			cardRp[2] = PubUtils.generateSysTime().substring(0, 8);
			cardRp[3] = PubUtils.generateSysTime();
			cardRp[4] = LocalContext.oprId;
			cardRp[5] = LocalContext.posId;
			cardRp[6] = LocalContext.CACHE_SAM;
			cardRp[7] = LocalContext.unitId;
			cardRp[8] = PubUtils.BA2HS(OperationWorker.mchnitid);
			cardRp[9] = PubUtils.getFromConfigs("batchNO");
//			cardRp[9] = PubUtils.getFromConfigs("batchNO");
			cardRp[10] = cardInfo[2];
			cardRp[11] = cardInfo[3];
			cardRp[12] = cardInfo[4];
			cardRp[13] = cardInfo[8];
			Trading	   = cardInfo[11];
			Logger.info("Log....vmId : " 		+ cardRp[0] + "\n" +
						"Log....POS_IC_SEQ : " 	+ cardRp[1] + "\n" +
						"Log....date : " 		+ cardRp[2] + "\n" +
						"Log....time : " 		+ cardRp[3] + "\n" +
						"Log....oprId : " 		+ cardRp[4] + "\n" +
						"Log....posId : " 		+ cardRp[5] + "\n" +
						"Log....sam : " 		+ cardRp[6] + "\n" +
						"Log....unitId : " 		+ cardRp[7] + "\n" +
						"Log....mchnitid : " 	+ cardRp[8] + "\n" +
						"Log....batchNB : " 	+ cardRp[9] + "\n" +
						"Log....cardID : " 		+ cardRp[10] + "\n" +
						"Log....cardType : " 	+ cardRp[11] + "\n" +
						"Log....cardFType : " 	+ cardRp[12] + "\n" +
						"Log....money : " 		+ cardRp[13] + "\n" +
						"Log....lastData : " 	+ Trading + "\n");
			
		} catch (Exception e) {
			Logger.error("read exception: " + e.getMessage());
			cardRp[0] = "错误:设备异常错误";
			return cardRp;
		}
		return cardRp;
	}
	
	private int cardAttr() {
		try {
			int size = LocalContext.CARD_ATTR_LIST.size();
			for (int i = 0; i <= size; i++) {
				String str = LocalContext.CARD_ATTR_LIST.get(i);
				String str1 = str.substring(0, 4);
				if (str1.equals(cardInfo[4] + cardInfo[3])) {
					Logger.info("cardInfo[4] + cardInfo[3]) ::: " + cardInfo[4]
							+ " . " + cardInfo[3]);
					return i;
				}
			}
		} catch (Exception e) {
			Logger.error("Exception : " + e.getMessage());
		}
		return -1;
	}

	private int cardType() {
		try {
			int size = LocalContext.SOR_CARD_TYPE.size();
			for (int i = 0; i <= size; i++) {
				String str = LocalContext.SOR_CARD_TYPE.get(i);
				String str1 = str.substring(0, 4);
				if (str1.equals(cardInfo[4] + cardInfo[3])) {
					return i;
				}
			}
		} catch (Exception e) {
			Logger.error("Exception : " + e.getMessage());
		}
		return -1;
	}
	
	/**
	 * 某月某日+n天=几月几日
	 */
	private String getDate(int days) {
		String date = PubUtils.generateSysTime();
		int year = Integer.parseInt(date.substring(0, 4));
		int month = Integer.parseInt(date.substring(4, 6));
		int day = Integer.parseInt(date.substring(6, 8));
		int leapyear = 0;
		int daytime = days;
		int sum = daytime + day;
		int month_date[] = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
		String Mo = null;
		String De = null;
		try {
			do {
				if (month == 2) {
					month_date[month - 1] += leapyear;

				}
				if (sum > month_date[month - 1]) {
					sum -= month_date[month - 1];
					month++;
					if (month == 13) {
						year++;
						if (year % 400 == 0
								|| (year % 100 != 0 && year % 4 == 0)) {
							leapyear = 1;
						} else {
							leapyear = 0;
						}
						month = 1;
					}
				}
			} while (sum > month_date[month - 1]);
			day = sum;
			Mo = month + "";
			De = day + "";
			if (Mo.length() == 1) {
				Mo = "0" + month;
			}
			if (De.length() == 1) {
				De = "0" + day;
			}
			Logger.info("the new date is ::" + year + Mo + De);
		} catch (Exception e) {
			Logger.error("getDate Exception. " + e.getMessage());
		}
		return year + Mo + De;
	}

	/**
	 * 联机充值申请
	 * @param 指令代码：AA04
	 * @param 交易类型：0X30 
	 * @param CSN  ：cardInfo[1]
	 * 
	 * @return 充值申请结果
	 */
	public String[] Apply(int money) {

		applyInfo = new String[4];
		try {
			String cmd = "AA04";
			String type = "5B";
			CSN = cardInfo[1];
			Money = PubUtils.BA2HS(PubUtils.i2bLt(money, 4));
			newBalance = PubUtils.b2iLt(PubUtils.HS2BA(cardInfo[8]), 4);
//			String Balance =cardInfo[8].substring(6, 8) + cardInfo[8].substring(4, 6) + cardInfo[8].substring(2, 4) +cardInfo[8].substring(0, 2);
//			newBalance = Integer.parseInt(Balance, 16);
			afterMoney = money + newBalance +"";
			Logger.info("Log....cmd : " 	+ cmd 	+ "\n" +
						"Log....type : " 	+ type 	+ "\n" +
						"Log....csn : " 	+ CSN 	+ "\n" +
						"Log....Money : " 	+ Money + "\n"+
						"Log....yu e : " 	+ newBalance + "\n");

			if(money + newBalance < 0){//充值金额是否合法
				Logger.info("the money is error. ");
				applyInfo[0] = "错误:充值金额不合法";
				return applyInfo;
			}
			
			if(money + newBalance >= Integer.parseInt(balan, 16)){//充值后余额不能超过xxx
				Logger.info("the money is error. " + afterMoney);
				String s = Integer.parseInt(balan, 16)+"";
				String s2 = s.substring(0, s.length()-2) + "." + s.substring(s.length()-2, s.length());
				applyInfo[0] = "错误:充值后金额不能超过"+ s2 + "元";
				return applyInfo;
			}
			
			Result apply = LocWorker.commandWork(cmd + type + CSN + Money);
			if (apply.codeType != 0) {
				Logger.info("<<<<<<<<<<<<  Application recharge is failse AA04. ");
				applyInfo[0] = "错误:"+BJSZYKT.error2.get("AA"+(apply.hdCode.substring(2,4).toLowerCase()));
				return applyInfo;
			}
			applyInfo[0] = new String(apply.fdBytes,   5,   4);
			applyInfo[1] = new String(apply.fdBytes,   9,   8);//MAC
			applyInfo[2] = new String(apply.fdBytes,   17,   96);//加密密文
			applyInfo[3] = new String(apply.fdBytes,   113,   4);//钱包交易序列号
			Logger.info("Log....return mac : " + applyInfo[1] + "\n" +
						"Log....return miwen : " + applyInfo[2]  + "\n" +
						"Log....return pastnb : " + applyInfo[3]  + "\n");
			
		} catch (Exception e) {
			Logger.error("Apply exception: " + e.getMessage());
		}
		return applyInfo;
	}
	
	/**
	 * 联机充值申请反馈
	 * @param 指令代码：		cmd
	 * @param IC交易流水号：	icNB
	 * @param 操作员ID：		userID
	 * @param 代理商编码：		agentNB
	 * @param 卡有效期：		rightDate
	 * @param MAC：			MAC
	 * @param 加密密文：		Ciphertext
	 * 
	 * @return 充值申请反馈结果
	 */
	public String[] ApplyBack() {
		applyBackInfo = new String[4];

		try {
			String cmd = "AA05";
			String icNB = PubUtils.getFromConfigs("POS_IC_SEQ");
			String userID = LocalContext.oprId;
			String agentNB = PubUtils.BA2HS(new byte[]{(byte) OperationWorker.agentShortCode});
			String rightDate = newDate;
			String MAC = RechargeInfo.CACHE_CIPHER_DATA_MAC.toUpperCase();
			String Ciphertext = RechargeInfo.CACHE_MACBUF.toUpperCase();

			Logger.info("cmd = " + cmd + "\n" +
							"Log....cmd 		= " + cmd + "\n" +
							"Log....POS_IC_SEQ = " + icNB + "\n" +
							"Log....userID 	= " + userID + "\n" +
							"Log....agentNB 	= " + agentNB + "\n" +
							"Log....rightDate 	= " + rightDate + "\n" +
							"Log....MAC 		= " + MAC + "\n" +
							"Log....Ciphertext = " + Ciphertext + "\n"
			);
			Result apply = LocWorker.commandWork(cmd + icNB + userID + agentNB + rightDate + MAC + Ciphertext);
			if (apply.codeType != 0) {
				Logger.info("<<<<<<<<<<<<<<< Application recharge back is failse AA05. ");
				applyBackInfo[0] = "错误:"+BJSZYKT.error2.get("AA"+(apply.hdCode.substring(2,4).toLowerCase()));
				return applyBackInfo;
			}
			applyBackInfo[0] = new String(apply.fdBytes,   5,   4);
			applyBackInfo[1] = new String(apply.fdBytes,   9,   2);//主机应答码
			applyBackInfo[2] = new String(apply.fdBytes,   11,   8);//主机流水号
			applyBackInfo[3] = new String(apply.fdBytes,   19,  110);//充值数据
			Logger.info("Log....return yingdama : " + applyBackInfo[1] + "\n" +
						"Log....return liushuihao : " + applyBackInfo[2] + "\n" +
						"Log....return shuju : " + applyBackInfo[3] + "\n" );
			if (!applyBackInfo[1].equals("00")) {
				Logger.info("zhu ji ying da ma cuo wu. :" + applyBackInfo[1]);
				applyBackInfo[0] = "错误:"+ BJSZYKT.error.get(applyBackInfo[1].toLowerCase());
				applyBackInfo[1] = "";
				applyBackInfo[2] = "";
				applyBackInfo[3] = "";
				return applyBackInfo;
			}
		} catch (Exception e) {
			Logger.error("ApplyBack exception: " + e.getMessage());
		}
		return applyBackInfo;
	}
	
	/**
	 * 联机充值确认
	 * @param 指令代码：		cmd:AA06
	 * @param 交易类型：		type:0x30
	 * @param 交易应答码：		answerNB
	 * @param 交易日期：		date
	 * @param 交易时间：		time
	 * @param 卡号：			cardID
	 * @param 交易金额：		
	 * @param 主机交易流水号：	hostNB
	 * @param 终端IC流水号：	icNB
	 * 
	 * @return 充值申请确认结果
	 */
	public String[] RConfirm() {
		applyconfirmInfo = new String[3];

		try {
			String cmd = "AA06";
			String type = applyBackInfo[3].substring(0,2).toUpperCase();
			String answerNB = applyBackInfo[1];
			String date = applyBackInfo[3].substring(38,46);
			String time = applyBackInfo[3].substring(46,52);
			String cardID = cardInfo[2];
			String money = applyBackInfo[3].substring(14,22).toUpperCase();
			String hostNB = applyBackInfo[2];
			String icNB = applyBackInfo[3].substring(22,30);
			Logger.info("Log....cmd : " + cmd + "\n" +
						"Log....type : " + type + "\n" +
						"Log....answerNB : " + answerNB + "\n" +
						"Log....date : " + date + "\n" +
						"Log....time : " + time + "\n" +
						"Log....cardID : " + cardID + "\n" +
						"Log....money : " + money + "\n" +
						"Log....hostNB : " + hostNB + "\n" +
						"Log....icNB : " + icNB + "\n");
			Result apply = LocWorker.commandWork(cmd + type + answerNB + date + time + cardID + money + hostNB + icNB);
			if (apply.codeType != 0) {
				Logger.info("<<<<<<<<<<<<<<   Application confirm is failse AA06. ");
				applyconfirmInfo[0] = "错误:"+BJSZYKT.error2.get("AA"+(apply.hdCode.substring(2,4).toLowerCase()));
				return applyconfirmInfo;
			}
			
			applyconfirmInfo[0] = new String(apply.fdBytes,   5,   4);
			applyconfirmInfo[1] = new String(apply.fdBytes,   9,   8);//MAC
			applyconfirmInfo[2] = new String(apply.fdBytes,   17,  64);//加密密文
			RechargeInfo.CACHE_CIPHER_DATA_MAC = applyconfirmInfo[1];//密文MAC
			RechargeInfo.CACHE_MACBUF = applyconfirmInfo[2];//加密密文
			Logger.info("Log....return MAC : " + applyconfirmInfo[1] + "\n" +
						"Log....return miwen : " + applyconfirmInfo[2] );
		} catch (Exception e) {
			Logger.error("Confirm exception: " + e.getMessage());
		}
		return applyconfirmInfo;
	}
	
	/**
	 * 联机充值确认反馈
	 * @param 指令代码：		cmd:AA07
	 * @param MAC：			MAC
	 * @param 加密密文：		Ciphertext
	 * 
	 * @return 充值确认反馈结果
	 */
	@SuppressLint("DefaultLocale")
	public String[] RConfirmBack() {
		applyconfirmBackInfo = new String[3];
		String cmd = "AA07";
		String MAC = RechargeInfo.CONFIRM_MAC.toUpperCase();
		String Ciphertext = RechargeInfo.CONFIRM_MACBUF.toUpperCase();
		Logger.info("Log....cmd : " + cmd + "\n" +
					"Log....MAC : " + MAC + "\n" +
					"Log....Ciphertext : " + Ciphertext + "\n");
		try {
			Result apply = LocWorker.commandWork(cmd + MAC + Ciphertext);
			if (apply.codeType != 0) {
				Logger.info("<<<<<<<<<<<<  Application confirm back is failse AA07. ");
				applyconfirmBackInfo[0] = "错误:"+BJSZYKT.error2.get("AA"+(apply.hdCode.substring(2,4).toLowerCase()));
				return applyconfirmBackInfo;
			}
			applyconfirmBackInfo[0] = new String(apply.fdBytes,   5,   4);
			applyconfirmBackInfo[1] = new String(apply.fdBytes,   9,   2);//交易类型
			applyconfirmBackInfo[2] = new String(apply.fdBytes,   11,  2);//主机应答码
			Logger.info("Log....return jiaoyilx : " + applyconfirmBackInfo[1] + "\n" +
						"Log....return zhujiydm : " + applyconfirmBackInfo[2] );
			if (!applyconfirmBackInfo[2].equals("00")) {
				Logger.info("Application confirm back is failse. ");
				applyconfirmBackInfo[0] = "错误:"+ BJSZYKT.error.get(applyconfirmBackInfo[2].toLowerCase());
				applyconfirmBackInfo[1] = "";
				applyconfirmBackInfo[2] = "";
				return applyconfirmBackInfo;
			}
		} catch (Exception e) {
			Logger.error("ConfirmBack exception: " + e.getMessage());
		}
		return applyconfirmBackInfo;
	}
	
	/**
	 * 卡片延期申请
	 * @param 指令代码：		cmd:AA4C
	 * @param 交易类型：		21
	 * @param 交易状态：		00
	 * @param CSN：			cardInfo[1]
	 * @param 卡有效期：		cardInfo[7]
	 * 
	 * @return 卡片延期申请结果
	 */
//	private String[] Delay() {
//		delayInfo = null;
//		Logger.info("<<<<   Application delay is start. ");
//		String cmd = "AA4C";
//		String type = "21";
//		String state = "00";
//		String CSN = cardInfo[1];
//		String rightDate = newDate;
//		try {
//			Result apply = LocWorker.commandWork(cmd + type + state + CSN + rightDate);
//			if (apply.codeType != 0) {
//				Logger.info("Application delay is failse. ");
//				return delayInfo;
//			}
//			delayInfo[0] = new String(apply.fdBytes,   5,   4);
//			delayInfo[1] = new String(apply.fdBytes,   9,   8);//MAC
//			delayInfo[2] = new String(apply.fdBytes,   11,  96);//加密密文
//			
//		} catch (Exception e) {
//			Logger.error("Delay exception: " + e.getMessage());
//		}
//		return delayInfo;
//	}
//	/**
//	 * 卡片延期申请反馈
//	 * @param 指令代码：		cmd:AA4D
//	 * @param MAC：			MAC
//	 * @param 加密密文：		Ciphertext
//	 * 
//	 * @return 卡片延期申请反馈结果
//	 */
//	private String[] DelayBack() {
//		delayBackInfo = null;
//		Logger.info("<<<<   Application delay back is start. ");
//		String cmd = "AA4D";
//		String MAC = delayInfo[1];
//		String Ciphertext = delayInfo[2];
//		try {
//			Result apply = LocWorker.commandWork(cmd + MAC + Ciphertext);
//			if (apply.codeType != 0) {
//				Logger.info("Application delay back is failse. ");
//				return delayBackInfo;
//			}
//			delayBackInfo[0] = new String(apply.fdBytes,   5,   4);
//			delayBackInfo[1] = new String(apply.fdBytes,   9,   2);//主机应答码
//			delayBackInfo[2] = new String(apply.fdBytes,   11,  2);//交易类型
//			delayBackInfo[3] = new String(apply.fdBytes,   13,  2);//交易状态
//			delayBackInfo[4] = new String(apply.fdBytes,   15,  8);//交易日期
//			delayBackInfo[5] = new String(apply.fdBytes,   23,  6);//交易时间
//			delayBackInfo[6] = new String(apply.fdBytes,   29,  8);//延期前有效期
//			delayBackInfo[7] = new String(apply.fdBytes,   37,  8);//延期后有效期
//			delayBackInfo[8] = new String(apply.fdBytes,   45,  8);//主机流水号
//			
//		} catch (Exception e) {
//			Logger.error("DelayBack exception: " + e.getMessage());
//		}
//		return delayBackInfo;
//	}
//	
//	/**
//	 * 卡片延期确认
//	 * @param 指令代码：		cmd:AA4E
//	 * @param 交易类型：		delayBackInfo[2]
//	 * @param 交易状态：		delayBackInfo[3]
//	 * @param 交易应答码：		delayBackInfo[1]
//	 * @param 交易日期：		delayBackInfo[4]
//	 * @param 交易时间：		delayBackInfo[5]
//	 * @param 卡号：			cardInfo[2]
//	 * @param 延期前卡有效期：	delayBackInfo[6]
//	 * @param 卡有效期：		cardInfo[7]
//	 * @param 主机交易流水号：	delayBackInfo[8]
//	 * 
//	 * @return 卡片延期确认结果
//	 */
//	private String[] DConfirm() {
//		delayconfirmInfo = null;
//		Logger.info("<<<<   Application delay confirm is start. ");
//		String cmd = "AA4E";
//		String type = delayBackInfo[2];
//		String state = delayBackInfo[3];
//		String answerNB = delayBackInfo[1];
//		String date = delayBackInfo[4];
//		String time = delayBackInfo[5];
//		String cardID = cardInfo[2];
//		String BrightDate = delayBackInfo[6];
//		String rightDate = newDate;
//		String hostNB = delayBackInfo[8];
//		try {
//			Result apply = LocWorker.commandWork(cmd + type + state + answerNB + date + time + cardID + BrightDate + rightDate + hostNB);
//			if (apply.codeType != 0) {
//				Logger.info("Application delay confirm is failse. ");
//				return delayconfirmInfo;
//			}
//			delayconfirmInfo[0] = new String(apply.fdBytes,   5,   4);
//			delayconfirmInfo[1] = new String(apply.fdBytes,   9,   8);//MAC
//			delayconfirmInfo[2] = new String(apply.fdBytes,   17,  64);//加密密文
//			
//		} catch (Exception e) {
//			Logger.error("DelayBack exception: " + e.getMessage());
//		}
//		return delayconfirmInfo;
//	}
//	
//	/**
//	 * 卡片延期确认反馈
//	 * @param 指令代码：		cmd:AA4F
//	 * @param MAC：			delayconfirmInfo[1]
//	 * @param 加密密文：		delayconfirmInfo[2]
//	 * 
//	 * @return 卡片延期确认反馈结果
//	 */
//	private String[] DConfirmBack() {
//		delayconfirmBackInfo = null;
//		Logger.info("<<<<   Application delay confirm back is start. ");
//		String cmd = "AA4E";
//		String MAC = delayconfirmInfo[1];
//		String Ciphertext = delayconfirmInfo[2];
//		try {
//			Result apply = LocWorker.commandWork(cmd + MAC + Ciphertext);
//			if (apply.codeType != 0) {
//				Logger.info("Application delay confirm back is failse. ");
//				return delayconfirmBackInfo;
//			}
//			delayconfirmBackInfo[0] = new String(apply.fdBytes,   5,   4);
//			delayconfirmBackInfo[1] = new String(apply.fdBytes,   9,   2);//交易类型
//			delayconfirmBackInfo[2] = new String(apply.fdBytes,   11,  2);//主机应答码	
//			
//		} catch (Exception e) {
//			Logger.error("DelayBack exception: " + e.getMessage());
//		}
//		return delayconfirmBackInfo;
//	}
	
	/**数据入库操作*/
	public void PreservationData(final QCQTrade bjszykt) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Logger.info("QCQTradeHandler Runnable start....");
				QCQDao.getInstance().insertOne(bjszykt);// 入库操作
			}
		}).start();
		
	}
}
