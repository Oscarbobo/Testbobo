package com.ubox.card.device.mzt;

import java.util.Arrays;

import android.annotation.SuppressLint;

import com.ubox.card.core.serial.RS232Worker;
import com.ubox.card.core.serial.SerialDuolne;
import com.ubox.card.util.Utils;
import com.ubox.card.util.device.DeviceUtils;
import com.ubox.card.util.logger.Logger;

public class MZTManager {
	
	public static final String FALSE    = "false";
	public static final String SUCCESS  = "success";
	public static final String TIME_OUT = "timeout";
	public static final String CANCEL   = "cancel";
	
	private static final String DEVICE_SUCCESS = "00";
	
	private static final int baudRate = 115200;
	private static final int dataBits = RS232Worker.DATABITS_8;
	private static final int stopBits = RS232Worker.STOPBITS_1;
	private static final int parity   = RS232Worker.PARITY_NONE;
	
	private static final SerialDuolne Doulne = new SerialDuolne(new MZTSerial(baudRate, dataBits, stopBits, parity)); 
	
	private static String cacheCardNO; // 缓存卡号: 机具TMD矬,需要应用程序保证同一张卡片刷卡
	private static String cacheBalance;
	
	/**
	 * 设备寻找卡片
	 * @param timeout 寻卡超时时间,单位ms
	 * @return 寻卡结果: [0]=寻卡结果,[1]=应答码,[2]=用户物理卡号
	 */
	@SuppressLint("DefaultLocale")
	public static String[] findCard(int timeout,String serNo) {
		byte[] cmd    = MZTUtils.genA0();
		int usedTime  = 0;
		int interval  = 1000;
		byte[] zipapa = null;
		
		/* * * * * * * * * * * * 接收数据 + 超时处理  * * * * * * * * * * *　*/
		while(usedTime < timeout) {
			if(Utils.isCancel(serNo) == true){
				return new String[] { CANCEL };
			}
			
			zipapa = Doulne.zipa(cmd, interval);
			if(zipapa != null) {
				break;
			}
			
			usedTime += interval;
		}
		
		if(usedTime > timeout) {
			return new String[] { TIME_OUT };
		}
		
		if(zipapa == null) {
			return new String[] { FALSE };
		}
		
		/* * * * * * * * * * 反馈数据流解析  * * * * * * * * * * * * * * */
		byte[] comb = MZTUtils.combingCMD(zipapa);
		if(comb == null) {
			return new String[] { FALSE };
		}
		
		String code = DeviceUtils.byte2HASCII(comb[3]);
		if(!code.equals(DEVICE_SUCCESS)) {
			Logger.warn("FALSE, code=" + code);
			return new String[] { FALSE };
		}
		
		byte[] csn = new byte[4];
		System.arraycopy(comb, 4, csn, 0, 4);
		
		return new String[] { SUCCESS, code, DeviceUtils.byteArray2HASCII(csn).toUpperCase()};
	}
	
	/**
	 * 读取卡片信息
	 * @return 读取结果: 
	 *         [0]=读卡结果,[1]=应答码,[2]=用户卡号,[3]=城市代码,[4]=行业代码,[5]=卡主类型,
	 * 		   [6]=卡子类型,[7]=卡版本,[8]=钱包余额,[9]=卡启用状态,[10]=卡启用日期,[11]=卡有效日期
	 */
	public static String[] readCard() {
		byte[] zipapa = Doulne.zipa(MZTUtils.genA1(), 2000);
		if(zipapa == null) {
			return new String[] { FALSE };
		}
		
		byte[] comb = MZTUtils.combingCMD(zipapa);
		if(comb == null) {
			return new String[] { FALSE };
		}
		
		String code = DeviceUtils.byte2HASCII(comb[3]);
		if(!code.equals(DEVICE_SUCCESS)) {
			Logger.warn("FALSE, code=" + code);
			return new String[] { FALSE };
		}
		
		/* * * * * * * * * * * * * * * * 读取卡信息  * * * * * * * * * * * * * * * * */
		String number       = DeviceUtils.byteArray2HASCII(Arrays.copyOfRange(comb, 4, 12));
		String cityCode     = DeviceUtils.byteArray2HASCII(Arrays.copyOfRange(comb, 12, 14)); 
		String industryCode = DeviceUtils.byteArray2HASCII(Arrays.copyOfRange(comb, 14, 16));
		String mainType     = DeviceUtils.byte2HASCII(comb[16]);
		String subType  	= DeviceUtils.byte2HASCII(comb[17]);
		String version  	= DeviceUtils.byte2HASCII(comb[18]);
		String balance      = DeviceUtils.byteArray2HASCII(Arrays.copyOfRange(comb, 19, 23));
		String useState		= DeviceUtils.byte2HASCII(comb[23]);
		String useDate	 	= DeviceUtils.byteArray2HASCII(Arrays.copyOfRange(comb, 24, 28));
		String effectDate	= DeviceUtils.byteArray2HASCII(Arrays.copyOfRange(comb, 28, 32));
		
		/* * * * * * * * * * * * * * * * 缓存卡号,卡余额  * * * *　* * * * * * * * * * * * */
		cacheCardNO  = number;
		cacheBalance = balance;
		
		return new String[] {
				SUCCESS, code, 	  number,  cityCode, industryCode, mainType,
				subType, version, balance, useState, useDate, 	   effectDate
			   };
	}
	
