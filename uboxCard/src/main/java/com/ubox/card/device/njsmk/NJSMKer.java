package com.ubox.card.device.njsmk;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

import android.annotation.SuppressLint;

import com.ubox.card.core.serial.IcCom;
import com.ubox.card.util.Utils;
import com.ubox.card.util.device.DeviceUtils;
import com.ubox.card.util.logger.Logger;

public class NJSMKer {

	private static byte[] SEND = new byte[] {
		0x7E, // 报文头
		0x10, // 报文长度
		0x00, (byte)0xFE, (byte)0xC2, // 报文固定
		
		0x30, 0x30, 0x30, 0x30, 0x30, 0x31, 	  // 消费金额
		0x20, 0x15, 0x03, 0x31, 0x18, 0x25, 0x16, // 消费时间
		
		0x22,      // LRC1 
		(byte)0xD0 // LRC2
	};
	
	static enum RESULT { SUCCESS, CANCEL, TIMEOUT, ERROR };
	
	private IcCom com = new IcCom(115200);
	
	
	private String code; 
	private String type; 
	private String balance; 
	private String time; 
	private String seq;
	private String company;
	private String webNode; 
	private String termNO; 
	private String cardNO;
	private String cardSer;
	
	/**
	 * 扣款
	 * @param money 金额,单位:分
	 * @return 扣款结果
	 */
	public RESULT cost(int money,String serNo) {
		byte[] cmd = genCmd(money);
		
		/* 与刷卡设备交互信息  */
		byte[] ret;
		try {
			ret = talkWith(cmd,serNo);
		} catch (TimeoutException e) {
			Logger.error("cost TimeoutException. "+e.getMessage(),e);
			return RESULT.TIMEOUT;
		} catch (CancelException e) {
			Logger.error("cost CancelException. "+e.getMessage(),e);
			return RESULT.CANCEL;
		} catch (Exception e) {
			Logger.error("cost Exception. "+e.getMessage(),e);
			return RESULT.ERROR;
		}
		
		try {
			/*
			 * 判断数据格式
			 */
			//判断头
			if(ret[0]!=(byte)0x7E){
				Logger.error("STR is error! "+Utils.toHex1(ret[0])+"!=7E");
				return RESULT.ERROR;
			}
			//判断LRC1
			byte lrc1 = calcLRC1(ret);
			if(ret[ret.length-2]!=lrc1){
				Logger.error("LRC1 is error! "+Utils.toHex1(ret[ret.length-2])+"!="+Utils.toHex1(lrc1));
				return RESULT.ERROR;
			}
			//判断LRC2
			byte lrc2 = calcLRC2(ret);
			if(ret[ret.length-1]!=lrc2){
				Logger.error("LRC2 is error! "+Utils.toHex1(ret[ret.length-1])+"!="+Utils.toHex1(lrc2));
				return RESULT.ERROR;
			}
			
			/* 解析设备反馈信息 */
			byte[] cCode  	= Arrays.copyOfRange(ret, 5, 9);
			code    = new String(cCode);
			if(!"0000".equals(code)){
				return RESULT.ERROR;
			}
			
			byte   ctype    = ret[9];
			byte[] cBalance = Arrays.copyOfRange(ret, 10, 18);
			byte[] cTime    = Arrays.copyOfRange(ret, 18, 32);
			byte[] cSeq     = Arrays.copyOfRange(ret, 32, 40);
			byte[] cCompany = Arrays.copyOfRange(ret, 40, 52);
			byte[] cWebNode = Arrays.copyOfRange(ret, 52, 60);
			byte[] cTermNO  = Arrays.copyOfRange(ret, 60, 68);
			byte[] cCardNO  = Arrays.copyOfRange(ret, 68, 84);
			byte[] cCardSer = Arrays.copyOfRange(ret, 84, 100);
			
			type    = Utils.toHex1(ctype);
			balance = new String(cBalance);
			time    = new String(cTime);
			seq     = new String(cSeq);
			company = new String(cCompany);
			webNode = new String(cWebNode);
			termNO  = new String(cTermNO);
			cardNO  = new String(cCardNO);
			cardSer = new String(cCardSer);
			
		} catch (Exception e) {
			Logger.error("cost Exception. "+e.getMessage(),e);
			return RESULT.ERROR;
		}
		
		Logger.info(String.format("cost response object = code:%s,type:%s,balance:%s,time:%s,seq:%s,company:%s,webNode:%s,termNO:%s,cardNO:%s,cardSer:%s",code,type,balance,time ,seq  ,company,webNode,termNO ,cardNO ,cardSer ));
		return RESULT.SUCCESS;
	}
	
