package com.ubox.card.device.quickpass.rule;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import android.annotation.SuppressLint;

import com.ubox.card.config.CardConst;
import com.ubox.card.util.FileUtil;
import com.ubox.card.util.logger.Logger;

/**
 * 活动开始时间：8月17日－11月17日 活动-->活动规则：
 * 活动1：北京银行持卡人在参与机具上，通过闪付方式支付时，可以享受以每瓶饮品人民币1分的价格购买。活动期间，每月50台机具前优惠名额1万名，
 * 3个月共计3万名。每月超出1名后的持卡人享受优惠活动2。每IC借记卡每日限参与一次，先到先得，用完即止。
 * 活动2：北京银行持卡人在参与机具上，通过闪付方式支付时，可以享受以9折价格购买参与机具中的所有商品。每IC借记卡每日限参与一次。
 * 同一IC借记卡可同时参与上述优惠活动1、2。
 */

@SuppressLint("SimpleDateFormat")
public class QUICKPASSRuleTwo {
	private static String path = CardConst.DEVICE_WORK_PATH + File.separator;
	public static boolean isrecord = false;
	private static int ruleType;

	public int rule(String id, int money) {
		int Money = money;
		String Id = id;
		int date1 = 20150817;
		int date2 = 20150917;
		int date3 = 20151017;
		int date4 = 20151117;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			Date nowTime = new Date();
			int nowDate = Integer.parseInt(sdf.format(nowTime));

			if (nowDate < date1 || nowDate > date4) {// 判断不在8、17-11、17日期内
				Logger.info("<<<< date is error :" + nowDate);
				return Money;
			}
			// 今日刷卡卡号名单
			Map<String, Object> TodayCardMap = FileUtil.fileToMap(path, nowDate+ ".txt");
			if (TodayCardMap.containsKey(Id)) {// 卡今日已经刷过
				Logger.info("<<<< card is used today :" + Id);
				return Money;
			}
			// 参加1分卡号名单
			if (nowDate >= date1 && nowDate <= date2) {// 判断在第一个月
				Logger.info("<<<< is in zhe first month .");
				Map<String, Object> FMCardMap = FileUtil.fileToMap(path,"FirstMonth.txt");
				if (FMCardMap.containsKey(Id)) {// 已经参加1分活动
					Logger.info("FirstMonth.txt contain this id so it is 9 zhe. "+ Id);
					Money = money * 90 / 100;
					isrecord = true;
					ruleType = 1;
					return Money;
				}
				Logger.info("FirstMonth.txt size is :"+ FMCardMap.size());
				if (FMCardMap.size() >= 200) {
					Logger.info("because the size >= 200 so it is 9 zhe. ");
					Money = money * 90 / 100;
					isrecord = true;
					ruleType = 1;
					return Money;
				}
				Logger.info(Id + " is 1fen ");
				Money = 1;
				isrecord = true;
				ruleType = 2;
				return Money;
			}
			if (nowDate > date2 && nowDate <= date3) {// 判断在第二个月
				Logger.info("<<<< is in zhe second month .");
				Map<String, Object> FMCardMap = FileUtil.fileToMap(path,"FirstMonth.txt");
				Map<String, Object> SMCardMap = FileUtil.fileToMap(path,"SecondMonth.txt");
				if (SMCardMap.containsKey(Id)) {// 已经参加1分活动
					Logger.info("SecondMonth.txt contain this id so it is 9 zhe. "+ Id);
					Money = money * 90 / 100;
					isrecord = true;
					ruleType = 1;
					return Money;
				}
				int number = 400 - FMCardMap.size();
				Logger.info("SecondMonth.txt size is :"+ SMCardMap.size()+ ". the real number is :"+
							number);
				if (SMCardMap.size() >= number) {
					Logger.info("because the size >= real number so it is 9 zhe. ");
					Money = money * 90 / 100;
					isrecord = true;
					ruleType = 1;
					return Money;
				}
				Logger.info(Id + " is 1fen ");
				Money = 1;
				isrecord = true;
				ruleType = 3;
				return Money;
			}
			if (nowDate > date3 && nowDate <= date4) {// 判断在第三个月
				Logger.info("<<<< is in zhe third month .");
				Map<String, Object> FMCardMap = FileUtil.fileToMap(path,"FirstMonth.txt");
				Map<String, Object> SMCardMap = FileUtil.fileToMap(path,"SecondMonth.txt");
				Map<String, Object> TMCardMap = FileUtil.fileToMap(path,"ThirdMonth.txt");
				if (TMCardMap.containsKey(Id)) {// 已经参加1分活动
					Logger.info("ThirdMonth.txt contain this id so it is 9 zhe. "+ Id);
					Money = money * 90 / 100;
					isrecord = true;
					ruleType = 1;
					return Money;
				}
				int number = 600 - FMCardMap.size() - SMCardMap.size();
				Logger.info("ThirdMonth.txt size is :"+ TMCardMap.size()+ ". the real number is :"+
						number);
				if (TMCardMap.size() >= number) {
					Logger.info("because the size >= real number so it is 9 zhe. ");
					Money = money * 90 / 100;
					isrecord = true;
					ruleType = 1;
					return Money;
				}
				Logger.info(Id + " is 1fen ");
				Money = 1;
				isrecord = true;
				ruleType = 4;
				return Money;
			}
		} catch (Exception e) {
			Logger.error("<<<< Exception e :" + e.getMessage());
		}
		return Money;
	}

	/**
	 * 记录做过营销活动的卡号等数据
	 */
	@SuppressLint("SimpleDateFormat")
	public boolean record(String data) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String nowDate = sdf.format(new Date());
			String fileName = "";
			if (ruleType == 1) {
				FileUtil.write(path + nowDate + ".txt", data, true);
			} else if (ruleType == 2) {
				fileName = "FirstMonth.txt";
				FileUtil.write(path + fileName, data, true);
			} else if (ruleType == 3) {
				fileName = "SecondMonth.txt";
				FileUtil.write(path + fileName, data, true);
			} else if (ruleType == 4) {
				fileName = "ThirdMonth.txt";
				FileUtil.write(path + fileName, data, true);
			} else {
				Logger.error("ruleType(" + ruleType + ") is error, record fail");
				return false;
			}
			ruleType = 0;
			Logger.info(">>>>> record:" + data + " is sucussful.");
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}
}
