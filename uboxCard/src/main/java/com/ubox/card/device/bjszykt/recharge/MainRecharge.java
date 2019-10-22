package com.ubox.card.device.bjszykt.recharge;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;

import com.ubox.card.bean.db.QCQTradeObj.QCQTrade;
import com.ubox.card.config.CardJson;
import com.ubox.card.device.bjszykt.BJSZYKT;
import com.ubox.card.device.bjszykt.localwork.LocWorker;
import com.ubox.card.device.bjszykt.network.NetWorker;
import com.ubox.card.device.bjszykt.pubwork.PubWorker;
import com.ubox.card.device.bjszykt.pubwork.Result;
import com.ubox.card.device.bjszykt.server.bean.Card;
import com.ubox.card.device.bjszykt.server.bean.KCQTradeResponse;
import com.ubox.card.device.bjszykt.server.bean.Order;
import com.ubox.card.util.StrUtil;
import com.ubox.card.util.Utils;
//git@git.uboxol.com:8855/mp/UboxCard.git
import com.ubox.card.util.logger.Logger;

@SuppressLint("SimpleDateFormat")
public class MainRecharge {
	private recharge rech = recharge.getInstance();

	/**与服务器数据交互*/
	public KCQTradeResponse rechargeServer() {
		Logger.info("<<<<<<<<<<<<<　　Recharge is start. >>>>>>>>>>>");
		KCQTradeResponse bean = new KCQTradeResponse();
		
		try{
			/** 读卡 */
			int state = readCardInfo(bean);
			if(state == -1){
				return bean;
			}
			
//			if(PubWorker.batchNB == null){
//				Logger.info("sign in is false. ");
//				return getErroMsg(bean, "错误：设备没有签到");
//			}
			
			/** 预充值快充券查询申请/应答-----5141-5142*/
			Logger.info("<<<<<<<<<<<<<　　预充值快充券查询申请/应答-----5141-5142. >>>>>>>>>>>");
			Result result = NetWorker.rechargeSearch(bean);
			if(result.codeType != Result.CODESUCCESS) { //解析失败
				if(result.hdCode == null){
					return getErroMsg(bean, "错误：网络异常");
				}
				String err = BJSZYKT.error.get(result.hdCode.toLowerCase());
				if(err.equals("卡状态异常，需要锁卡")){
					Logger.info("<<<<<<<<<<<<<<<<< blackcard lockcard t. ");
					LocWorker.lockCard(RechargeInfo.cardNo);// 锁卡
				}
				return getErroMsg(bean, "错误："+ err);
			}
			/** 解析预充值快充券查询应答 */
			Logger.info("<<<<<<<<<<<<<　　解析预充值快充券查询应答 >>>>>>>>>>>");
			PubWorker.parseRechargeSearchReplay(result.fdBytes, bean);

			/** 充值申请信息 AA04 */
			Logger.info("<<<<<<<<<<<<<　　充值申请信息 AA04 >>>>>>>>>>>");
			if (applyInfo(bean, Integer.valueOf(RechargeInfo.CACHE_ORDERSAVEAMT)) == -1){
				return bean;
			}
			/** 快充券充值交易申请请求/应答-----5143-5144*/
			Logger.info("<<<<<<<<<<<<<　　快充券充值交易申请请求/应答-----5143-5144 >>>>>>>>>>>");
			Result result1 = NetWorker.rechargeApply(bean);
			if(result.codeType != Result.CODESUCCESS) { //解析失败
				return getErroMsg(bean, "错误：网络异常");
			}
			/** 解析快充券充值交易申请应答 */
			Logger.info("<<<<<<<<<<<<<　　解析快充券充值交易申请应答 >>>>>>>>>>>");
			PubWorker.parseRechargeApplyReplay(result1.fdBytes);

			/** AA05 */
			Logger.info("<<<<<<<<<<<<<　　充值申请信息反馈 AA05 >>>>>>>>>>>");
			int state1 = applyBackInfo(bean);
			if(state1 == -1){
				
				return bean;
			}
			/** AA06 */
			Logger.info("<<<<<<<<<<<<<　　充值申请信息确认 AA06 >>>>>>>>>>>");
			int state2 = rConfirmInfo(bean);
			if(state2 == -1){
				
				return bean;
			}
			
			/** 快充券充值交易确认请求/应答----- 5145-5146*/
			Logger.info("<<<<<<<<<<<<<　　快充券充值交易确认请求/应答----- 5145-5146 >>>>>>>>>>>");
			Result result2 = NetWorker.rechargeApplyConfirm(bean);
			if(result2.codeType != Result.CODESUCCESS) { //解析失败
				return getErroMsg(bean, "错误：网络异常");
			}
			/** 解析快充券充值交易确认应答 */
			Logger.info("<<<<<<<<<<<<<　　解析快充券充值交易确认应答 >>>>>>>>>>>");
			PubWorker.parseRechargeApplyConfirm(result2.fdBytes);
			/** AA07 */
			Logger.info("<<<<<<<<<<<<<　　充值申请信息确认反馈 AA07 >>>>>>>>>>>");
			int state3 = rConfirmBackInfo(bean);
			if(state3 == -1){
				return bean;
			}
			
//			String type = Integer.parseInt(recharge.cardtype.substring(0, 4), 16) + " "
//					+ Integer.parseInt(recharge.cardtype.substring(4, 8), 16) + " "
//					+ Integer.parseInt(recharge.cardtype.substring(8, 12), 16) + " "
//					+ Integer.parseInt(recharge.cardtype.substring(12, 16), 16) + " "
//					+ Integer.parseInt(recharge.cardtype.substring(16, 20), 16) + " "
//					+ Integer.parseInt(recharge.cardtype.substring(20, 24), 16) + " "
//					+ Integer.parseInt(recharge.cardtype.substring(24, 28), 16) + " "
//					+ Integer.parseInt(recharge.cardtype.substring(28, 32), 16);
//			String[] chars = type.split(" ");
//			String str = "";
//			for (int i = 0; i < chars.length; i++) {
//				str = str + (char) Integer.parseInt(chars[i]);
//			}
					
			byte[] cardtype = Utils.decodeHex(recharge.cardtype);
			String str = new String(cardtype,"gb2312").trim();
			Logger.info("<<<<<<<<<<<<<<<<<<< CardType : " + str + "CardMoney : "+recharge.newBalance);

			Card card = new Card();
			card.setCardMoney(recharge.newBalance);
			card.setCardType(str);
			card.setCardBalance(Integer.valueOf(recharge.afterMoney));
			card.setCardNo(RechargeInfo.cardNo);
			card.setValidDate(recharge.newDate);

			Order ord = new Order();
			ord.setOrderNo(RechargeInfo.CACHE_ORDERNO);
			ord.setOrderAmt(Integer.valueOf(RechargeInfo.CACHE_ORDERSAVEAMT));
			ord.setOrderDate(RechargeInfo.CACHE_ORDERDATE);
			ord.setOrderTime(RechargeInfo.CACHE_ORDERTIME);

			bean.setCode(200);
			bean.setOrders(ord);
			bean.setCards(card);
			bean.setMsg("充值完成");

			//入库操作
			QCQTrade trade = new QCQTrade();
			trade.setBrushSeq(StrUtil.genSeq());
			trade.setVmId(CardJson.vmId);
			trade.setClientTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			trade.setCardNo(RechargeInfo.cardNo);
			trade.setValidDate(recharge.newDate);
			trade.setCardBalance(recharge.afterMoney);
			trade.setOrderNo(RechargeInfo.CACHE_ORDERNO);
			trade.setOrderDate(RechargeInfo.CACHE_ORDERDATE);
			trade.setOrderTime(RechargeInfo.CACHE_ORDERTIME);
			trade.setOrderAmt(RechargeInfo.CACHE_ORDERSAVEAMT);
			trade.setCode(bean.getCode()+"");
			trade.setMsg(bean.getMsg());
			rech.PreservationData(trade);
		}catch(Exception e){
			Logger.error("Exception :"+ e.getMessage(),e);
			bean.setCode(500);
		}
		return bean;
	}

