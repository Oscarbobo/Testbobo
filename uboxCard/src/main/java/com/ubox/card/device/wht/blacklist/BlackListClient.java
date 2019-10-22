package com.ubox.card.device.wht.blacklist;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URI;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.ByteArrayBuffer;

import com.ubox.card.config.CardConst;
import com.ubox.card.config.CardJson;
import com.ubox.card.util.device.DeviceUtils;
import com.ubox.card.util.logger.Logger;

class BlackListClient {

	/**
	 * Http方式
	 * 
	 * @return
	 */
	public static byte[] getBlack() {

		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 3000);
			httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,3000);
			
			URI uri = URIUtils.createURI("http", CardJson.address, CardJson.httpPort, "/vcardServer/blackList?message=blackList&vmId="+CardJson.vmId+"&appType="+CardJson.appType, null, null);
			HttpGet httpGet = new HttpGet(uri);
			httpGet.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
			
			Logger.info("Download URI = " + httpGet.getURI()); // DEBUG 日志
			HttpResponse httpResponse = httpClient.execute(httpGet);
			StatusLine line = httpResponse.getStatusLine();
			int          status       = line.getStatusCode();
			if(status != HttpStatus.SC_OK) {
				Logger.warn("Download fail, status code " + status);
				return null;
			}
			InputStream is = httpResponse.getEntity().getContent();
			BufferedInputStream bis = new BufferedInputStream(is);
			ByteArrayBuffer baf = new ByteArrayBuffer(20);

			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}
			byte[] b = baf.toByteArray();
			return b;

		} catch (ClientProtocolException e) {
			Logger.error("ClientProtocolException:", e);
		} catch (Exception e) {
			Logger.error("-------"+e.getMessage(),e);
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
					// no happen
				}
			}
		}
		return null;
	}

	public static File stream2File(byte[] b) {
		File file = DeviceUtils.holdFileRef(CardConst.DEVICE_WORK_PATH, "blackList.txt");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			fos.write(b);
			fos.flush();
			return file;
		} catch (FileNotFoundException e) {
			Logger.error(e.getLocalizedMessage());
		} catch (IOException e) {
			Logger.error(e.getLocalizedMessage());
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// no happen
				}

			}
		}
		return null;
	}

}
