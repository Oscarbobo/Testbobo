package com.ubox.card.device.uboxs2;

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
import com.ubox.card.util.device.DeviceUtils;
import com.ubox.card.util.logger.Logger;

@SuppressLint("DefaultLocale")
public class UBOXSER2 {

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
	public RESULT readCard(String nb){
		byte[] sendBytes = {(byte)0x02,(byte)0x02,(byte)0xa1,(byte)0x00,(byte)0xa1,(byte)0x03};	
		try {
			sendBytes[sendBytes.length - 2] = getBCC(sendBytes);

			//发送数据
			OutputStream os = com.getOutputStream();
			InputStream  is = com.getInputStream();
			os.write(sendBytes);
			Logger.info(">>> send readCard:" + Utils.toHex(sendBytes));
			//计时开始
			long startTime = System.currentTimeMillis();
			
			while(is.available() <= 0) {
				if (Utils.isCancel(nb) == true) {
					return RESULT.CANCEL;
				}
				long endTime= System.currentTimeMillis();
				if(endTime-startTime >= 2*1000){
					Logger.warn("<<<<<<<<<<<<<<<  Receive data is timeout. 2s");
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
	public Map<String, Object> cost(int money,String serialNO,String vmid) {
		int timeOut = 15*1000;
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("result", "01");
		
		try {
			if(serialNO.length()!=20){//判断长度是否20
				Logger.error(">>>> serialNo is error. length != 20, serialNo:"+serialNO);
				return map;
			}
			if(vmid.length()!=20){//判断长度是否20
				Logger.error(">>>> serialNo is error. length != 20, serialNo:"+vmid);
				return map;
			}

			String[] spMoney = string2BcdArray(int2string(money, 12), 2);
			String[] seqBCD = string2BcdArray(serialNO, 2);
//			byte[] seqBCD = serialNO.getBytes();
//			byte[] seqBCD = new byte[10];
//			System.arraycopy(seq, 0, seqBCD, 0, seqBCD.length);
			byte[] vmId = vmid.getBytes(); 
			String vmId1 = DeviceUtils.byteArray2HASCII(vmId);
			byte[] vmId2 = vmId1.getBytes();
			Logger.info("<<<<<<<<<<<<<<<<  vmId : " +vmId1);
			
			byte[] sendBytes = { (byte) 0x02, (byte) 0x1b, (byte) 0xa2,
					(byte)Integer.parseInt(spMoney[0],16) ,(byte)Integer.parseInt(spMoney[1],16) ,(byte)Integer.parseInt(spMoney[2],16) ,
					(byte)Integer.parseInt(spMoney[3],16) ,(byte)Integer.parseInt(spMoney[4],16) ,(byte)Integer.parseInt(spMoney[5],16) ,
					(byte)Integer.parseInt(seqBCD[0],16),(byte)Integer.parseInt(seqBCD[1],16),(byte)Integer.parseInt(seqBCD[2],16),
					(byte)Integer.parseInt(seqBCD[3],16),(byte)Integer.parseInt(seqBCD[4],16),(byte)Integer.parseInt(seqBCD[5],16),
					(byte)Integer.parseInt(seqBCD[6],16),(byte)Integer.parseInt(seqBCD[7],16),(byte)Integer.parseInt(seqBCD[8],16),
					(byte)Integer.parseInt(seqBCD[9],16), vmId2[0], vmId2[1], vmId2[2], vmId2[3], vmId2[4], vmId2[5], vmId2[6], vmId2[7],
					vmId2[8], vmId2[9], (byte)0x00, (byte)0x03 };
			sendBytes[sendBytes.length - 2] = getBCC(sendBytes);

			OutputStream os = com.getOutputStream();
			InputStream  is = com.getInputStream();
			
			// 发送数据
			os.write(sendBytes);

			long startTime = System.currentTimeMillis();
			Logger.info(">>> send cost:" + Utils.toHex(sendBytes).toUpperCase()+"---startTime:"+startTime);

			
			while(is.available()<=0) {
				long endTime= System.currentTimeMillis();
				if(endTime-startTime >= timeOut){
					Logger.warn("receive data is timeout."+timeOut/1000+" s---startTime:"+startTime);
					return map;
				}
				Thread.sleep(200);
			}

			Thread.sleep(1000);
			int blockType = is.read();
			
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
			
			if (receiveLenth != 54) {
				Logger.error("receive cost lenth is error. ! lenth: = "+receiveLenth);
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
				
				byte[] cardCostMoney = new byte[6];//扣款金额
				System.arraycopy(receiveBytes,13,cardCostMoney,0,cardCostMoney.length);
				String costMoney = RSCalcUtils.BCD2StringBE(cardCostMoney);
				map.put("cardCostMoney", Integer.parseInt(costMoney));
				Logger.info("cardCostMoney : "+costMoney);
				byte[] cardBalance = new byte[6];//余额
				System.arraycopy(receiveBytes,19,cardBalance,0,cardBalance.length);
				String tmpBalance = RSCalcUtils.BCD2StringBE(cardBalance);
				Logger.info("cardBalance : "+tmpBalance);
				
				if(!"999999999999".equals(tmpBalance)){
					try {
						map.put("cardBalance", Integer.parseInt(tmpBalance));
					} catch (Exception e) {
						Logger.error("cardBalance is error"+e.getMessage());
					}
				}
				
				byte[] cardDev = new byte[10];//终端机编号
				System.arraycopy(receiveBytes, 25, cardDev, 0, cardDev.length);
				map.put("cardDev", Utils.charsToString(cardDev));
				
				byte[] costSeq = new byte[10];//扣款流水号
				System.arraycopy(receiveBytes, 35, costSeq, 0, costSeq.length);
				map.put("costSeq", 	RSCalcUtils.BCD2StringBE(costSeq));
				
				byte[] costDate = new byte[7];//扣款日期
				System.arraycopy(receiveBytes, 45, costDate, 0, costDate.length);
				map.put("costDate", RSCalcUtils.BCD2StringBE(costDate));

			}
			if("02".equals(Utils.toHex1(receiveBytes[2]).toUpperCase())){
				byte[] cardBalance = new byte[6];//余额
				System.arraycopy(receiveBytes,19,cardBalance,0,cardBalance.length);
				map.put("cardBalance", Integer.parseInt(RSCalcUtils.BCD2StringBE(cardBalance)));
			}

			long endTime = System.currentTimeMillis();// 计时开始
			Logger.info("cost time:" + (endTime - startTime) / 1000.00 + "s");
		} catch (Exception e) {
			Logger.error("costMoney is error:"+e.getMessage() , e);
			map.put("result", "01");
			return map;
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
	
//	public byte[] str2BCD(String str) {
//		byte[] BCD = new byte[(str.length() + 1) / 2];
//		
//		if(str.length() % 2 != 0)
//			str = "0" + str;
//
//		for(int i = 0; i < str.length() - 1; ) {
//			byte high = (byte)(str.charAt(i ++) - '0');
//			byte low = (byte)(str.charAt(i ++) - '0');
//
//			BCD[(i - 1) / 2] = (byte)(low | (high << 4));
//		}
//
//		return BCD;
//	}
	
	/**
	 * 数字转字符串，自动补0
	 * 
	 * @param num 数字
	 * @param len 返回字符串长度
	 * @return String
	 */
	public static String int2string(long num,Integer len) {
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
