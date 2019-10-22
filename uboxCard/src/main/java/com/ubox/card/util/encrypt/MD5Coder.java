package com.ubox.card.util.encrypt;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @description MD5加密算法组件
 * @fileName com.ubox.utils.encrypt.MD5CoderUtil.java
 */
public class MD5Coder {
	/**
	 * @description 获取MD5加密串
	 * @param str
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static String getMD5Str(String str) throws NoSuchAlgorithmException, 
		UnsupportedEncodingException {   
		if(str == null || str.equals("")) {
			return null;
		}
        MessageDigest messageDigest = null;   
        messageDigest = MessageDigest.getInstance("MD5");   
        messageDigest.reset();   
        messageDigest.update(str.getBytes("UTF-8"));   
        byte[] byteArray = messageDigest.digest();   
        StringBuffer md5StrBuff = new StringBuffer();   
        for (int i = 0; i < byteArray.length; i++) {               
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1){
            	md5StrBuff.append("0").append(Integer.
            			toHexString(0xFF & byteArray[i]));
            }
            else{
            	md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
            }
        }   
        return md5StrBuff.toString();   
    }   
	
}
