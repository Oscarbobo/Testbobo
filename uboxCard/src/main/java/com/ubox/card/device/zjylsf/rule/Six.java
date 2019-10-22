package com.ubox.card.device.zjylsf.rule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.SuppressLint;

import com.alibaba.fastjson.JSON;
import com.ubox.card.config.CardConst;
import com.ubox.card.util.FileUtil;
import com.ubox.card.util.device.DeviceUtils;
import com.ubox.card.util.logger.Logger;

public class Six {
	@SuppressLint("SimpleDateFormat")
	public RuleResponse rule(String vmId,String productId,String cardNo,int money) {
		RuleResponse response = new RuleResponse();
		
		int resultMoney = money;
		int count = 0;
		boolean record = false;
		boolean discount = false;
		
		Logger.info(String.format(">>>>rule param:vmId=%s;productId=%s;cardNo=%s;money=%s", vmId, productId, cardNo, money));
		try {
			response.setCardNo(cardNo);
			response.setCount(0);
			response.setMoney(money);
			response.setRuleMoney(money);
			response.setRecord(false);
			response.setDiscount(false);
			
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMddmmHHss");
			
			if(money > 1000) { // 超过10元的商品不打折
				Logger.warn("product price("+money+") is not rule.");
				Logger.info("<<<< rule result:"+response);
				return response;
			}
			
			String nowTime = format.format(new Date());
			String nowDate = nowTime.substring(0, 8);
			
			int now   = Integer.parseInt(nowDate);
			int start6fen = 20150530;//getConfigDate("start");
			int end6fen   = 20150602;//getConfigDate("end");
			int start6zhe = 20150603;
			int end6zhe = 20150830;
			
			if(now < start6fen || now > end6zhe) {
				Logger.warn("date("+now+") is not rule.");
				Logger.info("<<<< rule result:"+response);
				return response;
			}
			
			/*
			 * 判断卡号
			 */
			//1、判断今天是否已参加活动，如参加过且几次
			ConcurrentHashMap<String,String> cardNumMap =loadCardNoAndCount(nowDate+".txt");
			if(!cardNumMap.containsKey(cardNo)){
				Logger.info(">>>> 今日未参加活动，cardNo:"+cardNo);
				count = 1;
				discount = true;
				record = true;
			}else{
				int c = Integer.parseInt(cardNumMap.get(cardNo));
				if(c<3 && c>=0){
					Logger.info(">>>>> 今日已参加"+c+"次,cardNo:"+cardNo);
					count = c+1;
					discount = true;
					record = true;
				}else{
					Logger.warn(">>>> 今日已参加"+c+"次，无机会参加活动,cardNo:"+cardNo);
					return response;
				}
			}
			
			if(now>=start6fen && now <=end6fen){
				resultMoney = 6;
			}else {
				if(now >=start6zhe && now <=end6zhe){
					resultMoney = (int) Math.round(money * 0.6);
				}else{
					Logger.info(">>>> 不在日期范围内,原价售卖："+now);
					return response;
				}
			}
			
			response.setRuleMoney(resultMoney);
			response.setCount(count);
			response.setRecord(record);
			response.setDiscount(discount);
			
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
		
		Logger.info("<<<< rule result:"+JSON.toJSONString(response));
		return response;
		
	}
	
	@SuppressLint("SimpleDateFormat")
	public boolean record(String data) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String nowDate = sdf.format(new Date());

			String[] arr = data.split(":");
			String cardNo = arr[0];
			Integer count = Integer.parseInt(arr[1]);
			ConcurrentHashMap<String,String> cardNumMap =loadCardNoAndCount(nowDate+".txt");
			
			if(cardNumMap.containsKey(cardNo)){
				Integer tmp = Integer.parseInt(cardNumMap.get(cardNo));
				if((tmp+1) != count){
					Logger.error("data is error. "+nowDate+".txt cardNo:"+cardNo+" count:("+tmp +"+1)!="+count);
					return false;
				}else{
					cardNumMap.put(cardNo, count+"");
					FileUtil.mapToFile(CardConst.DEVICE_WORK_PATH+File.separator+nowDate+".txt", cardNumMap, ":", false);
				}
			}else{
				FileUtil.write(CardConst.DEVICE_WORK_PATH+File.separator+nowDate +".txt", data, true);
			}
			
			Logger.info(">>>>> record:"+data +" is sucussful.");
			return true;
		}catch (Exception e) {
			Logger.error(e.getMessage(),e);
			return false;
		}
	}
	
	/*
	 * 加载 卡号:次数 记录文件
	 */
	private static ConcurrentHashMap<String,String> loadCardNoAndCount(String nowDate){
		ConcurrentHashMap<String,String> map = new ConcurrentHashMap<String, String>();
		
		InputStream is = null;
		BufferedReader reader = null;
		try {
			File dataFile = DeviceUtils.holdFileRef(CardConst.DEVICE_WORK_PATH, nowDate);
			is = new FileInputStream(dataFile);

			String line; // 用来保存每行读取的内容
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			while ((line = reader.readLine()) != null) { // 如果 line 为空说明读完了
				if(!"".equals(line)){
					String[] tmpArr = line.split(":");
					if(tmpArr.length != 2){
						Logger.error(line+" is error, rule is ':'");
						return null;
					}
					
					Integer count = null;
					try {
						count = Integer.parseInt(tmpArr[1]);
					} catch (Exception e) {
						Logger.error(line+" is error, "+tmpArr[1]+" is not Integer.");
						return null;
					}
					
					map.put(tmpArr[0], count+"");
				}
			}

		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		} finally {
			try {
				if (is != null)
					is.close();
				if (reader != null)
					reader.close();
			} catch (Exception e) {
				Logger.error(e.getMessage(), e);
			}
		}
		
		Logger.info(">>>> cardNoAndCount Map:"+map);
		return map;
	}
	
}
