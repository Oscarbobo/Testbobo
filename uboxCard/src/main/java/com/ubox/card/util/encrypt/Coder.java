package com.ubox.card.util.encrypt;

import java.security.MessageDigest;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
  
/**
 * @author huyuguo
 * @description 基础加密组件
 * @fileName com.ubox.des.Coder.java
 * @date 2011-9-6 上午10:11:36
 */
public abstract class Coder {   
    public static final String KEY_SHA = "SHA";   
    public static final String KEY_MD5 = "MD5";   
  
    /**  
     * MAC算法可选以下多种算法  
     *   
     * <pre>  
     * HmacMD5   
     * HmacSHA1   
     * HmacSHA256   
     * HmacSHA384   
     * HmacSHA512  
     * </pre>  
     */  
    public static final String KEY_MAC = "HmacMD5";   
  
    /**
     * @author huyuguo
     * @description BASE64解密
     * @param key
     * @return
     * @throws Exception
     * @date 2011-9-6 上午10:12:25
     */
    public static byte[] decryptBASE64(String key) throws Exception {   
        return Base64.decode(key, Base64.DEFAULT);   
    }   
  
    /**
     * @author huyuguo
     * @description BASE64加密
     * @param key
     * @return
     * @throws Exception
     * @date 2011-9-6 上午10:12:49
     */
    public static String encryptBASE64(byte[] key) throws Exception {   
        return Base64.encodeToString(key, Base64.DEFAULT);   
    }   
  
    /**
     * @author huyuguo
     * @description MD5加密
     * @param data
     * @return
     * @throws Exception
     * @date 2011-9-6 上午10:13:08
     */
    public static byte[] encryptMD5(byte[] data) throws Exception {   
        MessageDigest md5 = MessageDigest.getInstance(KEY_MD5);   
        md5.update(data);   
        return md5.digest();   
    }   
  
    /**
     * @author huyuguo
     * @description SHA加密
     * @param data
     * @return
     * @throws Exception
     * @date 2011-9-6 上午10:13:21
     */
    public static byte[] encryptSHA(byte[] data) throws Exception {   
        MessageDigest sha = MessageDigest.getInstance(KEY_SHA);   
        sha.update(data);   
        return sha.digest();   
  
    }   
  
    /**
     * @author huyuguo
     * @description 初始化HMAC密钥  
     * @return
     * @throws Exception
     * @date 2011-9-6 上午10:13:32
     */
    public static String initMacKey() throws Exception {   
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_MAC);   
        SecretKey secretKey = keyGenerator.generateKey();   
        return encryptBASE64(secretKey.getEncoded());   
    }   
  
    /**
     * @author huyuguo
     * @description HMAC加密
     * @param data
     * @param key
     * @return
     * @throws Exception
     * @date 2011-9-6 上午10:13:43
     */
    public static byte[] encryptHMAC(byte[] data, String key) throws Exception {   
        SecretKey secretKey = new SecretKeySpec(decryptBASE64(key), KEY_MAC);   
        Mac mac = Mac.getInstance(secretKey.getAlgorithm());   
        mac.init(secretKey);   
        return mac.doFinal(data);   
    }   
}  