	/**
	 * 设置错误信息
	 * @param bean
	 * @param msg
	 */
	private KCQTradeResponse getErroMsg(KCQTradeResponse bean, String msg) {
		bean.setCode(500);
		bean.setMsg(msg);

		return bean;
	}

	public static KCQTradeResponse getBean() {
		
		return  new MainRecharge().rechargeServer();
	}

	/**
	 * 获取卡的信息
	 */
	private int readCardInfo(KCQTradeResponse bean) {
		try {
			// 读取卡信息
			String[] cardRp = rech.ReadCard();

			if (cardRp == null || cardRp[0] == null) {

				bean.setCode(500);
				bean.setMsg("读卡失败");

				return -1;
			}
			if(cardRp[0].contains("错误")){
				String error = cardRp[0];

				bean.setCode(500);
				bean.setMsg(error);

				return -1;
			}
			RechargeInfo.oprId = cardRp[4];
			RechargeInfo.posId = cardRp[5];
			RechargeInfo.sam = cardRp[6];
			RechargeInfo.unitId = cardRp[7];
			RechargeInfo.mchntId = cardRp[8];
			RechargeInfo.batchNO = cardRp[9];
			RechargeInfo.cardNo = cardRp[10];
			RechargeInfo.cardType = cardRp[11];
			RechargeInfo.cardPhyType = cardRp[12];
			RechargeInfo.befBal = cardRp[13];

		}catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}

