package com.ubox.card.device.quickpass.rule;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import android.annotation.SuppressLint;
import com.alibaba.fastjson.JSON;
import com.ubox.card.config.CardConst;
import com.ubox.card.util.FileUtil;
import com.ubox.card.util.logger.Logger;

/**
 * 活动开始时间：5月22日－7月22日
 * 活动-->活动规则：
 * 活动1：持银联金融IC卡客户及绑定银行卡的NFC手机客户，通过闪付支付时，享受每瓶0.01元价格购买售价3元及以下商品。每日每台限20瓶，每张IC卡每日限参与活动一次，先到先得。
 * 活动2：持银联金融IC卡客户及绑定银行卡的NFC手机客户，通过闪付方式支付时，享受以9折价格购买参与机器中所有商品。每张IC卡每日限享受一次优惠。
 * ps：同一IC卡每日可同时参与上述专享优惠活动1和活动2各一次。
 * 
 * @author weipeipei
 * 
 */
public class QUICKPASSRule {
	private static String path = CardConst.DEVICE_WORK_PATH+File.separator;
	
	/**
	 * 营销活动规则方法，通过此方法传入售货机号、商品ID、卡号和商品金额【uvp传过来的扣款金额】，输出营销活动对象
	 */
	@SuppressLint("SimpleDateFormat")
	public QUICKPASSRuleResponse rule(String vmId, String productId, String cardNo, int money){
		
		QUICKPASSRuleResponse response = new QUICKPASSRuleResponse();
		
		Logger.info(String.format(">>>>rule param:vmId=%s;productId=%s;cardNo=%s;money=%s", vmId, productId, cardNo, money));
		int resultMoney = money;
		boolean record = false;
		boolean discount = false;
		int ruleType = 0;
		
		try {
			response.setCardNo(cardNo);
			response.setCount(0);
			response.setMoney(money);
			response.setRuleMoney(resultMoney);
			response.setRecord(false);
			response.setDiscount(false);
			response.setRuleType(ruleType);
			
			//5月22日－11月22日
			int startDate = 20150522;
			int endDate = 20151122;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd"); 
			Date nowTime = new Date();
			int nowDate = Integer.parseInt(sdf.format(nowTime));
			/*
			 * 判断日期
			 */
			if(nowDate < startDate || nowDate > endDate){
				Logger.info("date("+nowDate+") is not bjbank2015 rule ("+startDate+"-"+endDate+").");
				Logger.info("<<<< rule result:" + JSON.toJSONString(response));
				return response;
			}
			
			Map<String,Object> onefenMap = FileUtil.fileToMap(path, "1fen-"+nowDate+".txt");
			Map<String,Object> ninezheMap = FileUtil.fileToMap(path, "9zhe-"+nowDate+".txt");
			
			if(onefenMap.size() >= 20){//判断1分名额已满20
				Logger.info("today 1fen count is "+onefenMap.size());
				if(ninezheMap.containsKey(cardNo)){//判断是否参加过9折
					Logger.info(cardNo+" already done 9zhe.");
					Logger.info("<<<< rule result:"+JSON.toJSONString(response));
					return response;
				}else{
					Logger.info(cardNo+" is 9zhe ");
					resultMoney = money*90/100;
					ruleType = 2;
					record = true;
					discount = true;
				}
			}else{
				if(onefenMap.containsKey(cardNo)){//判断是否参加过1分
					Logger.info(cardNo+" already done 1fen.");
					if(ninezheMap.containsKey(cardNo)){//判断是否参加过9折
						Logger.info(cardNo+" already done 9zhe.");
						Logger.info("<<<< rule result:"+JSON.toJSONString(response));
						return response;
					}else{
						Logger.info(cardNo+" is 9zhe ");
						resultMoney = money*90/100;
						ruleType = 2;
						record = true;
						discount = true;
					}
				}else{
					if(money<=300){//判断商品是否3元及以下
						Logger.info(cardNo+" is 1fen ");
						resultMoney = 1;
						ruleType = 1;
						record = true;
						discount = true;	
					}else{
						Logger.info(money+" price > 3yuan ");
						if(ninezheMap.containsKey(cardNo)){//判断是否参加过9折
							Logger.info(cardNo+" already done 9zhe.");
							Logger.info("<<<< rule result:"+JSON.toJSONString(response));
							return response;
						}else{
							Logger.info(cardNo+" is 9zhe ");
							resultMoney = money*90/100;
							ruleType = 2;
							record = true;
							discount = true;
						}
					}
				}
			}
			
			response.setRuleMoney(resultMoney);
			response.setRecord(record);
			response.setDiscount(discount);
			response.setRuleType(ruleType);
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
		Logger.info("<<<< rule result:"+JSON.toJSONString(response));
		return response;
	}
	
	/**
	 * 记录做过营销活动的卡号等数据 
	 */
	@SuppressLint("SimpleDateFormat")
	public boolean record(String data,int ruleType){
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String nowDate = sdf.format(new Date());
			String fileName = "";
			if(ruleType == 1){
				fileName = "1fen-"+nowDate+".txt";
			}else if(ruleType == 2){
				fileName = "9zhe-"+nowDate+".txt";
			}else{
				Logger.error("ruleType("+ruleType+") is error, record fail");
				return false;
			}
			FileUtil.write(path + fileName, data, true);
			Logger.info(">>>>> record:"+data +" is sucussful.");
		}catch (Exception e) {
			Logger.error(e.getMessage(),e);
			return false;
		}
		
		return true;
	}
}
