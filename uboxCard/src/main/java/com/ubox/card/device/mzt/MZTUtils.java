package com.ubox.card.device.mzt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;

import com.ubox.card.util.TimeUtil;
import com.ubox.card.util.device.DeviceByteBuffer;
import com.ubox.card.util.device.DeviceUtils;
import com.ubox.card.util.logger.Logger;

public class MZTUtils {
	
	/**
	 * 解压ZIP
	 * 
	 * @param zip zip压缩的数据流
	 * @return 解压后的数据流.解压失败,则返回NULL
	 */
	public static byte[] uncompressZIP(byte[] zip) {
		try {
			GZIPInputStream       gunzip = new GZIPInputStream(new ByteArrayInputStream(zip));
			ByteArrayOutputStream out    = new ByteArrayOutputStream();
			byte[]                buffer = new byte[256];
			
			int len;
			while((len = gunzip.read(buffer)) != -1) {
				out.write(buffer, 0, len);
			}
			
			return out.toByteArray();
		} catch (Exception e) {
			Logger.error("Uncompress zip error.", e);
			return null;
		}
	}
	
	public static String getDate() {
		String current = TimeUtil.getCurrentDate();
		return current.substring(0, 8);
	}

	public static String getTime() {
		String current = TimeUtil.getCurrentDate();
		return current.substring(8);
	}

	/**
	 * 生成A0命令,寻卡
	 * @return A0命令
	 */
	public static byte[] genA0() {
		byte[] cmd = {
				0x10, 0x02,	/* HEAD */
		  (byte)0xA0, 0x00,	/* TEXT */
				0x00,		/* BCC  */
				0x10,0x03 	/* TAIL */
		};		
		
		return genCommon(cmd);
	}
	
	/**
	 * 生成A1命令,读卡
	 * @return A1命令
	 */
	public static byte[] genA1() {
		byte[] cmd = {
				0x10, 0x02,
		  (byte)0xA1, 0x00,
		  	 	0x00,
		  	 	0x10, 0x03
		};
		
		return genCommon(cmd);
	}
	
	/**
	 * 生成A2命令,脱机消费
	 * @param money 
	 * @param cardNO 
	 * @return A2命令
	 */
	public static byte[] genA2(int money, String cardNO) {
		byte[] cmd = {
				0x10, 0x02, //头
		  (byte)0xA2,       //指令
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //用户卡号
				0x00, 0x00, 0x00, 0x00, //交易金额
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //交易日期时间
				0x00, 		//bcc
				0x10, 0x03 	//尾
		};
		byte[] number = DeviceUtils.hASCII2ByteArray(cardNO);
		byte[] cost   = DeviceUtils.i2bLt(money, 4);
		byte[] date   = DeviceUtils.hASCII2ByteArray(TimeUtil.getCurrentDate());
		
		System.arraycopy(number, 0, cmd, 3, 8);
		System.arraycopy(cost, 0, cmd, 11, 4);
		System.arraycopy(date, 0, cmd, 15, 7);
		
		return genCommon(cmd);
	}
	
	/**
	 * 生成A5命令,查询TAC
	 * @param cardTradeCount 卡交易计数
	 * @return A5命令
	 */
	public static byte[] genA5(byte[] cardTradeCount) {
		byte[] cmd = {
				0x10, 0x02,
		  (byte)0xA5,		//指令
		  	    0x00, 0x00,	//卡交易计数器
				0x00,		//消费模式
				0x00,		//bcc
				0x10, 0x03
		};
		
		cmd[3] = cardTradeCount[0];
		cmd[4] = cardTradeCount[1];
		
		return genCommon(cmd);
	}
	
	private static byte[] genCommon(byte[] cmd) {
		cmd[cmd.length - 3] = MZTUtils.calcBCC(cmd);
		cmd = MZTUtils.add10(cmd); //处理TEXT+BCC中的0x10
		
		return cmd;
	}
	
	private static final byte DLE = (byte)0x10;
	private static final byte STX = 0x02;
	private static final byte ETX = 0x03;
	
	/**
	 * 梳理设备返回的数据流
	 * @param cmd 命令数据流
	 * @param type 命令类型
	 * @return 梳理结果,如果失败则返回NULL
	 */
	public static byte[] combingCMD(byte[] cmd) {
		/* * * * 校验命令头  * * * * * */
		if(cmd[0] != DLE) {
			Logger.error("HEAD: check DLE fail");
			return null;
		}
		if(cmd[1] != STX) {
			Logger.error("HEAD: check STX error");
			return null;
		}
		
		/* * * * 校验命令尾  * * * * * */
		if(cmd[cmd.length - 1] != ETX) {
			Logger.error("TAIL: check EXT fail");
			return null;
		}
		if(cmd[cmd.length - 2] != DLE) {
			Logger.error("TAIL: check DLE fail");
			return null;
		}
		
		/* * * * 数据域去除多余0x10 * * * * * */
		DeviceByteBuffer buffer = new DeviceByteBuffer();
		
		int idx = 0;
		buffer.append(cmd[idx ++]);
		buffer.append(cmd[idx ++]);
		for(int len = cmd.length - 2; idx < len; idx ++) {
			buffer.append(cmd[idx]);
			
			if(cmd[idx] == DLE) {
				idx ++;
			}
		}
		buffer.append(cmd[idx ++]);
		buffer.append(cmd[idx ++]);
		
		/* * * * * * * BCC码验证 * * * * * */
		byte[] comb = buffer.toByteArray();
		byte   bcc  = calcBCC(comb);
		if(bcc != comb[comb.length - 3]) {
			Logger.error("check BCC fail.comb=" + DeviceUtils.byteArray2HASCII(comb));
			return null;
		}
		
		return comb;
	}
	
	/**
	 * 计算BCC的值
	 * @param data 数据域
	 * @return 计算结果
	 */
	private static byte calcBCC(byte[] data) {
		byte tmp = data[2];
		for(int i = 3, j = data.length - 3; i < j; i++){
			tmp ^= data[i];
		}

		return tmp;
	}
	
	/**
	 * 在DLE后面再添加0x10
	 * @param cmd 通信数据
	 * @return 添加结果
	 */
	private static byte[] add10(byte[] cmd) {
		DeviceByteBuffer buffer = new DeviceByteBuffer();
		int 		start = 2;
		int 		end   = cmd.length - 2;
		
		buffer.append(cmd[0]);
		buffer.append(cmd[1]);
		
		for(int i = start; i < end; i++) {
			buffer.append(cmd[i]);
			if(cmd[i] == DLE) {
				buffer.append(0x10);
			}
		}
		
		buffer.append(cmd[cmd.length - 2]);
		buffer.append(cmd[cmd.length -1]);
		
		return buffer.toByteArray();
	}

}
