package com.ubox.card.device.hzsmk;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

import com.ubox.card.config.CardConst;
import com.ubox.card.config.CardJson;
import com.ubox.card.util.Utils;
import com.ubox.card.util.device.DeviceUtils;
import com.ubox.card.util.encrypt.DESCoder;
import com.ubox.card.util.logger.Logger;

public class HZSMKBlackListClient {
	private static String charSet = "UTF-8";
	
	public static byte[] getBlack() {
		try {
			Logger.info(">>>> get black card start... ");
			String url = "http://"+CardJson.address + ":"+ CardJson.httpPort +"/vcardServer/hzsmk/blacklist?vmId="+CardJson.vmId+"&appType="+CardJson.appType;
			HttpClient httpClient = new DefaultHttpClient();
	        //httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT);// 等待建立连接的最长时间
	        //httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, TIMEOUT);// 建立连接后,等待时间
	        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("param", null));

			// HttpGet get = new HttpGet(url);
			HttpPost httpPost = new HttpPost(url);
			String vsResultJson = null;
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(pairs,charSet));
			HttpResponse httpResponse = httpClient.execute(httpPost);
			// HttpResponse httpResponse = httpClient.execute(get);

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				httpPost.abort();
				// get.abort();
				return null;
			}

			vsResultJson = EntityUtils.toString(httpResponse.getEntity(), charSet);
			//解密数据
			byte[] resultJson = DESCoder.decryptBASE64(vsResultJson);
			
			Logger.info(">>>> From VCardServer ResultJson :" + resultJson.toString());
			return resultJson;
		} catch (UnsupportedEncodingException e) {
			Logger.error(">>>>FAIL: HttpHost setEntity error." + e.getMessage(), e);
			e.printStackTrace();
		} catch (IOException e) {
			Logger.error(">>>>FAIL: http POST error" + e.getMessage(), e);
			e.printStackTrace();
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
	} catch (Exception e) {
		e.printStackTrace();
		Logger.error(">>>>FAIL: Exception :" + e.getMessage(), e);
		}
		return null;
	}
	
	public static byte[] unzipStream(byte[] b) {
		if (b != null) {
			ObjectInputStream in2 = null;
			byte[] b3 = null;
			try {
				in2 = new ObjectInputStream(new GZIPInputStream(
						new ByteArrayInputStream(b)));
				b3 = ((String) in2.readObject()).getBytes();
				return b3;
			} catch (IOException e) {
				Logger.error(e.getLocalizedMessage());
			} catch (ClassNotFoundException e) {
				Logger.error(e.getLocalizedMessage());
			} finally {
				try {
					if (in2 != null) {
						in2.close();
					}
				} catch (IOException e) {
				}
			}
		}
		return null;
	}
	
	public static File stream2File(byte[] b) {
		File fileo = DeviceUtils.holdFileRef(CardConst.DEVICE_WORK_PATH, "m1blackList.txt");
		File filet = DeviceUtils.holdFileRef(CardConst.DEVICE_WORK_PATH, "cpublackList.txt");
		FileOutputStream foso = null;
		FileOutputStream fost = null;
		try {
			foso = new FileOutputStream(fileo);
			fost = new FileOutputStream(filet);
			String blackList = new String(b , charSet);
			
			JSONArray array = new JSONArray(blackList);
			JSONArray m1Array = new JSONArray();
			JSONArray cpuArray = new JSONArray();
			
			int length = array.length();
			for (int i = 0; i < length; i++) {
				if(array.getString(i).substring(0, 8).equals("FFFFFFFF")){
					m1Array.put(array.getString(i));
				} else {
					cpuArray.put(array.getString(i));
				}
			}
			byte[] m1B = Utils.stringToByte(m1Array.toString());
			byte[] cpuB = Utils.stringToByte(cpuArray.toString());
			
			foso.write(m1B);
			fost.write(cpuB);
			foso.flush();
			fost.flush();
			return fileo;
		} catch (FileNotFoundException e) {
			Logger.error(e.getLocalizedMessage());
		} catch (IOException e) {
			Logger.error(e.getLocalizedMessage());
		} catch(JSONException e){
			Logger.error(e.getLocalizedMessage());
		} finally {
			if (foso != null || fost != null) {
				try {
					foso.close();
					fost.close();
				} catch (IOException e) {
				}

			}
		}
		return null;
	}
}