	/**
	 * 生成消费命令
	 * @param money 
	 * @return 消费命令
	 */
	private byte[] genCmd(int money) {
		byte[] cmd  = Arrays.copyOf(SEND, SEND.length);
		byte[] csm  = Utils.intToHASCIIByte(6, money);//transMoney(money);
		byte[] date = transTime();
		
		System.arraycopy(csm, 0, cmd, 5, 6);
		System.arraycopy(date, 0, cmd, 11, 7);
		
		cmd[cmd.length - 2] = calcLRC1(cmd);
		cmd[cmd.length - 1] = calcLRC2(cmd);
		
		return cmd;
	}
	
	/**
	 * 计算LRC1
	 * @param cmd 命令指令 
	 */
	private byte calcLRC1(byte[] cmd) {
		byte lrc = cmd[2];
		
		int st  = 3;
		int ed  = cmd.length - 2;
		
		for(; st < ed; ++st) {
			lrc ^= cmd[st];
		}
		lrc ^= 0x33;
		
		//cmd[cmd.length - 2] = lrc;
		return lrc;
	}
	
	/**
	 * 计算LRC2
	 * @param cmd 命令指令
	 */
	private byte calcLRC2(byte[] cmd) {
		byte lrc = cmd[2];
		
		int st = 3;
		int ed = cmd.length - 2;
		
		for(; st < ed; ++st) {
			lrc += cmd[st];
		}
		lrc += 0x33;
		
		return lrc;
	}
	
	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	
	/**
	 * 交易时间转换成H-ASCII
	 * @return H-ASCII
	 */
	private byte[] transTime() {
		String ds = sdf.format(new Date());
		return DeviceUtils.hASCII2ByteArray(ds);
	}
	
//	/**
//	 * 刷卡取消
//	 * @return true-刷卡取消,false-刷卡正常进行
//	 * @throws Exception 
//	 */
//	private boolean doCancel(String serNo) {
////		return CmnMessageBuffer.clearCancelMsg() != null;
//		return Utils.isCancel(serNo);
//	}
	
	/**
	 * 与设备通信
	 * @param cmd 发送给设备的命令
	 * @return 接收到的命令
	 */
	private byte[] talkWith(byte[] cmd,String serNo) throws Exception {
		try {
			com.open();
			
			OutputStream os = com.getOutputStream();
			InputStream  is = com.getInputStream();
			
			// 通过串口发送数据给设备
			os.write(cmd);
			Logger.info("IO send: " + DeviceUtils.byteArray2HASCII(cmd));
			
			// 通过串口接收设备数据
			long startTime = System.currentTimeMillis();
			int lenData = 0;
			while(true) {
				lenData = is.available();
				if(lenData > 0){
					Logger.info("lenData:"+lenData);
					break;
				}
				
				long endTime= System.currentTimeMillis();
				if(endTime-startTime >= 20*1000){
					com.close();
					Logger.warn("IO read time out. ");
					throw new TimeoutException();
				}
				
			}
			
			Thread.sleep(1000);
			
			StringBuffer strB = new StringBuffer();
			int blockType = 0;
			lenData = is.available();
			while(lenData > 0){
				blockType = is.read();
				lenData --;
				if(blockType!=(byte)0x7E){
					strB.append(Utils.toHex1(Utils.makeByte1(blockType)));
					Logger.info("blockType:"+Utils.makeByte1(blockType));
				}else{
					Logger.info("7E before receive:"+strB);
					break;
				}
			}
			
//			Thread.sleep(500);
			
			if(lenData<=0){
				Logger.error("7E is not exist. receive:"+strB);
				byte[] b = {(byte)(blockType & 0xff)};
				return b;
			}
			
			int len       = is.read();
			byte[] ret    = new byte[len + 4];
			
			ret[0] = (byte)(blockType & 0xff);
			ret[1] = (byte)(len & 0xff);
			
			for(int i = 0; i < len; ++i){
				ret[i+2] = (byte)(is.read() & 0xff);
			}
			
			ret[ret.length-2] = (byte)(is.read() & 0xff);
			ret[ret.length-1] = (byte)(is.read() & 0xff);
			
			com.close();
			Logger.info("IO receive: " + DeviceUtils.byteArray2HASCII(ret));
			return ret;
		} catch (Exception e) {
			Logger.error("talkWith is exception. "+e.getMessage(), e);
			throw e;
		}
	}
	
	private static class CancelException extends RuntimeException {
		private static final long serialVersionUID = -8083411630408995054L;
	}
	
	private static class TimeoutException extends RuntimeException {
		private static final long serialVersionUID = -3132351707162593440L;
		
	}

	String getCode() {
		return code;
	}

	String getType() {
		return type;
	}

	String getBalance() {
		return balance;
	}

	String getTime() {
		return time;
	}

	String getSeq() {
		return seq;
	}

	String getCompany() {
		return company;
	}

	String getWebNode() {
		return webNode;
	}

	String getTermNO() {
		return termNO;
	}

	String getCardNO() {
		return cardNO;
	}

	String getCardSer() {
		return cardSer;
	}
	
	public String toString(){
		return ToStringBuilder.reflectionToString(this);
	}
}
