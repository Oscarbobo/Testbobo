package com.ubox.card.core.serial;

import com.ubox.card.util.logger.Logger;

/**
 * Duolne,负责与串口通信的小精灵
 */
public class SerialDuolne {
	
	private final RS232Worker worker;
	
	public SerialDuolne(RS232Worker worker) {
		this.worker = worker;
	}
	
	/**
	 * Duolnez在喊话,与串口设备通信
	 * 
	 * @param content 通信内容
	 * @param timeout 等待回话时间
	 * @return 设备回话内容,NULL表示失败 
	 */
	public byte[] zipa(byte[] content, int timeout) {
        if(RS232Worker.WORK_SUCCESS != worker.open()) {
        	return null;
        }
        if(RS232Worker.WORK_SUCCESS != worker.write(content)) {
        	return null;
        }
        
        byte[] r = worker.read(timeout);
        
        if(RS232Worker.WORK_SUCCESS != worker.close()) {
        	Logger.warn("RS232 close fail");
        }
        
        return r;
	}
}
