package com.ubox.card.device.mzt;

import com.ubox.card.core.serial.RS232Worker;
import com.ubox.card.util.device.DeviceByteBuffer;
import com.ubox.card.util.device.DeviceUtils;
import com.ubox.card.util.logger.Logger;

public class MZTSerial extends RS232Worker {
	
	public MZTSerial(int baudRate, int dataBits, int stopBits, int parity) {
		super(baudRate, dataBits, stopBits, parity);
	}

	private final DeviceByteBuffer byteBuffer = new DeviceByteBuffer();
	
	@Override
	public byte[] read(int timeout) {
		try {
			int wait_time = 0;
			while (true) {
				if (in.available() > 0) {
					break; 
				}

				try { 
					Thread.sleep(100); 
					wait_time += 100;
				} catch (InterruptedException e) { 
					Logger.warn("WARN: Sleep Interrupt."); 
				}
				
				if (wait_time > timeout) {
					return null;
				}
			}
			
			byteBuffer.reset();
			int data = -1;
            while((data = in.read()) != -1){
            	byteBuffer.append(data);
            	if(data == 0x03) { 
            		break; 
            	}
            }

            byte[] rs = byteBuffer.toByteArray();
            Logger.info("RS232 read: " + DeviceUtils.byteArray2HASCII(rs));
            
            return rs;
		} catch(Exception e) {
			Logger.error("SerialPort Read error", e);
			return null;
	 	} 
	}

}
