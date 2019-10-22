package com.ubox.card.device.quickpass;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;

import com.alibaba.fastjson.JSON;
import com.ubox.card.bean.db.QuickPassTradeObj.QuickPassTrade;
import com.ubox.card.config.DeviceConfig;
import com.ubox.card.db.dao.QuickPassTradeDao;
import com.ubox.card.util.SysUtil;
import com.ubox.card.util.logger.Logger;

@SuppressLint("DefaultLocale")
public class QUICKPASSUpload extends TimerTask{

	@Override
	public void run() {
		//记录成功的id，删除使用
//		List<String> idsSuccess = new ArrayList<String>();
		try {
			/* 从db中读取数据 */
			// 将quickpasslog.db中的最新30条数据同步到服务器
			int maxN = 30;

			List<Object> quickpassLogList = QuickPassTradeDao.getInstance().queryLatestMaxN(maxN);
			
			/* 上传NFC服务器 */
			if (quickpassLogList != null && quickpassLogList.size() != 0) {
				for(int i=0,j=quickpassLogList.size();i<j;i++){
					QuickPassTrade quickPassLog = (QuickPassTrade)quickpassLogList.get(i);
					SysUtil.sleep(2000);
					String id = uploadOneData2NFCServer(quickPassLog);
//					idsSuccess.add(id);
					
					//上传一条就删除一条
					if(!"".equals(id)){
						deleteOneData(id);
					}
				}
			} else {
				Logger.warn("quickpasslog.db中无数据！");
			}
			
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 上传一条数据到NFC服务器
	 * @param quickPassLog
	 * @return
	 */
	public static String uploadOneData2NFCServer(QuickPassTrade quickPassLog){
		Logger.info(">>>>>上传一条数据到NFC服务器的请求参数:"+JSON.toJSONString(quickPassLog));
		//记录成功的id，删除使用
		String id = quickPassLog.getId();
		
        String type         = "0259";
        String baseUrl      =
//                "http://106.120.223.202:15003";
                  "http://www.huiyuanbao.com/payhttp/default.aspx";
        String termID       = DeviceConfig.getInstance().getValue("TERMNO");
                //"99660182";
        String merchantID   =
//                "123456789012345";
                  "201002100000041";
        String macKey       =
//                "1111111111111111";
                  "ASUDNFHEYRPLKJNH";
        String money        = quickPassLog.getPTradeMoney()+"";
        String cardNo       = quickPassLog.getPCardNo();
        String cardSerialNo = quickPassLog.getPCardSerNo();
//        String serNo = quickPassLog.getPSerialNo();
        String icData       = quickPassLog.getPInputData().toUpperCase();
        //"9F26083050DAB05B6629F29F2701409F100A07010103900000010A019F37049C1A0D819F360204E1950500000000009A031502029C01009F02060000000000015F2A02015682027C009F1A0201569F03060000000000009F33030000009F3501229F1E0834353030353131308A0259319F7406454343303031";
        
		String[] re = icPayConsumeInform(type, baseUrl, termID, merchantID, macKey, money, cardNo, cardSerialNo, icData);
		if ((re != null && "00".equals(re[0]))
				|| (re != null && "01094".equals(re[0]))) {
			return id;
		}else{
			return "";
		}
	}
	
	/**
	 * 从db文件删除一条数据
	 * @param id
	 */
	public static void deleteOneData(String id){
		if(null == id){
			Logger.error("id is null");
		}else{
			QuickPassTradeDao.getInstance().deleteOne(id);
		}
	}

    @SuppressLint("SimpleDateFormat")
	public static String[] icPayConsumeInform(
            String type, String baseUrl, String termID, String merchantID, String macKey,
            String fen, String cardNo, String cardSerialNo, String icData) {
    	SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = DATE_FMT.format(new Date());
        String sper = "*";
        String tranDate   = date.substring(0, 8);
        String tranTime   = date.substring(8, 14);
        String macStr     = termID       + sper + // 终端号
                            tranDate     + sper + // 日期
                            tranTime     + sper + // 时间
                            tranTime     + sper + // 流水号
                            merchantID   + sper + // 商户号
                            fen          + sper + // 交易金额
                            cardNo       + sper + // 卡号
                            cardSerialNo + sper + // 卡序列号
                            icData       + sper + // ic卡数据域
                            macKey;               // macKey

        String result = null;
        
        try {
        	String mac        = getMD5Str(macStr);
        	
        	String sendData = wrapSendJson(type, termID, tranDate, tranTime, merchantID, fen, cardNo,
        			cardSerialNo, icData, mac);
        	
        	Logger.info(">>> macStr = " + macStr);
        	Logger.info(">>> json = " + sendData);
        	
        	List<NameValuePair> nvs = new ArrayList<NameValuePair>();
        	NameValuePair       bnv = new BasicNameValuePair("Data", sendData);
        	
        	nvs.add(bnv);
        	int   timeout = 10000;

        	HttpClient httpClient = new DefaultHttpClient();
        	HttpPost   httpPost   = new HttpPost(baseUrl);
        	httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
        	httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout);

        	httpPost.setEntity(new UrlEncodedFormEntity(nvs));
            HttpResponse response = httpClient.execute(httpPost);
            result = EntityUtils.toString(response.getEntity());
            
        } catch (Exception e) {
        	Logger.error(e.getMessage(),e);
        	return null;
        }

        Logger.info(">>> result = " + result); 
        return analyzeIcPayConsumeInform(result);
        
    }
    
    /**
     * {"TermID":"99660182","TranDate":"20150506","TranTime":"144643","SerNo":"144643","RetCode":"00","RetInfo":"支付成功","MAC":"26a1c4a80f98027631a191235685042a"}
     	* “00”	成功.() 
    	 * string[0] 返回状态 status; 
    	 * string[1] 操作信息
    	 * 
    */
	@SuppressWarnings("rawtypes")
	private static String[] analyzeIcPayConsumeInform(String result){
    	String[] re = {"",""};
    	try {
			Map map = new HashMap();
			map = JSON.parseObject(result);
			re[0] = map.get("RetCode")+"";
			re[1] = map.get("RetInfo")+"";
		} catch (Exception e) {
			Logger.error(e.getMessage(),e);
		}
    	return re;
    }
    
    private static String getMD5Str(String macStr) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(macStr.getBytes("utf-8"));

            return b2hs(messageDigest.digest());
        } catch (Exception e) {
            Logger.error(e.getMessage(),e);
            return "";
        }
    }
    
    private static String b2hs(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for(byte e : b) {
            String t = Integer.toHexString(e & 0xff);
            if(t.length() > 1)
                sb.append(t);
            else
                sb.append('0').append(t);
        }

        return sb.toString();
    }

