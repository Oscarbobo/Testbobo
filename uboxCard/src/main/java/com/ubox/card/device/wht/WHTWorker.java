/**
 * 
 */
package com.ubox.card.device.wht;

import com.alibaba.fastjson.JSON;
import com.ubox.card.config.CardJson;
import com.ubox.card.core.serial.IcCom;
import com.ubox.card.util.Utils;
import com.ubox.card.util.device.DeviceUtils;
import com.ubox.card.util.logger.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author gaolei
 * @version 2015年4月22日
 * 
 */
public class WHTWorker {
	private IcCom com = new IcCom(38400,IcCom.DATABITS_8,IcCom.STOPBITS_1,IcCom.PARITY_NONE);
	
	static final int SUCCESS = 0;
	static final int FAIL    = 1;
	static final int CANCEL  = 2;
	public static final int LENGTH_TEST = 12;// 通信测试接口返回的数据长度
	public static final int LENGTH_READCARD = 64;// 读卡信息接口返回的数据长度
	public static final int LENGTH_COST = 64;// 武汉通消费接口返回的数据长度
	public static byte COMMAND_COMMUNI_TEST = (byte) 0x01;// 接收数据命令码：通信测试
	public static byte COMMAND_READ_CARD = (byte) 0x02;// 接收数据命令码：读卡信息
	public static byte COMMAND_COST = (byte) 0x03;// 接收数据命令码：武汉通消费、
	
