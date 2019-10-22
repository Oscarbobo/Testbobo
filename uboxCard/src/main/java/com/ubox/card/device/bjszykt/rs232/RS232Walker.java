package com.ubox.card.device.bjszykt.rs232;

import java.io.ByteArrayOutputStream;

import com.ubox.card.core.serial.RS232Worker;
import com.ubox.card.util.device.DeviceUtils;
import com.ubox.card.util.logger.Logger;

public class RS232Walker extends RS232Worker{
    
	
    public RS232Walker(String com) {
        this(com, 9600);
    }

    public RS232Walker(String com, int rate) {
        this(com, rate, RS232Worker.DATABITS_8, RS232Worker.STOPBITS_1, RS232Worker.PARITY_EVEN);
    }

    public RS232Walker(String com, int rate, int databits, int stopbits, int parity) {
        super(rate, databits, stopbits, parity);

    }

	@Override
	public byte[] read(int timeout) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			int wait_time = 0;
			while (true) {
				if (in.available() > 0)  break; 

				try { Thread.sleep(50); } catch (InterruptedException e) { }
				wait_time += 50;
				
				if (wait_time > timeout) {
					Logger.error("read io data is timeout. "+wait_time+">"+timeout);
					return null;
				}
			}
			bos.reset();
			
			Thread.sleep(1000);
			
			int data = -1;
            while((data = in.read()) != -1){
            	bos.write(data);
            	if(data == 0x03) break;
            }
            bos.flush();
            byte[] rs = bos.toByteArray();
            Logger.info("RS232 read: " + DeviceUtils.byteArray2HASCII(rs));
            
            return rs;
		} catch(Exception e) {
			Logger.error("SerialPort Read error", e);
			return null;
	 	}finally{
	 		try {
				bos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
	 		
	 	} 
	}
}