	/**
	 * 脱机消费
	 * @param money 扣款金额,单位:分
	 * @param cardNO 卡号
	 * @return [0]=消费结果,[1]=应答码,[2]=卡交易计数器,[3]=PSAM卡交易计数器,
	 *         [4]=PASM卡号,[5]=交易TAC
	 */
	public static String[] offlineConsume(int money, String cardNO) {
		if(!cardNO.equals(cacheCardNO)) {
			Logger.error("cardNO error.cardNO=" + cardNO);
			return new String[] { FALSE };
		}
		
		byte[] zipapa = Doulne.zipa(MZTUtils.genA2(money, cardNO), 2000);
		if(zipapa == null) {
			return new String[] { FALSE };
		}
		
		byte[] comb = MZTUtils.combingCMD(zipapa);
		if(comb == null) {
			return new String[] { FALSE };
		}
		
		String code = DeviceUtils.byte2HASCII(comb[3]);
		if(code.equals(DEVICE_SUCCESS)) {
			byte[] cardTradeCount = Arrays.copyOfRange(comb, 4, 6);
			byte[] pasmTradeCount = Arrays.copyOfRange(comb, 6, 10);
			byte[] pasmNumber     = Arrays.copyOfRange(comb, 10, 16);
			byte[] tac            = Arrays.copyOfRange(comb, 16, 20);
					
			return new String[] { 
					SUCCESS, DEVICE_SUCCESS, 
					DeviceUtils.byteArray2HASCII(cardTradeCount),
					DeviceUtils.byteArray2HASCII(pasmTradeCount),
					DeviceUtils.byteArray2HASCII(pasmNumber),
					DeviceUtils.byteArray2HASCII(tac)
					};
		} else if(code.equals("08")) {
			/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
			 * "08"处理:
			 * 1、读取卡信息,读卡失败按照扣款失败处理
			 * 2、判断是否同一张卡;非同一张卡,按照扣款失败处理
			 * 3、如果是同一张卡,则根据余额判断是否已扣款
			 * 4、如果扣款成功，则发送A5获得TAC,否则扣款失败处理
			 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
			String[] cardInfo = readCard();
			if(!cardInfo[0].equals(SUCCESS)) { 		// 读卡失败
				Logger.warn("08: read card fail");
				return new String[] { FALSE };
			}
			
			if(!cardInfo[2].equals(cacheCardNO)) { 	// 卡号不同
				Logger.warn("08: not the same card");
				return new String[] { FALSE };
			}
			
			if(cardInfo[8].equals(cacheBalance)) {	// 余额不变
				Logger.warn("08: do not consume.");
				return new String[] { FALSE };
			}
			
			byte[] cardTradeCount = Arrays.copyOfRange(comb, 4, 6);
			String costTAC 		  = getCostTAC(cardTradeCount);
			
			if(costTAC == null) {
				Logger.warn("08: get TAC fail.");
				return new String[] { FALSE };
			} else {
				byte[] pasmTradeCount = Arrays.copyOfRange(comb, 6, 10);
				byte[] pasmNumber     = Arrays.copyOfRange(comb, 10, 16);

				return new String[] { 
						SUCCESS, DEVICE_SUCCESS, 
						DeviceUtils.byteArray2HASCII(cardTradeCount),
						DeviceUtils.byteArray2HASCII(pasmTradeCount),
						DeviceUtils.byteArray2HASCII(pasmNumber),
						costTAC
				};
			}
		} else { // 直接就是扣款失败
			return new String[] { FALSE };
		}
	}
	
	/**
	 * 获取TAC
	 * @param cardTradeCount 卡交易计数
	 * @return 成功返回TAC,失败返回NULL
	 */
	private static String getCostTAC(byte[] cardTradeCount) {
		byte[] zipapa = Doulne.zipa(MZTUtils.genA5(cardTradeCount), 2000);
		if(zipapa == null) {
			return null;
		}
		
		byte[] comb = MZTUtils.combingCMD(zipapa);
		if(comb == null) {
			return null;
		}
		
		String code = DeviceUtils.byte2HASCII(comb[3]);
		if(code.equals(DEVICE_SUCCESS)) {
			byte[] tac = Arrays.copyOfRange(comb, 3, 7);
			return DeviceUtils.byteArray2HASCII(tac);
		} else if(code.equals("08") || code.equals("05")) {
			/* * * * * * * * * * * * * * * * * * * 
			 * 再尝试一次获取TAC,如果获取失败,则按照失败处理 
			 * * * * * * * * * * * * * * * * * * */
			byte[] zipapa2 = Doulne.zipa(MZTUtils.genA5(cardTradeCount), 2000);
			if(zipapa2 == null) {
				return null;
			}

			byte[] comb2 = MZTUtils.combingCMD(zipapa2);
			if(comb2 == null) {
				return null;
			}
			
			String code2 = DeviceUtils.byte2HASCII(comb2[3]); 
			if(code2.equals(DEVICE_SUCCESS)) {
				byte[] tac2 = Arrays.copyOfRange(comb2, 3, 7);
				return DeviceUtils.byteArray2HASCII(tac2);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
}