	public void open(){
		try {
			com.open();
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}
	
	public void close(){
		try {
			com.close();
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 测试设备与中控的连通性
	 * @return 0-测试成功;1-测试失败 2-撤销
	 */
	int testComm(String serNo) {
		try {
			if(doCancel(serNo)) {
				Logger.info("Cancel testComm");
				return CANCEL;
			}
			// 发送通信测试指令
			byte[] sendBuffer = { (byte) 0x01, (byte) 0x00, (byte) 0x08,
					(byte) 0x16, (byte) 0x12, (byte) 0x17, (byte) 0x11,
					(byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x00 };
			
			byte crc8 = WhtUtils.getCrc8(sendBuffer, sendBuffer.length - 1);
			sendBuffer[sendBuffer.length - 1] = crc8;
			
			Logger.info(">>>> testComm send:" + DeviceUtils.byteArray2HASCII(sendBuffer));
	        
			com.getOutputStream().write(sendBuffer);
	        
	        Thread.sleep(100);
	        
	        //接受数据
	        int timeout = 1000;
	        boolean needCancel = true;
	        byte[] revBuffer = readIO(timeout,needCancel,serNo);
	        Logger.info("<<<< testComm receive:" + DeviceUtils.byteArray2HASCII(revBuffer));
	        
	        //校验数据长度正确性,前三个字节接收命令的正确性
			if (revBuffer.length != LENGTH_TEST){
				Logger.error("receive data length is error. "+revBuffer.length +"!="+ LENGTH_TEST);
				return 1;
			}
			if (revBuffer[0] != COMMAND_COMMUNI_TEST){
				Logger.error("receive data STX is error. "+DeviceUtils.byte2HASCII(revBuffer[0]) +"!="+ DeviceUtils.byte2HASCII(COMMAND_COMMUNI_TEST));
				return 1;
			}
			if (revBuffer[1] != 0x00 || revBuffer[2] != 0x08){
				Logger.error("receive data length is error. "+DeviceUtils.byte2HASCII(revBuffer[1]) +"!=0x00 || 0x08!="+ DeviceUtils.byte2HASCII(revBuffer[2]));
				return 1;
			}
			
		
			crc8 = WhtUtils.getCrc8(revBuffer, revBuffer.length - 1);// crc8校验
			if (crc8 != revBuffer[revBuffer.length - 1]) {// 判断校验码
				Logger.error("receive data crc8 is error. "+DeviceUtils.byte2HASCII(crc8) +"!="+ DeviceUtils.byte2HASCII(revBuffer[revBuffer.length - 1]));
				return 1;

			}

			revBuffer = null;
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			return 1;
		}
		return 0;
	}
	
	/**
	 * 读取卡信息
	 * @return
	 */
	String[] read(String serNo) {
		long currentTime = System.currentTimeMillis();// 当前时间

		String[] cardMessage = {"10",""};
		
		try {
			
			if(doCancel(serNo)) {
				Logger.info("Cancel testComm");
				cardMessage[0] = "11";//撤销
				return cardMessage;
			}
			
			
			String vmId = "000000" + CardJson.vmId;
			String subStr = vmId.substring(vmId.length() - 6, vmId.length());
			byte seq = Utils.makeByte1(Integer.parseInt(subStr.substring(0, 2)));
			byte seq1 = Utils.makeByte1(Integer.parseInt(subStr.substring(2, 4)));
			byte seq2 = Utils.makeByte1(Integer.parseInt(subStr.substring(4, 6)));
			
			byte[] oper = Utils.makeByte3(1);// 3字节操作员号
			byte[] dateTime = WhtUtils.isCurrDay();// 7字节的时间
			
			String dayTime = "9000";// 三字节等待时间
			byte[] waitTime = Utils.makeByte3(Integer.valueOf(dayTime).intValue());
	
			byte[] sendBuffer = { (byte) 0x02, (byte) 0x00, (byte) 0x10, seq, seq1,
					seq2, oper[0], oper[1], oper[2], dateTime[0], dateTime[1],
					dateTime[2], dateTime[3], dateTime[4], dateTime[5],
					dateTime[6], waitTime[0], waitTime[1], waitTime[2], (byte) 0x00 };
			
			byte crc8 = WhtUtils.getCrc8(sendBuffer, sendBuffer.length - 1);
			sendBuffer[sendBuffer.length - 1] = crc8;
			
			Logger.info("<<<< readCard send=" + Utils.toHex(sendBuffer));
			com.getOutputStream().write(sendBuffer);
			
			Thread.sleep(100);// 线程休眠
	
			//接受数据
	        int timeout = 1000;
	        boolean needCancel = true;
	        byte[] revBuffer = readIO(timeout,needCancel,serNo);
	        Logger.info("<<<< readCard receive:" + Utils.toHex(revBuffer));
	       
	        //校验数据长度正确性,前三个字节接收命令的正确性
			if (revBuffer.length != LENGTH_READCARD){
				Logger.error("receive data length is error. "+revBuffer.length +"!="+ LENGTH_READCARD);
				return cardMessage;
			}
			if (revBuffer[0] != COMMAND_READ_CARD){
				Logger.error("receive data STX is error. "+DeviceUtils.byte2HASCII(revBuffer[0]) +"!="+ DeviceUtils.byte2HASCII(COMMAND_READ_CARD));
				return cardMessage;
			}
			if (revBuffer[1] != 0x00 || revBuffer[2] != 0x3C){
				Logger.error("receive data length is error. "+DeviceUtils.byte2HASCII(revBuffer[1]) +"!=00 || 3C!="+ DeviceUtils.byte2HASCII(revBuffer[2]));
				return cardMessage;
			}
			
			crc8 = WhtUtils.getCrc8(revBuffer, revBuffer.length - 3);// crc8校验
			if (crc8 != revBuffer[revBuffer.length - 1]) {// 判断校验码
				Logger.error("receive data crc8 is error. "+DeviceUtils.byte2HASCII(crc8) +"!="+ DeviceUtils.byte2HASCII(revBuffer[revBuffer.length - 1]));
				return cardMessage;
	
			}
			
			cardMessage[0] = ""+Utils.makeUint8(revBuffer[61]);// 读卡返回的状态0
	
			byte[] distributionCard = { revBuffer[15],
					revBuffer[16], // 发行卡号
					revBuffer[17], revBuffer[18], revBuffer[19],
					revBuffer[20], revBuffer[21], revBuffer[22] };
			cardMessage[1] = Utils.toHex(distributionCard);
	
			Logger.info("readCard end. time=" + (System.currentTimeMillis() - currentTime));
			
			revBuffer = null;
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
		return cardMessage;
	}
	
	/**
	 * 扣款
	 * @param money 扣款金额,单位:分
	 * @return
	 */
	Object[] cost(int money,String serNo) {
		Object [] status = { 10, 0, "", "", 0, "","","",0,0,"",""};

		try {
			if(doCancel(serNo)) {
				Logger.info("Cancel testComm");
				status[0] = 11;//撤销
				return status;
			}
			
			String vmId = "000000" + CardJson.vmId;
			String subStr = vmId.substring(vmId.length()-6, vmId.length());
			byte seq = Utils.makeByte1(Integer.parseInt(subStr.substring(0, 2)));
			byte seq1 = Utils.makeByte1(Integer.parseInt(subStr.substring(2, 4)));
			byte seq2 = Utils.makeByte1(Integer.parseInt(subStr.substring(4, 6)));
			
			
			byte[] oper = Utils.makeByte3(1);// 3字节操作员号
			
			byte[] dateTime = WhtUtils.isCurrDay();// 7字节的时间
			
			String dayTime = "9000";// 三字节等待时间
			byte[] waitTime = Utils.makeByte3(Integer.valueOf(dayTime).intValue());
			byte[] price = Utils.makeByte4(money);// 交易金额
			
			byte[] sendBuffer = { (byte) 0x03, (byte) 0x00, (byte) 0x14, seq, seq1,
					seq2, oper[0], oper[1], oper[2], dateTime[0], dateTime[1],
					dateTime[2], dateTime[3], dateTime[4], dateTime[5],
					dateTime[6], price[0], price[1], price[2], price[3],
					waitTime[0], waitTime[1], waitTime[2], (byte) 0x00 };
			
			byte crc8 = WhtUtils.getCrc8(sendBuffer, sendBuffer.length - 1);
			sendBuffer[sendBuffer.length - 1] = crc8;
			
			Logger.info(">>>> cost send:" + Utils.toHex(sendBuffer));
			
			com.getOutputStream().write(sendBuffer);// 扣款指令
	
			// 当前时间
			long currentTime = System.currentTimeMillis();
	
			Thread.sleep(100);// 线程休眠，为了给刷卡机反应的时间
			
			//接受数据
	        int timeout = 1000;
	        boolean needCancel = true;
	        byte[] revBuffer = readIO(timeout,needCancel,serNo);
	        Logger.info("<<<< cost receive:" + Utils.toHex(revBuffer));
	        
				
	        //校验数据长度正确性,前三个字节接收命令的正确性
	      	if (revBuffer.length != LENGTH_COST){
	  			Logger.error("receive data length is error. "+revBuffer.length +"!="+ LENGTH_COST);
	  			return status;
	  		}
	  		if (revBuffer[0] != COMMAND_COST){
	  			Logger.error("receive data STX is error. "+DeviceUtils.byte2HASCII(revBuffer[0]) +"!="+ DeviceUtils.byte2HASCII(COMMAND_COST));
	  			return status;
	  		}
	  		if (revBuffer[1] != 0x00 || revBuffer[2] != 0x3c){
	  			Logger.error("receive data length is error. "+DeviceUtils.byte2HASCII(revBuffer[1]) +"!=00 || 3C!="+ DeviceUtils.byte2HASCII(revBuffer[2]));
	  			return status;
	  		}
	  	
	  		crc8 = WhtUtils.getCrc8(revBuffer, revBuffer.length - 3);// crc8校验
	  		if (crc8 != revBuffer[revBuffer.length - 1]) {// 判断校验码
	  			Logger.error("receive data crc8 is error. "+DeviceUtils.byte2HASCII(crc8) +"!="+ DeviceUtils.byte2HASCII(revBuffer[revBuffer.length - 1]));
	  			return status;
	
	  		}
	  		
			status[0] = Utils.makeUint8(revBuffer[61]);
	
			if ((Integer) status[0] == 0 || (Integer) status[0] == 7) {
				verifyCost();
	
				status[1] = Utils.makeUint32(revBuffer[33],// 读卡返回的余额1
						revBuffer[34], revBuffer[35], revBuffer[36]);
	
				byte[] distributionCard = { revBuffer[15],
						revBuffer[16], revBuffer[17], revBuffer[18],
						revBuffer[19], revBuffer[20], revBuffer[21],
						revBuffer[22] };
				status[2] = Utils.toHex(distributionCard);// 发行卡号
	
				byte[] phyCardNo = { revBuffer[23], revBuffer[24],
						revBuffer[25], revBuffer[26] };
				status[3] = Utils.toHex(phyCardNo);// 物理卡号
	
				status[4] = Utils.makeUint3(revBuffer[3], revBuffer[4],
						revBuffer[5]);
	
				byte[] psamCard = { revBuffer[9], revBuffer[10],
						revBuffer[11], revBuffer[12], revBuffer[13],
						revBuffer[14] };
				status[5] = Utils.toHex(psamCard);// psam卡号
	
				byte[] cmain = { revBuffer[27] };
				status[6] = Utils.toHex(cmain);// 1字节的主卡类型
	
				byte[] smain = { revBuffer[28] };
				status[7] = Utils.toHex(smain);// 1字节的子卡类型
	
				status[8] = Utils.makeUint32(revBuffer[29],
						revBuffer[30], revBuffer[31], revBuffer[32]);// 4字节用户卡计数器
	
				status[9] = Utils.makeUint32(revBuffer[50],
						revBuffer[51], revBuffer[52], revBuffer[53]);// 4字节的交易流水
	
				byte[] tac = { revBuffer[54], revBuffer[55],
						revBuffer[56], revBuffer[57] };
				status[10] = Utils.toHex(tac);// 4字节的TAC
	
				byte[] tradeTime = { revBuffer[43], revBuffer[44],
						revBuffer[45], revBuffer[46], revBuffer[47],
						revBuffer[48], revBuffer[49] };
				status[11] = Utils.toHex(tradeTime);// 取刷卡器的时间
	
			}
	
			Logger.info("cost end.time=" + (System.currentTimeMillis() - currentTime));
			revBuffer = null;
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
		Logger.info("<<<< cost response:"+JSON.toJSONString(status));
		return status;
	}
		
	public void verifyCost() {
		
		byte[] sendBuffer = { (byte) 0x14, (byte) 0x00, (byte) 0x0a,
				(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04,
				(byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08,
				(byte) 0x09,(byte) 0x10, (byte) 0x00 };//消息确认机制 
		
		byte crc8 = WhtUtils.getCrc8(sendBuffer, sendBuffer.length - 1);
		sendBuffer[sendBuffer.length - 1] = crc8;

		try {
			com.getOutputStream().write(sendBuffer);// 消息确认机制指令
		} catch (IOException e) {
			e.printStackTrace();
		}
		Logger.info(">>>>>verifyCost send=" + Utils.toHex(sendBuffer));
	}
	
	/**
	 * 刷卡取消
	 * @return true-刷卡取消,false-刷卡正常进行
	 * @throws Exception 
	 */
	private boolean doCancel(String serNo) {
		return Utils.isCancel(serNo);
	}
	
	/**
	 * 读取串口数据
	 * @param timeout    超时限制,单位ms
	 * @param needCancel 是否需要"取消功能"
	 * @return []-接收失败;非[]-接收成功
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	private byte[] readIO(int timeout, boolean needCancel,String serNo) throws IOException, InterruptedException {
        InputStream mIn = com.getInputStream();

        long startTime = System.currentTimeMillis();
        while(mIn.available() < 0) {//判断数据流里有多少个字节可以读取
            Thread.sleep(100);

            if(System.currentTimeMillis() - startTime > timeout) {
                Logger.error("Read time out.");
                return new byte[] {};
            }

            if(needCancel && doCancel(serNo)) {
                Logger.error("Read cancel.");
                return new byte[] {};
            }
        }

        int    stx = mIn.read();
        int len1 = mIn.read();
        int    len = mIn.read();
        byte[] ret = new byte[len + 4];

        ret[0] = (byte)(stx & 0xff);
        ret[1] = (byte)(len1 & 0xff);
        ret[2] = (byte)(len & 0xff);

        for(int i = 0; i < len; ++i){
            ret[i + 3] = (byte)(mIn.read() & 0xff);
        }

        ret[len + 3] = (byte)(mIn.read() & 0xff);

        return ret;
	}
}
