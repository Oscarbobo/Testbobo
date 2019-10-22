package com.ubox.card.device.bjszykt.server.webapp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import android.annotation.SuppressLint;

import com.alibaba.fastjson.JSON;
import com.ubox.card.device.bjszykt.recharge.MainRecharge;
import com.ubox.card.device.bjszykt.server.bean.KCQTradeResponse;
import com.ubox.card.device.bjszykt.server.webapp.NanoHTTPD.IHTTPSession;
import com.ubox.card.device.bjszykt.server.webapp.NanoHTTPD.Response;
import com.ubox.card.util.logger.Logger;

/**
 * 
 * @author gaolei
 * @version 2015年9月23日
 * 
 */
@SuppressLint({ "ServiceCast", "SdCardPath" })
public class BJSZYKTKCQPlugin implements WebServerPlugin {

	/* (non-Javadoc)
	 * @see com.ubox.h2.WebServerPlugin#canServeUri(java.lang.String, java.io.File)
	 */
	@Override
	public boolean canServeUri(String uri, File rootDir) {
		if(uri.endsWith(".ub")){
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.ubox.h2.WebServerPlugin#initialize(java.util.Map)
	 */
	@Override
	public void initialize(Map<String, String> commandLineOptions) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.ubox.h2.WebServerPlugin#serveFile(java.lang.String, java.util.Map, com.ubox.h2.NanoHTTPD.IHTTPSession, java.io.File, java.lang.String)
	 */
	@Override
	public Response serveFile(String uri, Map<String, String> headers,
			IHTTPSession session, File file, String mimeType) {
		
		Response res = null;
//		StringBuilder strB = new StringBuilder();
		try {
			Logger.info(">>>>>>>> uri:"+uri);
			Logger.info(">>>>>>>> headers:"+headers);
			Logger.info(">>>>>>>> session:"+session);
			Logger.info(">>>>>>>> file:"+file);
			Logger.info(">>>>>>>> mimeType:"+mimeType);
			Logger.info(">>>>>>>> parms:"+session.getParms());
			Logger.info(">>>>>>>> queryParms:"+session.getQueryParameterString());
			
			if("/bjszykt/kuaichongquan/start.ub".equals(uri)){
				File indexFile = new File("/mnt/sdcard/Ubox/resource/webapp/bjszykt/kuaichongquan/index.html");
				res = new Response(Response.Status.OK, NanoHTTPD.MIME_HTML, new FileInputStream(indexFile), (int) indexFile.length());
			}else if("/bjszykt/kuaichongquan/trade.ub".equals(uri)){
//				strB.append("{\"code\":200,\"msg\":\"充值成功\",\"cards\":{\"cardNo\":\"123123123123\",\"cardBalance\":88000,\"validDate\":\"2025-12-12\"},\"orders\":{\"orderNo\":\"1231231231\",\"orderAmt\":20,\"orderDate\":\"2015-10-10\",\"orderTime\":\"\"}}");
				KCQTradeResponse response = MainRecharge.getBean();
				String str = JSON.toJSONString(response);
				Logger.info(">>>>> send data:"+str);
				res = new Response(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, new ByteArrayInputStream(str.getBytes("UTF-8")), str.getBytes("UTF-8").length);
			}else if("/bjszykt/kuaichongquan/return.ub".equals(uri)){
				String[] command = { "/system/bin/sh","-c", "busybox pkill com.ubox.cvs"};
				try {
					Runtime.getRuntime().exec(command);
					
				} catch (IOException e) {
					Logger.error("Runtime.getRuntime().exec is Exception. "+e.getMessage()+" cmd:"+command ,e);
				}
				res = new Response(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, new ByteArrayInputStream("".getBytes("UTF-8")), "".getBytes("UTF-8").length);
			}else{
				byte[] bs = {};
				res = new Response(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, new ByteArrayInputStream(bs), bs.length);
			}
			res.addHeader("Accept-Ranges", "bytes");
		} catch (Exception e) {
			Logger.error("NanoHttpd exception. "+e.getMessage(), e);
		}
		
		return res;
	}

}
