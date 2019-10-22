package com.ubox.card.device.whtlf;

import com.ubox.card.core.serial.IcCom;
import com.ubox.card.util.Utils;
import com.ubox.card.util.device.DeviceUtils;
import com.ubox.card.util.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class WHTLFer {

	private static final WHTLFer wht2er = new WHTLFer();
	
	private WHTLFer(){}
	
	public static WHTLFer getInstance(){
		return wht2er;
	}

	static enum RESULT { SUCCESS, CANCEL, TIMEOUT, ERROR };
	
	private IcCom com = new IcCom(4800,IcCom.DATABITS_8,IcCom.STOPBITS_1,IcCom.PARITY_EVEN);
	
	/**
	 * 打开串口
	 * @return
	 */
	public RESULT open(){
		try {
			com.open();
		} catch (Exception e) {
			Logger.error("open is exception. "+e.getMessage(), e);
			return RESULT.ERROR;
		}
		return RESULT.SUCCESS;
	}
	
	/**
	 * 关闭串口
	 * @return
	 */
	public RESULT close(){
		try {
			com.close();
		} catch (Exception e) {
			Logger.error("close is exception. "+e.getMessage(), e);
			return RESULT.ERROR;
		}
		return RESULT.SUCCESS;
	}
	
	/**
	 * 测试设备
	 * @return
	 */
	public RESULT test(){
		try {
			Logger.info("test start ");
			long startTime = System.currentTimeMillis();
			byte[] send = {(byte)0x05};
			
			/* 与刷卡设备交互信息  */
			byte[] ret;
			int timeout = 1;//秒
			int sleepTime = 100;//毫秒
			try {
				ret = talkWith(send,timeout,sleepTime);
			} catch (TimeoutException e) {
				Logger.error("test TimeoutException. "+e.getMessage(),e);
				return RESULT.TIMEOUT;
			} catch (Exception e) {
				Logger.error("test Exception. "+e.getMessage(),e);
				return RESULT.ERROR;
			}
			
			if(ret == null || ret.length < 1){
				Logger.error("test receive bytes is null or length < 1. data:"+Utils.toHex(ret));
				return RESULT.ERROR;
			}
			if(ret[0] != (byte)0x06){
				Logger.error("test receive data is error. data:"+Utils.toHex(ret));
				return RESULT.ERROR;
			}
			
			Logger.info("test end. time:"+(System.currentTimeMillis()-startTime)/1000.00+"s");
			return RESULT.SUCCESS;
		} catch (Exception e) {
			Logger.error("test is exception. "+e.getMessage(),e);
			return RESULT.ERROR;
		}
	}
	
	/**
	 * 读卡
	 * @return
	 */
	public Map<String,Integer> read(){
		Map<String,Integer> map = new HashMap<String, Integer>();
		map.put("code", -1);//0:成功，1:无卡, 2:需要复位(PW),3:没有应答,其他都是错误 
		try {
			Logger.info("read start ");
			long startTime = System.currentTimeMillis();
			byte[] send = {(byte)0x02,(byte)0x43,(byte)0x52,(byte)0x03,(byte)0x12};
			
			/* 与刷卡设备交互信息  */
			byte[] ret;
			int timeout = 2;
			int sleepTime = 1000;//毫秒
			try {
				ret = talkWith(send,timeout,sleepTime);
			} catch (TimeoutException e) {
				Logger.error("read TimeoutException. "+e.getMessage(),e);
				map.put("code", 3);
				return map;
			} catch (Exception e) {
				Logger.error("read Exception. "+e.getMessage(),e);
				return map;
			}
			
			if(ret == null || ret.length < 4){
				Logger.error("read receive bytes is null or length < 4. data:"+Utils.toHex(ret));
				return map;
			}
			//判断头
			if(ret[0] != (byte)0x02){
				Logger.error("read receive data STR is error. "+Utils.toHex1(ret[0])+"!=02");
				return map;
			}
			//判断尾
			if(ret[ret.length-2] != (byte)0x03){
				Logger.error("read receive data EXT is error. "+Utils.toHex1(ret[ret.length-2])+"!=03");
				return map;
			}
			//判断LRC
			byte lrc = lrc(ret);
			if(ret[ret.length-1] != lrc){
				Logger.error("read receive data LRC is error. "+Utils.toHex1(ret[ret.length-1])+"!="+Utils.toHex1(lrc));
				return map;
			}
			
			//判断无卡
			if(ret[1] == (byte)0x43 && ret[2] == (byte)0x52){
				map.put("code", 1);
				return map;
			}
			
			//判断需要复位
			if(ret[1] == (byte)0x50 && ret[2] == (byte)0x57){
				map.put("code", 2);
				return map;
			}
			
			//判断有卡成功
			if(ret[1] == (byte)0x63 && ret[2] == (byte)0x72){
				if(ret[ret.length-3] != (byte)0x00){
					return map;
				}
				//处理余额
				byte[] byteBalance = new byte[3];
				byteBalance = Arrays.copyOfRange(ret, 7, 10);
				String strBalance = Utils.toHex1(byteBalance[2])+Utils.toHex1(byteBalance[1])+Utils.toHex1(byteBalance[0]);
				int balance = Integer.parseInt(strBalance)*10;
				Logger.info("balance:"+balance);
				map.put("code", 0);
				map.put("balance", balance);
			}
			
			Logger.info("read end. map:"+map.toString()+",time:"+(System.currentTimeMillis()-startTime)/1000.00+"s");
			return map;
		} catch (Exception e) {
			Logger.error("read is exception. "+e.getMessage(),e);
			return map;
		}
	}
	
	/**
	 * 复位
	 * @return
	 */
	public RESULT reset(){
		try {
			Logger.info("reset start ");
			long startTime = System.currentTimeMillis();
			byte[] send = {(byte)0x02,(byte)0x50,(byte)0x44,(byte)0x03,(byte)0x17};
			
			/* 与刷卡设备交互信息  */
			byte[] ret;
			int timeout = 1;
			int sleepTime = 500;
			try {
				ret = talkWith(send,timeout,sleepTime);
			} catch (TimeoutException e) {
				Logger.error("reset TimeoutException. "+e.getMessage(),e);
				return RESULT.TIMEOUT;
			} catch (Exception e) {
				Logger.error("reset Exception. "+e.getMessage(),e);
				return RESULT.ERROR;
			}
			
			if(ret == null || ret.length != 6 ){
				Logger.error("reset receive bytes is null or length !=6. data:"+Utils.toHex(ret));
				return RESULT.ERROR;
			}
			//判断头
			if(ret[0] != (byte)0x02){
				Logger.error("reset receive data STR is error. "+Utils.toHex1(ret[0])+"!=02");
				return RESULT.ERROR;
			}
			//判断尾
			if(ret[ret.length-2] != (byte)0x03){
				Logger.error("reset receive data EXT is error. "+Utils.toHex1(ret[ret.length-2])+"!=03");
				return RESULT.ERROR;
			}
			//判断LRC
			byte lrc = lrc(ret);
			if(ret[ret.length-1] != lrc){
				Logger.error("reset receive data LRC is error. "+Utils.toHex1(ret[ret.length-1])+"!="+Utils.toHex1(lrc));
				return RESULT.ERROR;
			}
			
			Logger.info("reset end. time:"+(System.currentTimeMillis()-startTime)/1000.00+"s");
			return RESULT.SUCCESS;
		} catch (Exception e) {
			Logger.error("read is exception. "+e.getMessage(),e);
			return RESULT.ERROR;
		}
	}
	
	/**
	 * 扣款
	 * @param money 金额,单位:分
	 * @return 扣款结果
	 */
	public RESULT pay(int money) {
		try {
			Logger.info("pay start ");
			long startTime = System.currentTimeMillis();
			String strMoney = "0000000"+money;
			strMoney = strMoney.substring(strMoney.length()-1-6,strMoney.length()-1);
			byte[] bMoney = Utils.decodeHex(strMoney);
			
			byte[] send = {(byte)0x02,(byte)0x43,(byte)0x57,bMoney[2],bMoney[1],bMoney[0],(byte)00,(byte)00,(byte)03,(byte)12};
			send[send.length-1] = lrc(send);
			
			/* 与刷卡设备交互信息*/
			byte[] ret;
			int timeout = 10;
			int sleepTime = 1000;
			try {
				ret = talkWith(send,timeout,sleepTime);
			} catch (TimeoutException e) {
				Logger.error("pay TimeoutException. "+e.getMessage(),e);
				return RESULT.TIMEOUT;
			} catch (Exception e) {
				Logger.error("pay Exception. "+e.getMessage(),e);
				return RESULT.ERROR;
			}
			Logger.info("ret.length = "+ret.length);
			Logger.info("ret content(cost response:)"+ DeviceUtils.byteArray2HASCII(ret));
			if(ret == null || ret.length != 6 ){
				Logger.error("pay receive bytes is null or length !=6. data:"+Utils.toHex(ret));
				return RESULT.ERROR;
			}
			//判断头
			if(ret[0] != (byte)0x02){
				Logger.error("pay receive data STR is error. "+Utils.toHex1(ret[0])+"!=02");
				return RESULT.ERROR;
			}
			//判断尾
			if(ret[ret.length-2] != (byte)0x03){
				Logger.error("pay receive data EXT is error. "+Utils.toHex1(ret[ret.length-2])+"!=03");
				return RESULT.ERROR;
			}
			//判断LRC
			byte lrc = lrc(ret);
			if(ret[ret.length-1] != lrc){
				Logger.error("pay receive data LRC is error. "+Utils.toHex1(ret[ret.length-1])+"!="+Utils.toHex1(lrc));
				return RESULT.ERROR;
			}
			
			//判断RSP 
			if(ret[ret.length-3] != (byte)0x30){
				Logger.error("pay receive data RSP is error. "+Utils.toHex1(ret[ret.length-3])+"!=30");
				return RESULT.ERROR;
			}
			
			Logger.info("pay end. time:"+(System.currentTimeMillis()-startTime)/1000.00+"s");
			return RESULT.SUCCESS;
		} catch (Exception e) {
			Logger.error("pay is Exception. "+e.getMessage(),e);
			return RESULT.ERROR;
		}
		
	}
	
	/**
	 * 计算LRC
	 * @param cmd 命令指令 
	 */
	public static byte lrc(byte[] cmd){
		byte b = (byte)0x00;
		if(cmd.length<2){
			return b; 
		}
		b = cmd[1];
		for(int i=2,j=cmd.length-1;i<j;i++){
			b ^= cmd[i];
		}
		return b;
	}
	
	
	/**
	 * 
	 * 与设备通信
	 * @param cmd 发送给设备的命令
	 * @param timeout 指令超时（秒）
	 * @param sleepTime 延时（毫秒）
	 * @return 接收到的命令
	 * @throws Exception
	 */
	private byte[] talkWith(byte[] cmd,int timeout,int sleepTime) throws Exception {
		try {
			OutputStream os = com.getOutputStream();
			InputStream  is = com.getInputStream();
			
			// 通过串口发送数据给设备
			os.write(cmd);
			Logger.info("IO send: " + DeviceUtils.byteArray2HASCII(cmd));
			
			// 通过串口接收设备数据
			long startTime = System.currentTimeMillis();
			while(is.available() <= 0) {//此处修改把"<"改为"<="
				long endTime= System.currentTimeMillis();
				if(endTime-startTime >= timeout*1000){
					Logger.warn("IO read time out. ");
					throw new TimeoutException();
				}
			}
			
			Thread.sleep(sleepTime);
//			Logger.info("before write, is.available:"+is.available());
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			while(is.available() > 0){
				bao.write(is.read());
			}
			byte ret[] = bao.toByteArray();
//			Logger.info("after write, is.available():"+is.available());
			Logger.info("ret[] length = "+ret.length);
			Logger.info("IO receive: " + DeviceUtils.byteArray2HASCII(ret));
			return ret;
		} catch (Exception e) {
			Logger.error("talkWith is exception. "+e.getMessage(), e);
			throw e;
		}
	}
	
	private static class TimeoutException extends RuntimeException {
		private static final long serialVersionUID = -3132351707162593440L;
		
	}

}