		return 0;

	}

	/**
	 * 充值申请反馈信息
	 */
	private int applyInfo(KCQTradeResponse bean, int money) {

		try {

			String[] cardRp = rech.Apply(money);
			if (cardRp == null || cardRp[0] == null) {

				bean.setCode(500);
				bean.setMsg("读卡失败");

				return -1;
			}
			if(cardRp[0].contains("错误")){
				String error = cardRp[0];

				bean.setCode(500);
				bean.setMsg(error);

				return -1;
			}if (cardRp == null || cardRp[0] == null) {
				if(cardRp[0].contains("错误")){
					String error = cardRp[0];
					bean.setCode(500);
					bean.setMsg(error);

					return -1;
				}
				bean.setCode(500);
				bean.setMsg("空");

				return -1;
			}
			RechargeInfo.rechargeInfo = cardRp[0];
			RechargeInfo.rechargeInfoMac = cardRp[1];
			RechargeInfo.rechargeInfoMacBuf = cardRp[2];
			RechargeInfo.rechargeInfoPsamTransNo = cardRp[3];
		}catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}

		return 0;
	}

	/**
	 * 充值申请反馈信息
	 */
	private int applyBackInfo(KCQTradeResponse bean) {
		try{
			String[] cardRp = rech.ApplyBack();
			if (cardRp == null || cardRp[0] == null) {

				bean.setCode(500);
				bean.setMsg("读卡失败");

				return -1;
			}
			if(cardRp[0].contains("错误")){
				String error = cardRp[0];

				bean.setCode(500);
				bean.setMsg(error);

				return -1;
			}
			RechargeInfo.AnswerNB = cardRp[0];
			RechargeInfo.MasterNB = cardRp[1];
			RechargeInfo.MasterID = cardRp[2];
			RechargeInfo.Data = cardRp[3];
		}catch(Exception e){
			Logger.error("Exception :"+ e.getMessage());
		}
		return 0;
	}

	/**
	 * 充值确认信息
	 */
	private int rConfirmInfo(KCQTradeResponse bean) {
		try{
			String[] cardRp = rech.RConfirm();
			if (cardRp == null || cardRp[0] == null ||"".equals(cardRp[0])) {

				bean.setCode(500);
				bean.setMsg("读卡失败");

				return -1;
			}
			if(cardRp[0].contains("错误")){
				String error = cardRp[0];

				bean.setCode(500);
				bean.setMsg(error);

				return -1;
			}
			RechargeInfo.AnsNb = cardRp[0];
			RechargeInfo.Type = cardRp[1];
			RechargeInfo.MasId = cardRp[2];

		}catch(Exception e){
			Logger.error("Exception :"+ e.getMessage());
		}
		return 0;
	}

	/**
	 * 充值确认反馈信息
	 */
	private int rConfirmBackInfo(KCQTradeResponse bean) {
		try{
			String[] cardRp = rech.RConfirmBack();
			if (cardRp == null || cardRp[0] == null) {

				bean.setCode(500);
				bean.setMsg("读卡失败");

				return -1;
			}
			if(cardRp[0].contains("错误")){
				String error = cardRp[0];

				bean.setCode(500);
				bean.setMsg(error);

				return -1;
			}
			RechargeInfo.AnsNB = cardRp[0];
			RechargeInfo.MAC = cardRp[1];
			RechargeInfo.Ciphertext = cardRp[2];
		}catch(Exception e){
			Logger.error("Exception :"+ e.getMessage(),e);
		}
		return 0;
	}
}
