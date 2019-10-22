package com.ubox.card.util.encrypt;

import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * @author huyuguo
 * @description 
 * <pre>  
 * 支持 DES、DESede(TripleDES,就是3DES)、AES、Blowfish、RC2、RC4(ARCFOUR)  
 * DES                  key size must be equal to 56  
 * DESede(TripleDES)    key size must be equal to 112 or 168  
 * AES                  key size must be equal to 128, 192 or 256,but 192 and 256 bits may not be available  
 * Blowfish             key size must be multiple of 8, and can only range from 32 to 448 (inclusive)  
 * RC2                  key size must be between 40 and 1024 bits  
 * RC4(ARCFOUR)         key size must be between 40 and 1024 bits  
 * 具体内容 需要关注 JDK Document http://.../docs/technotes/guides/security/SunProviders.html  
 * </pre>  
 * @fileName com.ubox.utils.des.DESCoder.java
 * @date 2011-9-6 下午1:24:12
 */
public class DESCoder extends Coder {
	/**  
     * ALGORITHM 算法 <br>  
     * 可替换为以下任意一种算法，同时key值的size相应改变。  
     *   
     * <pre>  
     * DES                  key size must be equal to 56  
     * DESede(TripleDES)    key size must be equal to 112 or 168  
     * AES                  key size must be equal to 128, 192 or 256,but 192 and 256 bits may not be available  
     * Blowfish             key size must be multiple of 8, and can only range from 32 to 448 (inclusive)  
     * RC2                  key size must be between 40 and 1024 bits  
     * RC4(ARCFOUR)         key size must be between 40 and 1024 bits  
     * </pre>  
     *   
     * 在Key toKey(byte[] key)方法中使用下述代码  
     * <code>SecretKey secretKey = new SecretKeySpec(key, ALGORITHM);</code> 替换  
     * <code>  
     * DESKeySpec dks = new DESKeySpec(key);  
     * SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);  
     * SecretKey secretKey = keyFactory.generateSecret(dks);  
     * </code>  
     */  
    public static final String ALGORITHM = "DES";  
    /**
     * @author huyuguo
     * @description 转换密钥
     * @param key
     * @return
     * @throws Exception
     * @date 2011-9-6 下午1:23:52
     */
    private static Key toKey(byte[] key) throws Exception {   
        DESKeySpec dks = new DESKeySpec(key);   
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);   
        SecretKey secretKey = keyFactory.generateSecret(dks);   
        // 当使用其他对称加密算法时，如AES、Blowfish等算法时，用下述代码替换上述三行代码   
        // SecretKey secretKey = new SecretKeySpec(key, ALGORITHM);   
        return secretKey;   
    }   
    
    /**
     * @author huyuguo
     * @description 解密
     * @param data
     * @param key
     * @return
     * @throws Exception
     * @date 2011-9-6 下午1:23:42
     */
    public static byte[] decrypt(byte[] data, String key) throws Exception {   
        Key k = toKey(decryptBASE64(key));   
        Cipher cipher = Cipher.getInstance(ALGORITHM);   
        cipher.init(Cipher.DECRYPT_MODE, k);   
        return cipher.doFinal(data);   
    }   
  
    /**
     * @author huyuguo
     * @description 加密
     * @param data
     * @param key
     * @return
     * @throws Exception
     * @date 2011-9-6 下午1:23:32
     */
    public static byte[] encrypt(byte[] data, String key) throws Exception {   
        Key k = toKey(decryptBASE64(key));   
        Cipher cipher = Cipher.getInstance(ALGORITHM);   
        cipher.init(Cipher.ENCRYPT_MODE, k);   
  
        return cipher.doFinal(data);   
    }   
  
    /**
     * @author huyuguo
     * @description 生成密钥
     * @return
     * @throws Exception
     * @date 2011-9-6 下午1:23:20
     */
    public static String initKey() throws Exception {   
        return initKey(null);   
    }   
  
    /**
     * @author huyuguo
     * @description 生成密钥
     * @param seed
     * @return
     * @throws Exception
     * @date 2011-9-6 下午1:23:07
     */
    public static String initKey(String seed) throws Exception {   
        SecureRandom secureRandom = null;   
  
        if (seed != null) {   
            secureRandom = new SecureRandom(decryptBASE64(seed));   
        } else {   
            secureRandom = new SecureRandom();   
        }   
  
        KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM);   
        kg.init(secureRandom);   
  
        SecretKey secretKey = kg.generateKey();   
  
        return encryptBASE64(secretKey.getEncoded());   
    }  
    
    /**
     * @author huyuguo
     * @description For test.
     * @param args
     * @throws Exception
     * @date 2011-9-6 上午11:07:31
     */
    public static void main(String[] args) throws Exception{
		String inputStr = "胡玉国胡玉国胡玉国";   
        String key = DESCoder.initKey();   
        System.err.println("原文:\t" + inputStr);   
        System.err.println("密钥:\t" + key);   
        byte[] inputData = inputStr.getBytes();   
        inputData = DESCoder.encrypt(inputData, key);   
        System.err.println("加密后:\t" + DESCoder.encryptBASE64(inputData));   
        byte[] outputData = DESCoder.decrypt(inputData, key);   
        String outputStr = new String(outputData);   
        System.err.println("解密后:\t" + outputStr); 
        
        String tt = "二";
        byte[] b0 = tt.getBytes("GBK");
        byte[] b1 = tt.getBytes("UTF-8");
        
        System.out.println("---------b0----------------");
        System.out.println(b0.length);
        for(byte b : b0){
        	System.out.print(b);
        }
        
        System.out.println("\n---------b1----------------");
        System.out.println(b1.length);
        for(byte b : b1){
        	System.out.print(b);
        }
	}
}
