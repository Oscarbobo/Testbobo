package com.ubox.card.core.serial;

import com.ubox.card.util.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IcCom {

	public static final String CARD_COM  = "/dev/ttyO4";
	public static final int  DATABITS_8  = 8;
	public static final int  DATABITS_7  = 7;
	public static final int  STOPBITS_2  = 2;
	public static final int  STOPBITS_1  = 1;
	public static final int  PARITY_NONE = 'n';
	public static final int  PARITY_ODD  = 'o'; //奇校验
	public static final int  PARITY_EVEN = 'e'; //偶校验
	public static final int  PARITY_BIG_EVEN = 'E';
	
	private android_serialport_api.SerialPort serialPort;
	private InputStream  in;
	private OutputStream out;
	
	// 串口设置
	private final int baudRate;
	private final int dataBits;
	private final int stopBits;
	private final int parity;
	
    public IcCom() {
    	this.baudRate = 9600;
    	this.dataBits = DATABITS_8;
    	this.stopBits = STOPBITS_1;
    	this.parity   = PARITY_NONE;
    }
    
    public IcCom(int baudRate) {
    	this.baudRate = baudRate;
    	this.dataBits = DATABITS_8;
    	this.stopBits = STOPBITS_1;
    	this.parity   = PARITY_NONE;
    }
    
    public IcCom(int baudRate,int dataBits,int stopBits,int parity) {
    	this.baudRate = baudRate;
    	this.dataBits = dataBits;
    	this.stopBits = stopBits;
    	this.parity   = parity;
    }
    
	public void open() {
		try {
			serialPort = new android_serialport_api.SerialPort(new File(CARD_COM), baudRate, 0, dataBits, stopBits, parity);
			in         = serialPort.getInputStream();
			out        = serialPort.getOutputStream();
			Logger.info("-----------------------------------进入打开串口方法7>>>>>"+"openSerialPort CAED_COM:"+CARD_COM+" baudRate:"+baudRate +" in:"+in+" out:"+out);
		} catch (SecurityException e) {
			Logger.error(e.getLocalizedMessage(), e);
		} catch (IOException e) {
			Logger.error(e.getLocalizedMessage(), e);
		}
	}

	public void close() {
		try {
			if(out != null){
				out.close();
				out = null;
			}
			if(in != null) {
				in.close();
				in = null;
			}
			if(serialPort != null) {
				serialPort.close();
				serialPort = null;
			}
		} catch(IOException e) {
			Logger.error(e.getLocalizedMessage(), e);
		} catch(Throwable e) {
			Logger.error(e.getLocalizedMessage(), e);
		}
	}

	public InputStream getInputStream() {
		return in;
	}

	public OutputStream getOutputStream() {
		return out;
	}
}
