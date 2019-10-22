package com.ubox.card.device.ynylsf.rs232;

import com.ubox.card.core.serial.RS232Worker;
import com.ubox.card.util.device.DeviceUtils;
import com.ubox.card.util.logger.Logger;

public class YNYLSFSerial extends RS232Worker {
	
	public YNYLSFSerial(int baudRate, int dataBits, int stopBits, int parity) {
		super(baudRate, dataBits, stopBits, parity);
	}

	@Override
	public byte[] read(int timeout) {
		try {
			int waitTime = 0;
			while (in.available() < 0) {
				try { 
					Thread.sleep(100); 
				} catch (InterruptedException e) {
					Logger.warn(">>>>WARN: Sleep Interrupt."); 
				}
				
				waitTime += 100;
				
				if (waitTime > timeout) { 
					return new byte[] {};
				}
			}
			
            byte[] buf = new byte[3];
			// read STX
			buf[0] = (byte)in.read();
			// read LEN
			buf[1] = (byte)(in.read() & 0xff);
			buf[2] = (byte)(in.read() & 0xff);
			
			// read CONT
			int    len = ((buf[1] << 8) & 0xff00) + (buf[2] & 0xff);
            byte[] ret = new byte[len + 5];
            System.arraycopy(buf, 0, ret, 0, 3);
            
            for(int i = 0; i < len; ++i) {
            	ret[i + 3] = (byte)(in.read() & 0xff);
            }
            
            ret[len + 3] = (byte)(in.read() & 0xff); 
            ret[len + 4] = (byte)(in.read() & 0xff);
            
            Logger.info("RS232 read: " + DeviceUtils.byteArray2HASCII(ret));
            
            return ret;
		} catch(Exception e) {
			Logger.error("SerialPort Read error", e);
			return null;
	 	} 
	}

}
