package com.ubox.card.core.serial;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ubox.card.util.device.DeviceUtils;
import com.ubox.card.util.logger.Logger;

public abstract class RS232Worker {
	
	public static final int WORK_SUCCESS = 0;
	
	public static final String CARD_COM = "/dev/ttyO4";
	
	public static final int DATABITS_8  = 8;
	public static final int DATABITS_7  = 7;
	public static final int STOPBITS_2  = 2;
	public static final int STOPBITS_1  = 1;
	public static final int PARITY_NONE = 'n';
	public static final int PARITY_ODD  = 'o';
	public static final int PARITY_EVEN = 'e';
	
	protected android_serialport_api.SerialPort serialPort;
	protected InputStream  in;
	protected OutputStream out;
	
	private final int baudRate;
	private final int dataBits;
	private final int stopBits;
	private final int parity;
	
    public RS232Worker(int baudRate, int dataBits, int stopBits, int parity) {
    	this.baudRate = baudRate;
    	this.dataBits = dataBits;
    	this.stopBits = stopBits;
    	this.parity   = parity;
    }

    /**
     * 打开串口
     *
     * @return 0-成功,非0-失败
     */
    public int open() {
    	try {
			serialPort = new android_serialport_api.SerialPort(new File(CARD_COM), baudRate, 0, dataBits, stopBits, parity);
			
			in  = serialPort.getInputStream();
			out = serialPort.getOutputStream(); 
			return WORK_SUCCESS;
		} catch (Exception e) {
			Logger.error("New SerialPort Error", e);
			return 1;
		}
    	
    }

    /**
     * 关闭串口
     *
     * @return 0-成功,非0-失败
     */
    public int close() {
    	if(out != null) {
    		try {
    			out.close();
    		} catch(IOException e) {
    			e.printStackTrace();
    		}
    	}

    	if(in != null) {
    		try {
    			in.close();
    		} catch(IOException e) {
    			e.printStackTrace();
    		}
    	} 
    	
    	if(serialPort == null) {
    		Logger.warn("SerialPort is NULL");
    		return 1;
    	}
    	serialPort.close(); 
    	
    	return WORK_SUCCESS;
    }

    /**
     * 向串口发送信息
     *
     * @param bs 消息流
     * @return 0-成功,非0-失败
     */
    public int write(byte[] bs) {
    	if(serialPort == null) {
    		Logger.warn("SerialPort is NULL");
    		return 1;
    	} else {
    		try {
    			out.write(bs); 
    		} catch (IOException e) {
    			Logger.error(">>>>ERROR: SerialPort write error", e); 
    			return 2;
    		}
    		Logger.info("RS232 write: " + DeviceUtils.byteArray2HASCII(bs));
    		
    		return WORK_SUCCESS;
    	}
    }

    /**
     * 从串口读取数据
     *
     * @param timeout 超时时间
     * @return null则超时
     */
    public abstract byte[] read(int timeout); 

}

