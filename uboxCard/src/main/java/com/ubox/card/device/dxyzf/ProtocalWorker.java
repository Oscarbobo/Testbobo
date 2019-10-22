package com.ubox.card.device.dxyzf;

import com.ubox.card.core.serial.RS232Worker;
import com.ubox.card.core.serial.SerialDuolne;
import com.ubox.card.util.device.DeviceByteBuffer;
import com.ubox.card.util.device.DeviceUtils;
import com.ubox.card.util.logger.Logger;

public class ProtocalWorker {
	
	private final SerialDuolne doulne = new SerialDuolne(new Worker());
	
	/**
	 * 查询设备
	 * @return true-设备查询成功,false-设备查询失败
	 */
	public boolean inquiry() {
		byte[] cmd = { 
				0x02, 
				0x01, (byte)0xA0,
		  (byte)0xA0,
				0x03 
		};
		
		byte[] re = doulne.zipa(cmd, 2000);
		int    ck = checkProtocal(re);
		if(ck == CHECK_SUCCESS) {
			return true;
		} else {
			Logger.error("CRC fail, code=" + ck);
			return false;
		}
	}
	
	/**
	 * 扣款准备,搜寻感应区卡片
	 * @return 寻卡结果
	 */
	public String prepare() {
		byte[] cmd = {
				0x02, 
				0x02, (byte) 0xA1, 0x00,
		  (byte)0xA1, 
		  		0x03 
		};
		
		byte[] re = doulne.zipa(cmd, 5000);
		int    ck = checkProtocal(re);
		if(ck != CHECK_SUCCESS) {
			Logger.error("CRC fail, code=" + ck);
			return DXYZFContext.FAIL;
		}
		
		if(re[2] == 0x01) { // 感应到卡片
			return DXYZFContext.SUCCESS;
		} else if(re[2] == 0x00) { // 没有感应到卡片
			return DXYZFContext.NO_CARD;
		} else {
			Logger.error("Unknow status: 0x" + DeviceUtils.byte2HASCII(re[2]));
			return DXYZFContext.FAIL;
		}
	}
	
	/**
	 * 扣款
	 * @param cmd 扣款命令
	 * @param timeout 超时时间
	 * @return 设备反馈结果.NULL表示设备异常
	 */
	public byte[] cost(byte[] cmd, int timeout) {
		byte[] re = doulne.zipa(cmd, timeout);
		int    ck = checkProtocal(re);
		
		if(ck != CHECK_SUCCESS) {
			Logger.error("CRC fail, code=" + ck);
			return null;
		}
		
		return re;
	}
	
	private final int CHECK_SUCCESS   = 0;
	private final int CHECK_FAIL_HEAD = 1;
	private final int CHECK_FAIL_TAIL = 2;
	private final int CHECK_FAIL_ORC  = 3;
	private final int CHECK_FAIL_LEN  = 4;
	
	/**
	 * 检查协议
	 * @param protocal 协议消息
	 * @return 检查结果
	 */
	private int checkProtocal(byte[] protocal) {
		if(protocal == null) {
			return CHECK_FAIL_TAIL;
		}
		
		int len = protocal.length;
		
		if(protocal[0] != 0x02) {
			return CHECK_FAIL_HEAD;
		}
		
		if(protocal[len - 1] != 0x03) {
			return CHECK_FAIL_TAIL;
		}
		
		/* * * * * * * * 异或校验    * * * * * * * */
		int dataLen = protocal[1] & 0xFF; // 数据长度
		if( dataLen != len - 4) {
			Logger.error("Length error");
			return CHECK_FAIL_LEN;
		}
		
		byte orc = protocal[2]; // 校验起始位
		for(int idx = 3, i = 1; i < dataLen; idx ++, i++) {
			orc ^= protocal[idx];
		}
		
		if(orc != protocal[len - 2]) {
			return CHECK_FAIL_ORC;
		}
		
		return CHECK_SUCCESS;
	}
	
	/**
	 * 与设备交互类
	 */
	private static class Worker extends RS232Worker {
		
		private final DeviceByteBuffer buffer = new DeviceByteBuffer();

		public Worker() {
			super(9600, RS232Worker.DATABITS_8, RS232Worker.STOPBITS_1, RS232Worker.PARITY_EVEN);
		}

		@Override
		public byte[] read(int timeout) {
			try {
				int time = 0;
				while(time < timeout) { // 等待,直到串口有数据反或者超时
					if(in.available() > 0) {
						Thread.sleep(200);
						break;
					} else {
						Thread.sleep(200);
						time += 200;
					}
				}
				
				if(time >= timeout) {
					Logger.warn("RS232 read timeout");
					return null;
				}
				
				byte[] buff = new byte[40];
				int rlen    = in.read(buff);
				
				buffer.reset();
				buffer.append(buff, 0, rlen);
				
				byte[] read = buffer.toByteArray();
				Logger.info("RS232 read: " + DeviceUtils.byteArray2HASCII(read));
				
				return read;
			} catch(Throwable tr) {
				Logger.error("RS232 read fail", tr);
				return null;
			}
		}
	}
}
