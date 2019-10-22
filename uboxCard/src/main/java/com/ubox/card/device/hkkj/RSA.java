package com.ubox.card.device.hkkj;


import com.ubox.card.util.Base64Utils;


import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

public class RSA {

    public static final String KEY_ALGORITHM = "RSA";
    public static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";
    public static final int KEY_SIZE = 2048;

    public static final String PUBLIC_KEY ="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjpecOadrCgRePxMdT9v2SXlANlLNF7N4k6BelcCGiNQgrA/1lq0eA0eI+F6uSMMuNQwSaTDsV3G/S13zSccv/bB7AiQLcmye610mbB2/EsJIDtJwkqAp7CCtLUY8Y8Ltw11RRl3ZGbQQrNXiuMkM2G5q9tTdDU7fTd3bLE1SAvJRIEhxAev6rNWuIc2FOQbDLT2zgmHF7yA7WXTSSDg3cXMGI9X7UVGKnjMZMBih/c7As1fuHqakg+qx5U9qxSPQ0tyXuUnW1rO/ZYUlpDl6Te2VeP1FOZWFQkHWbgi0uvuyqDTPPODSXvw6Bgy6qdFwscquDm4NfpowXEC0R9mEvQIDAQAB";
    public static final String PRIVATE_KEY ="MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCOl5w5p2sKBF4/Ex1P2/ZJeUA2Us0Xs3iToF6VwIaI1CCsD/WWrR4DR4j4Xq5Iwy41DBJpMOxXcb9LXfNJxy/9sHsCJAtybJ7rXSZsHb8SwkgO0nCSoCnsIK0tRjxjwu3DXVFGXdkZtBCs1eK4yQzYbmr21N0NTt9N3dssTVIC8lEgSHEB6/qs1a4hzYU5BsMtPbOCYcXvIDtZdNJIODdxcwYj1ftRUYqeMxkwGKH9zsCzV+4epqSD6rHlT2rFI9DS3Je5SdbWs79lhSWkOXpN7ZV4/UU5lYVCQdZuCLS6+7KoNM884NJe/DoGDLqp0XCxyq4Obg1+mjBcQLRH2YS9AgMBAAECggEBAIo5uf3Bp2eceGgYdW+2wPbUjUSb26jAF4C0EzyD12bIi6uABQg9ZTy0mbh6v+RVIHPX8fj6rgDOiSvcNT0t0/3OmE+o9VfEqAFs2RQxecoLf6KuqOXaUQlPW9DppxzmF2otCd52lz/tZ8TDyfZrHwKndxUzn92IubtX92DoyzmCi8wQcZjyx9HYS4CQnoqCXbemEfTcKk+t1F9eUNAXqma4HHBukLCmKO65JgAdnhtKrVwEO0M3MRjXLfup4EXCporLeQ3qP5anG6tnt5n7QRAm7AZWy6Ara+hd6i0hnhiO4BjvPUdcPKPLEGPp643H1oCKCRyhAuc1+w/2xxdaGUECgYEA1yjeO9uXlyExppjWo/A0yC45RWy1k2DZp6TqPPwYUdRAaFi82Z+hpFGwj7htzuAhEs77Cduyv/w7BfuVU07PchN8It8Ixhx0V/EXy2KRgi7amdYcWXJbAX4nD4V9VQNbp4U8BOeAwAL4vxMJGGQV5m4Qv493eogLJa0FrlhGYnUCgYEAqaiDtU8wLQO2zUmu5BORzeOLXdVmcMX2zpSwTKiFbr9h+b0H5YTyeFwnB7OQjxOeDtVLOIO02CzXi4ktr06Jo92IwMO1Fl2GIVZ7UKjA+xC8pvo8onnfTwDfHucDlnqOi1vnkzjtpbzUbAUMpWy7wa2Qvhb9u4YrfEuH8aBMwCkCgYEAmrS9Dsc3eaiZfdO5ygkGusVZ0sx2xvDFr+BJuO3/Qp9ebGkZ6VT5siA21zBibHUF8vM22IDLR3ZCrmuWlF8/lCPkh2Jdea2uDinB1TEbtUKhVin5Gmu6ehlOVr/CPaV7erm3TUN0dRvt5d33FT2p7ZVIn6S14Tn+4elGA3Vfrp0CgYEAohyxbWfzYq/v1o0XnjvM0G0i2FB3CD9BK/gL1dQZW89nsRidGQVGIxf6+3LrKKvYFdhdfezrurxuYZh8nMxw3LFsYLUgrfRDnEtknZgCfmRpvyvREHdfl7//cynO1dTZq/PvfU+iVQHwAB97C/6wEcrlSZ3yaWM03hnWFl3TuLECgYAUc2cIL3VIWhaioB27PFUxf7sIwkDTvrl553Luby2oJdyIb2ywFvVhskFYQLe3iJX+CW+lYEd5JYg4VDdBiswqCNkeXmuBNZVE1zlRDUFMZ65gljEWGbt4sLaQ13XRDSQ3NcCAxxEg5YwloLfg/CzBt/cYJMUgJfcCt8qGkbQrQw==";
    /**
     * 生成密钥对
     *
     * @return
     */
    public static Map<String, byte[]> generateKeyBytes() {

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator
                    .getInstance(KEY_ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            Map<String, byte[]> keyMap = new HashMap<String, byte[]>();
            keyMap.put("publicKey", publicKey.getEncoded());
            keyMap.put("privateKey", privateKey.getEncoded());
            return keyMap;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 还原公钥
     *
     * @param keyBytes
     * @return
     */
    public static PublicKey restorePublicKey(byte[] keyBytes) {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
        try {
            KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
            PublicKey publicKey = factory.generatePublic(x509EncodedKeySpec);
            return publicKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 还原私钥
     *
     * @param keyBytes
     * @return
     */
    public static PrivateKey restorePrivateKey(byte[] keyBytes) {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
                keyBytes);
        try {
            KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
            PrivateKey privateKey = factory
                    .generatePrivate(pkcs8EncodedKeySpec);
            return privateKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 得到公钥
     * @param key 密钥字符串（经过base64编码）
     * @throws Exception
     */
    public static PublicKey getPublicKey(String key) throws Exception {
        byte[] keyBytes = Base64Utils.decodeByte(key);
//        keyBytes = (new Base64()).decodeBuffer(key);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    /**
     * 得到私钥
     * @param key 密钥字符串（经过base64编码）
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(String key) throws Exception {
        byte[] keyBytes=Base64Utils.decodeByte(key);
//        keyBytes = (new BASE64Decoder()).decodeBuffer(key);
//        keyBytes = key.getBytes("UTF-8");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }


}
