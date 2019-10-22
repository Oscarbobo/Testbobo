package com.ubox.card.device.bjszykt.network;

import com.alibaba.fastjson.JSON;
import com.ubox.card.device.bjszykt.pubwork.LocalContext;
import com.ubox.card.device.bjszykt.pubwork.PubUtils;
import com.ubox.card.device.bjszykt.pubwork.Result;
import com.ubox.card.device.bjszykt.server.bean.KCQTradeResponse;
import com.ubox.card.util.logger.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import java.security.MessageDigest;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class NetUtils {
    /**
     * 生成消息报文
     *
     * @param messageType 消息类型
     * @param ver 版本号
     * @param data 数据内容
     * @return 消息报文
     */
    public static byte[] initMessage(byte[] messageType, byte ver, byte[] data) {
        if(messageType == null || data == null)
            throw new IllegalArgumentException("paraments is NULL");
        if(messageType.length != 2)
            throw new IllegalArgumentException("MessageType is NOT 2");

        /* 包头 */
        byte[] packetHead   = new byte[5];

        byte[] packetLen    = PubUtils.i2bLt(10 + data.length, 2);  // 包长度
        byte[] synMessage   = new byte[] { 0x0F, 0x0F };            // 同步信息
        byte compress       = 0x00;                                 // 不压缩
        System.arraycopy(packetLen, 0, packetHead, 0, 2);
        System.arraycopy(synMessage, 0, packetHead, 2, 2);
        packetHead[4] = compress;

        /* 包体 */
        byte[] packetBody   = new byte[messageType.length + 1 + data.length];

        packetBody[0]       = messageType[0];
        packetBody[1]       = messageType[1];
        packetBody[2]       = ver;
        System.arraycopy(data, 0, packetBody, 3, data.length);

        /* CRC */
        char[] chars = new char[packetBody.length];
        for(int i = 0; i < packetBody.length; i ++) chars[i] = (char)packetBody[i];
        byte[] CRC = PubUtils.I2CRC(PubUtils.pubCalCRC(chars), 4);

        /** 生成消息报文 **/
        byte[] message = new byte[packetHead.length + packetBody.length + CRC.length];

        System.arraycopy(packetHead, 0, message, 0, 5);                         // copy包头
        System.arraycopy(packetBody, 0, message, 5, packetBody.length);         // copy包体
        System.arraycopy(CRC, 0, message, 5 + packetBody.length, CRC.length);   // copy CRC结果

        return message;
    }

    /**
     * 检测消息报文的格式
     *
     * @param cmdBts 消息报文
     * @return 0-成功,非0-失败
     */
    public static int checkCommand(byte[] cmdBts) {
        if(cmdBts == null) throw new IllegalArgumentException("bytes is NULL.");

        int bLen           = cmdBts.length;
        int calcPackLength = PubUtils.b2iLt(new byte[]{cmdBts[0], cmdBts[1]}, 2);

        if(bLen != calcPackLength + 2) {
            Logger.info(">>>>FAIL: packet length error");
            return 1;
        }

        byte[] crcNeed = new byte[bLen - 9];
        System.arraycopy(cmdBts, 5, crcNeed, 0, crcNeed.length);

        char[] chars = new char[crcNeed.length];
        for(int i = 0; i < crcNeed.length; i ++)
            chars[i] = (char)crcNeed[i];
        byte[] myCRC = PubUtils.I2CRC(PubUtils.pubCalCRC(chars), 4);

        byte[] CRC   = new byte[] {cmdBts[bLen - 4], cmdBts[bLen - 3], cmdBts[bLen - 2], cmdBts[bLen - 1]};

        for(int i = 0; i < 4; i++)
            if(myCRC[i] != CRC[i]) {
                Logger.info(">>>>FAIL:Check CRC error.");
                return 2;
            }

        return 0;
    }

    /**
     * 计算数据包名称
     *
     * @return 数据包名称字符串
     */
    public static String calcDataPacketName() {
        String unitId   = LocalContext.unitId;
        String sysTime  = PubUtils.generateSysTime();
        String SAML6    = LocalContext.CACHE_SAM.substring(LocalContext.CACHE_SAM.length() - 6);
        String MMMM     = calcMessageIDMMMM();
        String BB       = "A3";

        return unitId + sysTime + SAML6 + MMMM + BB;
    }

    private static int countMessageMMMM = 1;
    /**
     * 获取包序号
     *
     * @return 包序号
     */
    private static String calcMessageIDMMMM() {
        String rsl = String.valueOf(countMessageMMMM);

        if(++countMessageMMMM > 9997) {
            countMessageMMMM = 1;
        }

        if(rsl.length() == 1) return "000" + rsl;
        else if(rsl.length() == 2) return "00" + rsl;
        else if(rsl.length() == 3) return "0" + rsl;
        else return rsl;
    }

    /**
     * 获取包编号
     *
     * @return 包编号
     */
    public static String generatePacketNO() {
        String unitId   = LocalContext.unitId;
        String sysTime  = PubUtils.generateSysTime();
        String SAML6    = LocalContext.CACHE_SAM.substring(LocalContext.CACHE_SAM.length() - 6);
        String MMMM     = getPacketMMMM();

        return unitId + sysTime + SAML6 + MMMM;
    }
    /**
     * 获取包编号
     */
    private static int countPacketMMMM = 1;
    private static String getPacketMMMM() {
        String rsl = String.valueOf(countPacketMMMM);

        if(++countPacketMMMM > 9997) {
            countPacketMMMM = 1;
        }

        if(rsl.length() == 1) return "000" + rsl;
        else if(rsl.length() == 2) return "00" + rsl;
        else if(rsl.length() == 3) return "0" + rsl;
        else return rsl;
    }

    /**
     * 发送消息包体到前置机(不校验反馈数据报文)
     *
     * @param message 消息包体
     * @return 反馈结果
     */
    @SuppressWarnings("rawtypes")
    public static Result sendMessage(byte[] message, String url) {
        /** 初始化传输JSON参数 */
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("vmId", LocalContext.posId);
        param.put("clientSeq", generateClientSeq());
        param.put("createTime", PubUtils.generateSysTime());
        param.put("data", PubUtils.BA2HS(message));
        param.put("sign", calcSignValue(param));

        /** 与vcardserver进行交互 */
        String vsJson = httpClientSendJSON(JSON.toJSONString(param), url);

        /** 解析vcardserver返回的JSON */
        if(vsJson == null) {
            Logger.info(">>>>FAIL:Negotiations with VCardServer");
            return new Result(Result.BYTESCODE, null, 1, null, -1);
        }

        /** 验证vcardserver反馈JSON*/
        HashMap vsReMap = JSON.parseObject(vsJson, HashMap.class);

        int resultCode = (Integer)vsReMap.get("resultCode");
        if(resultCode != 200) {
            String resultMsg = (String)vsReMap.get("resultMsg");
            Logger.info(">>>>FAIL:Pass-through VCardServer.ResultCode=" + resultCode + ", resultMsg=" + resultMsg);

            return new Result(Result.BYTESCODE, null, 1, null, -1);
        }

        if(!verifyVSJson(vsReMap)) {
            Logger.info(">>>>FAIL:Check sign with VCardServer");
            return new Result(Result.BYTESCODE, null, 1, null, -1);
        }

        /** 提取前置机返回的byte流 */
        byte[] fdBytes = PubUtils.HS2BA((String) vsReMap.get("data"));

        return new Result(Result.CODESUCCESS, fdBytes, 0, null, -1);
    }

    /**
     * 发送消息包体到前置机(不校验反馈数据报文)
     *
     * @param message 消息包体
     * @return 反馈结果
     */
    @SuppressWarnings("rawtypes")
    public static Result sendMessage(byte[] message, String url, String msgType, KCQTradeResponse bean) {
        /** 初始化传输JSON参数 */
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("vmId", LocalContext.posId);
        param.put("clientSeq", generateClientSeq());
        param.put("msgType", msgType);
        param.put("createTime", PubUtils.generateSysTime());
        param.put("data", PubUtils.BA2HS(message));
        param.put("sign", calcSignValue(param));

        /** 与vcardserver进行交互 */
        String vsJson = httpClientSendJSON(JSON.toJSONString(param), url);

        /** 解析vcardserver返回的JSON */
        if(vsJson == null) {
            Logger.info(">>>>FAIL:Negotiations with VCardServer");
            return new Result(Result.BYTESCODE, null, 1, null, -1);
        }

        /** 验证vcardserver反馈JSON*/
        HashMap vsReMap = JSON.parseObject(vsJson, HashMap.class);

        int resultCode = (Integer)vsReMap.get("resultCode");
        String msg = (String)vsReMap.get("resultMsg");
        bean.setCode(resultCode);
        bean.setMsg(msg);

        if(resultCode != 200) {
            String resultMsg = (String)vsReMap.get("resultMsg");
            Logger.info(">>>>FAIL:Pass-through VCardServer.ResultCode=" + resultCode + ", resultMsg=" + resultMsg);

            return new Result(Result.BYTESCODE, null, 1, null, -1);
        }

        if(!verifyVSJson(vsReMap)) {
            Logger.info(">>>>FAIL:Check sign with VCardServer");
            return new Result(Result.BYTESCODE, null, 1, null, -1);
        }

        /** 提取前置机返回的byte流 */
        byte[] fdBytes = PubUtils.HS2BA((String) vsReMap.get("data"));

        return new Result(Result.CODESUCCESS, fdBytes, 0, null, -1);
    }

    /**
     * 生成签名值
     *
     * @param param 待签名JSON参数
     */
    private static String calcSignValue(Map<String, Object> param) {
        Map<String, String> filterMap = paraFilter(param);

        String text = createLinkedString(filterMap);
        return sign(text, LocalContext.SIGN_KEY, LocalContext.INPUT_CHARSET);
    }

    /**
     * 验证VCard Server反馈的消息合法性
     *
     * @param vsJson VCard Server反馈的消息
     * @return true-验证成功,false-验证失败
     */
    @SuppressWarnings("rawtypes")
    private static boolean verifyVSJson(HashMap vsJson) {
        @SuppressWarnings("unchecked")
        Map<String, String> filterMap = paraFilter(vsJson);

        String text = createLinkedString(filterMap);
        String sign = (String)vsJson.get("sign");
        return verify(text, sign, LocalContext.SIGN_KEY, LocalContext.INPUT_CHARSET);
    }


    /**
     * 生成终端唯一编号, 位数不够时右补0x00并按照约定长度补齐.约定长度20
     *
     * @param posId 售货机ID
     * @return 20位长度的ASCII码
     */
    public static byte[] generatePOSID(String posId) {
        if(posId == null) throw new IllegalArgumentException("params is NULL");
        //目前系统的售货机ID最大长度是10
        if(posId.length() > 10) throw new IllegalArgumentException("vmId length is more than 10");

        byte[] tmp = posId.getBytes();
        byte[] POSID = new byte[20];
        System.arraycopy(tmp, 0, POSID, 0, tmp.length);

        return POSID;
    }

    /**
     * 生成clientSeq
     *
     * @return clientSeq值
     */
    private static String generateClientSeq(){
        return LocalContext.posId + System.currentTimeMillis();
    }

    /**
     * 链接需要签名的参数对
     *
     * @param param 参数对
     * @return 待签名字符串
     */
    private static String createLinkedString(Map<String, String> param) {
        List<String> keys = new ArrayList<String>(param.keySet());
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        for(String key: keys) sb.append(key).append("=").append(param.get(key));

        return sb.toString();
    }

    /**
     * 除去数组中的空值和签名参数
     *
     * @param sArray 签名参数组
     * @return 去掉空值与签名参数后的新签名参数组
     */
    public static Map<String, String> paraFilter(Map<String, Object> sArray) {
        Map<String, String> result = new HashMap<String, String>();

        if (sArray == null || sArray.size() <= 0)  return result;

        for (String key : sArray.keySet()) {
            Object value = sArray.get(key);

            if (value == null || key.equalsIgnoreCase("sign"))
                continue;
            result.put(key, String.valueOf(value));
        }

        return result;
    }

    /**
     * 向vcardserver发送消息,其透传数据到前置机
     *
     * @param jstr json字符串
     * @return vcardserver返回的json字符串
     */
    private static String httpClientSendJSON(String jstr, String url) {
        int TIMEOUT = 80 * 1000; // http超时时间

        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT);// 等待建立连接的最长时间
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, TIMEOUT);// 建立连接后,等待时间

        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("param", jstr));

        Logger.info(">>>> Send message to VCardServer <<<<"  +
                        "\n>>>> URL:    " + url +
                        "\n>>>> pairs:  " + pairs
        );

        HttpPost httpPost = new HttpPost(url);
        String vsResultJson = null;
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(pairs, LocalContext.INPUT_CHARSET));
            HttpResponse httpResponse = httpClient.execute(httpPost);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if(statusCode != HttpStatus.SC_OK) {
                httpPost.abort();
                Logger.warn(">>>>FAIL: http POST fail,Http_Status=" + statusCode);
                return null;
            }

            vsResultJson = EntityUtils.toString(httpResponse.getEntity(), LocalContext.INPUT_CHARSET);
            Logger.info(">>>> From VCardServer: " + vsResultJson);
        } catch(UnsupportedEncodingException e) {
            Logger.error(">>>>FAIL: HttpHost setEntity error.", e);
            return null;
        } catch(IOException e) {
            Logger.error(">>>>FAIL: http POST error", e);
            return null;
        } finally {
            httpClient.getConnectionManager().shutdown();
        }

        return vsResultJson;
    }


    /**
     *
     * 签名字符串
     * @param text 需要签名的字符串
     * @param key 密钥
     * @param input_charset 编码格式
     * @return 签名结果
     */
    private static String sign(String text, String key, String input_charset) {
        text = text + key;
        return getMd5(text);
    }

    /**
     * 签名字符串
     * @param text 需要签名的字符串
     * @param sign 签名结果
     * @param key 密钥
     * @param input_charset 编码格式
     * @return 签名结果
     */
    public static boolean verify(String text, String sign, String key, String input_charset) {
        text = text + key;
//        String mysign = DigestUtils.md5Hex(getContentBytes(text, input_charset));
        String mysign =  getMd5(text);

        return mysign.equals(sign);
    }

    /*
    private static byte[] getContentBytes(String content, String charset) {
        if (charset == null || "".equals(charset))
            return content.getBytes();
        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);
        }
    }
    */

    /**
     * 获取md5值
     *
     * @return md5值
     */
    public static String getMd5(String oldStr) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        try {

            // 获得MD5摘要算法 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.reset();
            // 使用指定的字节更新摘
            mdInst.update(oldStr.getBytes());
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str).toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
