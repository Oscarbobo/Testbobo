package com.ubox.card.device.hkkj;


import com.ubox.card.util.Base64Utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;


public class SHA256withRsaUtils {

    public static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    public static final String ENCODE_ALGORITHM = "SHA-256";
      public static final String PLAIN_TEXT = "{\"cardNo\":\"1549292209\",\"cardInfo\":\"00000000000000000000000000000b12\",\"totalDue\":2.5,\"vmid\":\"99900602\",\"deviceName\":\"\",\"productId\":3,\"productName\":\"可口可乐芬达橙味汽水330ml\",\"quantity\":1,\"orderBillingType\":\"TRADE\"}";
    //    public static final String PLAIN_TEXT = "{\"cardNo\":\"b152585c\",\"totalDue\":2.5,\"vmid\":\"99900602\",\"deviceName\":\"test\",\"productId\":3,\"productName\":\"可口可乐芬达橙味汽水330ml\",\"quantity\":1,\"orderBillingType\":\"TRADE\"}";



    public static void main(String[] args) throws Exception {
        // 公私钥对
//        Map<String, byte[]> keyMap = RSA.generateKeyBytes();
//        byte[] publicKeys = keyMap.get("publicKey");
//        byte[] privateKeys = keyMap.get("privateKey");
//
//        System.out.println( org.apache.commons.codec.binary.Base64.encodeBase64String(publicKeys) );
//        System.out.println( org.apache.commons.codec.binary.Base64.encodeBase64String(privateKeys) );
//
//        PublicKey publicKey = RSA.restorePublicKey(keyMap.get( "publicKey" ));
//        PrivateKey privateKey = RSA.restorePrivateKey(keyMap.get( "privateKey" ));

        PublicKey publicKey = RSA.getPublicKey( RSA.PUBLIC_KEY );
        PrivateKey privateKey = RSA.getPrivateKey( RSA.PRIVATE_KEY );
         //签名
        byte[] sing_byte = sign(privateKey, PLAIN_TEXT);

        System.out.println(PLAIN_TEXT);
        String s = String.valueOf(Base64Utils.encode(sing_byte));
        System.err.println("str--"+s);

        // 验签
        boolean pass = verifySign(publicKey, PLAIN_TEXT, sing_byte);
        System.out.println("是否通过--->"+pass);



    }

    /**
     * 签名
     *
     * @param privateKey
     *            私钥
     * @param plain_text
     *            明文
     * @return
     */
    public static byte[] sign(PrivateKey privateKey, String plain_text) {
        byte[] signed = null;
        try {
//            MessageDigest messageDigest = MessageDigest.getInstance(ENCODE_ALGORITHM);
//            messageDigest.update(plain_text.getBytes("UTF-8"));
//            byte[] outputDigest_sign = messageDigest.digest();
//            System.out.println("SHA-256加密后-----》" +bytesToHexString(outputDigest_sign));
            Signature sign = Signature.getInstance( SIGNATURE_ALGORITHM );
            sign.initSign(privateKey);
//            sign.update(outputDigest_sign);
            sign.update(plain_text.getBytes("UTF-8"));
            signed = sign.sign();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return signed;
    }

    /**
     * 验签
     *
     * @param publicKey
     *            公钥
     * @param plain_text
     *            明文
     * @param signed
     *            签名
     */
    public static boolean verifySign(PublicKey publicKey, String plain_text, byte[] signed) {
        boolean signedSuccess=false;
        try {
//            MessageDigest messageDigest = MessageDigest.getInstance(ENCODE_ALGORITHM);
//            messageDigest.update(plain_text.getBytes());
//            byte[] outputDigest_verify = messageDigest.digest();
//            System.out.println("SHA-256加密后-----》" +bytesToHexString(outputDigest_verify));
            Signature verifySign = Signature.getInstance(SIGNATURE_ALGORITHM);
            verifySign.initVerify( publicKey );
//            verifySign.update(outputDigest_verify);
            verifySign.update( plain_text.getBytes("UTF-8") );
            signedSuccess = verifySign.verify(signed);
//            System.out.println("验证成功？---" + signedSuccess);

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e ) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return signedSuccess;
    }

    /**
     * bytes[]换成16进制字符串
     *
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


    public static String stringToHexString(byte[] src)
    {
        StringBuilder sb = new StringBuilder();
        for (byte b : src) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();

    }

    /**
     * 字节数组转Base64编码

     */





}