//    private static byte[] hs2b(String hs) {
//        byte[] b = new byte[hs.length() / 2];
//
//        for(int i = 0, len = hs.length(); i < len; i+=2) {
//            b[i / 2] = (byte)Integer.parseInt(hs.substring(i, i + 2), 16);
//        }
//
//        return b;
//    }

    private static String wrapSendJson(
            String type, String termID, String tranDate, String tranTime, String merChantID,
            String tradeMoney, String cardNo, String cardSerialNo, String icData, String mac) {

       return "{ " +
                    "\"Type\":\""         + type         + "\", " +
                    "\"TermID\":\""       + termID       + "\", " +
                    "\"TranDate\":\""     + tranDate     + "\", " +
                    "\"TranTime\":\""     + tranTime     + "\", " +
                    "\"SerNo\":\""        + tranTime     + "\", " + 
                    "\"MerchantID\":\""   + merChantID   + "\", " +
                    "\"TradeMoney\":\""   + tradeMoney   + "\", " +
                    "\"CardNo\":\""       + cardNo       + "\", " +
                    "\"CardSerialNo\":\"" + cardSerialNo + "\", " +
                    "\"ICData\":\""       + icData       + "\", " +
                    "\"MAC\":\""          + mac          + "\"" +
               " }";
    }
}
