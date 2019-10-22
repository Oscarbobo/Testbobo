package com.ubox.card.device.bbjtyh;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;

import com.ubox.card.core.serial.IcCom;
import com.ubox.card.util.RSCalcUtils;
import com.ubox.card.util.Utils;
import com.ubox.card.util.logger.Logger;

@SuppressLint("DefaultLocale")
public class BBJTYHER {

	static enum RESULT { SUCCESS, CANCEL, TIMEOUT, ERROR };
	
	private IcCom com = new IcCom(9600,IcCom.DATABITS_8,IcCom.STOPBITS_1,IcCom.PARITY_EVEN);
	
	public void open(){
		com.open();
	}
	
	public void close(){
		com.close();
	}
	
	/**
	 * 异或校验
	 * 
	 * @param data
	 * @return
	 */
	private byte getBCC(byte[] data) {
		if (null == data) {
			throw new NullPointerException();
		}
		byte pb = data[2];
		for (int i = 3, len = data.length - 2; i < len; i++) {
			pb ^= data[i];
		}
		return pb;
	}
	
	/**
	 * 寻卡
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	public RESULT readCard(){
		byte[] sendBytes = {(byte)0x02,(byte)0x02,(byte)0xA1,(byte)0x00,(byte)0x00,(byte)0x03};	
		try {
			sendBytes[sendBytes.length - 2] = getBCC(sendBytes);

			//发送数据
			OutputStream os = com.getOutputStream();
			InputStream  is = com.getInputStream();
			os.write(sendBytes);
			Logger.info(">>> send readCard:" + Utils.toHex(sendBytes));
			//计时开始
			long startTime = System.currentTimeMillis();
			
			while(is.available() < 0) {
				long endTime= System.currentTimeMillis();
				if(endTime-startTime >= 2*1000){
					Logger.warn("receive data is timeout. 2s");
					return RESULT.TIMEOUT;
				}
				
			}

			Thread.sleep(1000);

			int blockType = 0;
			blockType = is.read();
			
			int len       = is.read();
			byte[] receiveBytes    = new byte[len + 4];
			
			receiveBytes[0] = (byte)(blockType & 0xff);
			receiveBytes[1] = (byte)(len & 0xff);
			
			for(int i = 0; i < len; ++i){
				receiveBytes[i+2] = (byte)(is.read() & 0xff);
			}
			
			receiveBytes[receiveBytes.length-2] = (byte)(is.read() & 0xff);
			receiveBytes[receiveBytes.length-1] = (byte)(is.read() & 0xff);
			
			Logger.info("<<<receive readCard:"+Utils.toHex(receiveBytes).toUpperCase());
			
			//校验头
			int receiveLenth = receiveBytes.length;
			if((byte)0x02 != receiveBytes[0]){
				Logger.error("receive read Card response's head is error. 0x02 != "+Utils.toHex1(receiveBytes[0]));
				return RESULT.ERROR;
			}
			
			//校验尾
			if((byte)0x03 != receiveBytes[receiveLenth-1]){
				Logger.error("receive read Card response's end is error. 0x03 != "+Utils.toHex1(receiveBytes[receiveLenth-1]));
				return RESULT.ERROR;
			}
			
			//校验验证码
			if(getBCC(receiveBytes)!= receiveBytes[receiveLenth-2]){
				Logger.error("receive read Card response's BCC is error. "+Utils.toHex1(getBCC(receiveBytes)) +"!="+ Utils.toHex1(receiveBytes[receiveLenth-2]));
				return RESULT.ERROR;
			}
			
			long endTime = System.currentTimeMillis();//计时
			Logger.debug("read card time:" + (endTime - startTime) / 1000.00 + "s");	
			
			if(Utils.toHex1(receiveBytes[2]).toUpperCase().equals("00")){
				return RESULT.SUCCESS;
			}else{
				if(Utils.toHex1(receiveBytes[2]).toUpperCase().equals("01")){
					//无卡
					return RESULT.TIMEOUT;
				}else{
					return RESULT.ERROR;
				}
			}
		} catch (Exception e) {
			Logger.error("read card is exception. "+e.getMessage(), e);
		}
		
		return RESULT.ERROR;
	}
	/**
	 * 扣款
	 * @param money 金额,单位:分
	 * @return 扣款结果
	 */
	public Map<String, Object> cost(int money,String serialNO) {
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("result", "01");
		
		try {
			if(serialNO.length()!=20){//判断长度是否20
				Logger.error(">>>> serialNo is error. length != 20, serialNo:"+serialNO);
				return map;
			}

			String[] spMoney = string2BcdArray(int2string(money, 12), 2);
			byte[] seq = str2BCD(serialNO);
			byte[] seqBCD = new byte[10];
			System.arraycopy(seq, 0, seqBCD, 0, seqBCD.length);
			
			byte[] sendBytes = { (byte) 0x02, (byte) 0x11, (byte) 0xA2,
					(byte)Integer.parseInt(spMoney[0],16) ,(byte)Integer.parseInt(spMoney[1],16) ,(byte)Integer.parseInt(spMoney[2],16) ,
					(byte)Integer.parseInt(spMoney[3],16) ,(byte)Integer.parseInt(spMoney[4],16) ,(byte)Integer.parseInt(spMoney[5],16) ,
					seqBCD[0], seqBCD[1], seqBCD[2],
					seqBCD[3], seqBCD[4], seqBCD[5], 
					seqBCD[6], seqBCD[7], seqBCD[8], 
					seqBCD[9],(byte)0x00,(byte)0x03};
			sendBytes[sendBytes.length - 2] = getBCC(sendBytes);

			OutputStream os = com.getOutputStream();
			InputStream  is = com.getInputStream();
			
			// 发送数据
			os.write(sendBytes);
			Logger.info(">>> send cost:" + Utils.toHex(sendBytes).toUpperCase());

			long startTime = System.currentTimeMillis();
			
			while(is.available()<0) {
				long endTime= System.currentTimeMillis();
				if(endTime-startTime >= 10*1000){
					Logger.warn("receive data is timeout. 10s");
					return map;
				}
				
			}
			Thread.sleep(1000);
			
			int blockType = 0;
			blockType = is.read();
			
			int len       = is.read();
			byte[] receiveBytes    = new byte[len + 4];
			
			receiveBytes[0] = (byte)(blockType & 0xff);
			receiveBytes[1] = (byte)(len & 0xff);
			
			for(int i = 0; i < len; ++i){
				receiveBytes[i+2] = (byte)(is.read() & 0xff);
			}
			
			receiveBytes[receiveBytes.length-2] = (byte)(is.read() & 0xff);
			receiveBytes[receiveBytes.length-1] = (byte)(is.read() & 0xff);
			
			
			Logger.info("<<<receive cost:" + Utils.toHex(receiveBytes).toUpperCase());

			// 校验头
			int receiveLenth = receiveBytes.length;
			if ((byte) 0x02 != receiveBytes[0]) {
				Logger.error("receive cost response's head is error. 0x02 != "+Utils.toHex1(receiveBytes[0]));
				return map;
			}

			// 校验尾
			if ((byte) 0x03 != receiveBytes[receiveLenth - 1]) {
				Logger.error("receive cost response's end is error. 0x03 != "+Utils.toHex1(receiveBytes[receiveLenth - 1]));
				return map;
			}

			//校验BCC值
			if (getBCC(receiveBytes)!= receiveBytes[receiveLenth - 2]) {
				Logger.error("receive cost response's BCC is error. "+Utils.toHex1(getBCC(receiveBytes)) +"!="+ Utils.toHex1(receiveBytes[receiveLenth - 2]));
				return map;
			}
			
			
			map.put("result",Utils.toHex1(receiveBytes[2]));
			if ("00".equals(Utils.toHex1(receiveBytes[2]).toUpperCase())) {
				byte[] cardNO = new byte[10];// 卡号
				System.arraycopy(receiveBytes, 3, cardNO, 0,cardNO.length);
				map.put("cardNO", RSCalcUtils.BCD2StringBE(cardNO));
				
				byte[] cardBalance = new byte[6];//余额
				System.arraycopy(receiveBytes,13,cardBalance,0,cardBalance.length);
				String tmpBalance = RSCalcUtils.BCD2StringBE(cardBalance);
				if(!"999999999999".equals(tmpBalance)){
					try {
						map.put("cardBalance", Integer.parseInt(tmpBalance));
					} catch (Exception e) {
						Logger.error("cardBalance is error"+e.getMessage());
					}
				}
				
				byte[] cardDev = new byte[8];//终端机编号
				System.arraycopy(receiveBytes, 19, cardDev, 0, cardDev.length);
				map.put("cardDev", Utils.charsToString(cardDev));
				
				byte[] costSeq = new byte[3];//扣款流水号
				System.arraycopy(receiveBytes, 27, costSeq, 0, costSeq.length);
				map.put("costSeq", 	RSCalcUtils.BCD2StringBE(costSeq));
				
				byte[] costDate = new byte[4];//扣款日期
				System.arraycopy(receiveBytes, 30, costDate, 0, costDate.length);
				map.put("costDate", RSCalcUtils.BCD2StringBE(costDate));

				
				byte[] costTime = new byte[3];//扣款时间
				System.arraycopy(receiveBytes, 34, costTime, 0, costTime.length);
				map.put("costTime", RSCalcUtils.BCD2StringBE(costTime));
			}
			if("02".equals(Utils.toHex1(receiveBytes[2]).toUpperCase())){
				byte[] cardBalance = new byte[6];//余额
				System.arraycopy(receiveBytes,13,cardBalance,0,cardBalance.length);
				map.put("cardBalance", Integer.parseInt(RSCalcUtils.BCD2StringBE(cardBalance)));
			}

			long endTime = System.currentTimeMillis();// 计时开始
			Logger.info("cost time:" + (endTime - startTime) / 1000.00 + "s");
		} catch (Exception e) {
			Logger.error("costMoney is error:"+e.getMessage() , e);
		}
		Logger.info("<<<< map:"+map.toString());
		return map;
		
	}
	
