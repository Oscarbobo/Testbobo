package com.ubox.card.device.amt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ubox.card.util.logger.Logger;

public class IcComImpl {

	public static final String CARD_COM  = "/dev/ttyO4";
	public static final int  DATABITS_8  = 8;
	public static final int  DATABITS_7  = 7;
	public static final int  STOPBITS_2  = 2;
	public static final int  STOPBITS_1  = 1;
	public static final int  PARITY_NONE = 'n';
	public static final int  PARITY_ODD  = 'o';
	public static final int  PARITY_EVEN = 'e';
	
	private android_serialport_api.SerialPort serialPort;
	private InputStream  in;
	private OutputStream out;
	
	// 串口设置
	private final int baudRate;
	private final int dataBits;
	private final int stopBits;
	private final int parity;
	
    public IcComImpl() {
    	baudRate = 9600;
    	dataBits = DATABITS_8;
    	stopBits = STOPBITS_1;
    	parity   = PARITY_NONE;
    }
    
	void open() {
		try {
			serialPort = new android_serialport_api.SerialPort(new File(CARD_COM), baudRate, 0, dataBits, stopBits, parity);
			in         = serialPort.getInputStream();
			out        = serialPort.getOutputStream();
		} catch (SecurityException e) {
			Logger.error(e.getLocalizedMessage(), e);
		} catch (IOException e) {
			Logger.error(e.getLocalizedMessage(), e);
		}
	}

	void close() {
		try {
			if(out != null)
				out.close();
			if(in != null)
				in.close();
			if(serialPort != null)
				serialPort.close();
		} catch(IOException e) {
			Logger.error(e.getLocalizedMessage(), e);
		} catch(Throwable e) {
			Logger.error(e.getLocalizedMessage(), e);
		}
	}

	InputStream getInputStream() {
		return in;
	}

	OutputStream getOutputStream() {
		return out;
	}
}
