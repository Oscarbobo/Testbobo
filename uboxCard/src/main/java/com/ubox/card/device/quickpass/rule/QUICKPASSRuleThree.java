package com.ubox.card.device.quickpass.rule;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import android.annotation.SuppressLint;

import com.ubox.card.config.CardConst;
import com.ubox.card.util.FileUtil;
import com.ubox.card.util.logger.Logger;

@SuppressLint("SimpleDateFormat")
public class QUICKPASSRuleThree {
	private static String path = CardConst.DEVICE_WORK_PATH + File.separator;
	public static boolean isrecord = false;
	private static int ruleType;

	public int rule(String id, int money) {
		int Money = money;
		String Id = id;
		int date1 = 20160301;
		int date2 = 20160930;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			Date nowTime = new Date();
			int nowDate = Integer.parseInt(sdf.format(nowTime));

			if (nowDate < date1 || nowDate > date2) {// 判断不在活动日期内
				Logger.info("<<<< date is error :" + nowDate);
				return Money;
			}
			// 今日刷卡卡号名单
			Map<String, Object> TodayCardMap5 = FileUtil.fileToMap(path, nowDate+ "5.txt");
			if (TodayCardMap5.containsKey(Id)) {// 卡今日已经刷过
				Logger.info("<<<< card is used today :" + Id);
				return Money;
			}
			Map<String, Object> TodayCardMap1 = FileUtil.fileToMap(path, nowDate+ "1.txt");
			Map<String, Object> TodayCardMap2 = FileUtil.fileToMap(path, nowDate+ "2.txt");
			Map<String, Object> TodayCardMap3 = FileUtil.fileToMap(path, nowDate+ "3.txt");
			Map<String, Object> TodayCardMap4 = FileUtil.fileToMap(path, nowDate+ "4.txt");
			if (!TodayCardMap1.containsKey(Id)) {// 第1次
				Logger.info("<<<< is zhe first time .");
				Money = 1;
				isrecord = true;
				ruleType = 1;
				return Money;
			}
			if (!TodayCardMap2.containsKey(Id)) {// 第2次
				Logger.info("<<<< is zhe second time .");
				Money = 1;
				isrecord = true;
				ruleType = 2;
				return Money;
			}
			if (!TodayCardMap3.containsKey(Id)) {// 第3次
				Logger.info("<<<< is zhe third time .");
				Money = 1;
				isrecord = true;
				ruleType = 3;
				return Money;
			}
			if (!TodayCardMap4.containsKey(Id)) {// 第4次
				Logger.info("<<<< is zhe fourth time .");
				Money = 1;
				isrecord = true;
				ruleType = 4;
				return Money;
			}
			if (!TodayCardMap5.containsKey(Id)) {// 第5次
				Logger.info("<<<< is zhe fifth time .");
				Money = 1;
				isrecord = true;
				ruleType = 5;
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
			if (ruleType == 1) {
				FileUtil.write(path + nowDate + "1.txt", data, true);
			} else if (ruleType == 2) {
				FileUtil.write(path + nowDate + "2.txt", data, true);
			} else if (ruleType == 3) {
				FileUtil.write(path + nowDate + "3.txt", data, true);
			} else if (ruleType == 4) {
				FileUtil.write(path + nowDate + "4.txt", data, true);
			} else if (ruleType == 5) {
				FileUtil.write(path + nowDate + "5.txt", data, true);
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