	/**
	 * 根据指定长度拆分字符串（金额），不足位补0
	 * @param s
	 * @param length
	 * @return
	 */
	private String[] string2BcdArray(String s, int length){
		if (length < 1) {
			return new String[]{s};
		}
		int size = length - (s.length() % length);
		if (s.length() % length != 0) {
			for (int i = 0; i < size; i++) {
				s = "0" + s;
			}
		}
		char[] c = s.toCharArray();
		List<String> rel = new ArrayList<String>();
		String temp = "";
		for (int i = 0; i < c.length; i++) {
			temp += c[i];
			if ((i + 1) % length == 0) {
//				System.out.println(temp);
				rel.add(temp + "");
				temp = "";
			}
		}
		return rel.toArray(new String[rel.size()]);
	}
	
	public byte[] str2BCD(String str) {
		byte[] BCD = new byte[(str.length() + 1) / 2];
		
		if(str.length() % 2 != 0)
			str = "0" + str;

		for(int i = 0; i < str.length() - 1; ) {
			byte high = (byte)(str.charAt(i ++) - '0');
			byte low = (byte)(str.charAt(i ++) - '0');

			BCD[(i - 1) / 2] = (byte)(low | (high << 4));
		}

		return BCD;
	}
	
	/**
	 * 数字转字符串，自动补0
	 * 
	 * @param num 数字
	 * @param len 返回字符串长度
	 * @return String
	 */
	private String int2string(Integer num,Integer len) {
		String tmp = "";
		for(int i =0;i<len;i++){
			tmp="0"+tmp;
		}
		if("".equals(tmp)){
			return "";
		}
		
		String resultStr = tmp+num+"";
		return resultStr.substring(resultStr.length()-len,resultStr.length());
		
	}
	
	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	
}
