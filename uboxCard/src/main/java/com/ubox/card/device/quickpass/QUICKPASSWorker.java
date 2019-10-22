package com.ubox.card.device.quickpass;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;
import com.onecomm.serialport.IcCardSerialport;
import com.onecomm.serialport.Utils;
import com.ubox.card.core.serial.IcCom;
import com.ubox.card.util.logger.Logger;

public class QUICKPASSWorker extends IcCardSerialport {

	IcCom com;
	
	public QUICKPASSWorker(){
		if(com == null) {
			com = new IcCom(9600, IcCom.DATABITS_8, IcCom.STOPBITS_1, IcCom.PARITY_NONE);
		}
	}
	@Override
	public void closeSerialport() {
		Logger.info("-----------------------------------关闭串口");
		com.close();
	}

	@Override
	public InputStream getInputStream() {
		return com.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() {
		return com.getOutputStream();
	}

	@Override
	public void openSerialport() {
		Logger.info("-----------------------------------走到打开串口方法1>>>>>");
		com.open();
		Logger.info("-----------------------------------走完打开串口方法2>>>>>");

	}
}
