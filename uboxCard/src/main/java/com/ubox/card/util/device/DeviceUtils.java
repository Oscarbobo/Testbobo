package com.ubox.card.util.device;

import java.io.File;
import java.io.IOException;

import com.ubox.card.util.logger.Logger;

public class DeviceUtils {

    /**
     * 获取指定路径下的文件句柄.如果文件不存在,则创建路径和文件
     *
     * @param path 指定路径
     * @param fileName 指定文件
     * @return 文件句柄
     */
    public static File holdFileRef(String path, String fileName) {
    	if(path == null || fileName == null) {
        	throw new IllegalArgumentException("argument is NULL");
    	}
    	
        File p = new File(path);
        if(!p.exists()) {
        	Logger.info((p.mkdirs() ? "SUCCESS: " : "FAIL: ") + "Create path=" + path);
        }

        File f = new File(path + File.separator + fileName);
        if(!f.exists()) {
        	try {
        		Logger.info((f.createNewFile() ? "SUCCESS: " : "FAIL: ") + "Create fileName=" + fileName);
        	} catch (IOException e) {
        		Logger.error("FAIL to create file: " + fileName, e);
        	}
        }

        return f;
    }
    
	/**
	 * Byte数组转换成H-ASCII字符串
	 * 
	 * @param byteArray byte数组
	 * @return H-ASCII字符串
	 */
	public static String byteArray2HASCII(byte[] byteArray) {
        if(byteArray == null) {
        	throw new IllegalArgumentException("argument is NULL");
        }

        StringBuilder sb = new StringBuilder(byteArray.length * 2);
        for(int b : byteArray) {
            String tmp = Integer.toHexString(b & 0xFF);
            if(tmp.length() < 2) {
            	sb.append('0');
            }
            sb.append(tmp);
        }

        return sb.toString();
	}
	
	/**
	 * Byte转换成字符串
	 * @param b
	 * @return
	 */
	public static String byte2HASCII(byte b) {
		String tmp = Integer.toHexString(b & 0xFF);
		
		if(tmp.length() < 2) {
			return "0" + tmp;
		} else {
			return tmp;
		}
	}
	
    /**
	 * H-ASCII字符串转换成Byte数组
     *
     * @param hAS HEX字符串
     * @return 字符数组
     */
    public static byte[] hASCII2ByteArray(String hAS) {
        if(hAS == null) {
        	throw new IllegalArgumentException("arg is NULL");
        }
        
        if(hAS.length() % 2 != 0) {
        	throw new IllegalArgumentException("H-ASCII must Even.");
        }
        
        if(hAS.length() == 0) {
        	return new byte[0];
        }

        char[] hs   = hAS.toCharArray();
        byte[] bArr = new byte[hs.length / 2];
        
        for(int i = 0; i < hs.length; i ++) {
            byte high = (byte)Character.digit(hs[i], 16);
            byte low  = (byte)Character.digit(hs[++i], 16);
            bArr[i/2] = (byte)((high << 4) | low);
        }

        return bArr;
    }
    
    /**
     * 正整数转换成字节数组(小端模式)
     *
     * @param digital 正整数
     * @param length 返回的字节数组长度
     * @return 小端字节数组
     */
    public static byte[] i2bLt(int digital, int length) {
        if(length <= 0) {
        	throw new IllegalArgumentException("length <= 0");
        }

        byte[] bs = new byte[length];
        for(int i = 0, y = 0; (y < length) && (i < 4) ; i++, y++) {
            byte b = (byte)((digital >> (i * 8)) & 0xFF);
            bs[y] = b;
        }

        return bs;
    }

    /**
     * 正整数转换成字节数组(大端模式)
     *
     * @param digtial 正整数
     * @param length 返回的字节数组长度
     * @return 大端字节数组
     */
    public static byte[] i2bLg(int digtial, int length) {
        if(length <= 0 || length > 4) {
        	throw new IllegalArgumentException("length must in 1~4");
        }

        byte[] bs = new byte[length];
        for(int i = 0, y = length - 1; (y >= 0) && (i < 4); i++, y--) {
            bs[y] = (byte)(digtial >> (i * 8));
        }

        return bs;
    }
    
}
