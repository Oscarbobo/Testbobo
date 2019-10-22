package com.ubox.card.device.hzsmk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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

import android.annotation.SuppressLint;

import com.ubox.card.bean.db.HZSMKTradeObj.HZSMKTrade;
import com.ubox.card.core.serial.IcCom;
import com.ubox.card.db.dao.HZSMKDao;
import com.ubox.card.util.Utils;
import com.ubox.card.util.device.DeviceUtils;
import com.ubox.card.util.encrypt.DESCoder;
import com.ubox.card.util.logger.Logger;

@SuppressLint("SimpleDateFormat")
public class HZSMKer {
	
	public static ArrayList<String> BlackCardLS = new ArrayList<String>();
	private static HZSMKer heser = new HZSMKer();

	private HZSMKer() {};

	/** 单例模式 获得实例*/
	public static HZSMKer getInstance() {

		return heser;
	}

	static enum RESULT { SUCCESS, CANCEL, TIMEOUT, ERROR, NORESPONSE };

	private IcCom com = new IcCom(57600, IcCom.DATABITS_8, IcCom.STOPBITS_1,IcCom.PARITY_NONE);

	/**
	 * 打开RS232串口 
	 * 波特：57600
	 */
	public RESULT open() {
		try {
			com.open();
		} catch (Exception e) {
			Logger.error("open rs232 is exception. " + e.getMessage(), e);
			return RESULT.ERROR;
		}
		return RESULT.SUCCESS;
	}
	
	/** 关闭串口 */
	public RESULT close() {
		try {
			com.close();
		} catch (Exception e) {
			Logger.error("close rs232 is exception. " + e.getMessage(), e);
			return RESULT.ERROR;
		}
		return RESULT.SUCCESS;
	}
	
	/**从服务器获取黑名单*/
	public void blackCard(){
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						Logger.info(">>>> get black card start... ");
						String url = "http://192.168.14.27:7080/vcardServer/hzsmk/blacklist?vmId=888888&appType=999";
						String charSet = "UTF-8";
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
							return;
						}
	
						vsResultJson = EntityUtils.toString(httpResponse.getEntity(), charSet);
						//解密数据
						byte[] resultJson = DESCoder.decryptBASE64(vsResultJson);
						String ResultJson = new String(resultJson, "UTF-8");
						
						JSONArray array = new JSONArray(ResultJson);
						int lenth = array.length();
						
						for (int i = 0; i < lenth; i++) {
							BlackCardLS.add(array.getString(i));
						}
						Logger.info(">>>> From VCardServer ResultJson :" + ResultJson);
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
			}
		}).start();
	}

	/** 复位刷卡器
	public RESULT resetCard() {
		try {
			Logger.info("resetCard is start");
			byte[] send = { (byte) 0xAA, 0x02, 0x21, 0x23, (byte) 0xCC };
			byte[] ret;
			int timeout = 1;
			int sleepTime = 500;
			try {
				ret = talkWith(send, timeout, sleepTime);
			} catch (TimeoutException e) {
				Logger.error("reset TimeoutException. " + e.getMessage(), e);
				return RESULT.TIMEOUT;
			} catch (Exception e) {
				Logger.error("reset Exception. " + e.getMessage(), e);
				return RESULT.ERROR;
			}
			
			if (ret == null) {
				Logger.error("reset receive bytes is null");
				return RESULT.ERROR;
			}
			if (ret[0] != (byte) 0xBB) {
				Logger.error("reset receive data head is error. " +Utils.toHex1(ret[0]));
				return RESULT.ERROR;
			}
			if (ret[2] != (byte) 0xCC) {
				Logger.error("reset receive data is error. " + Utils.toHex1(ret[2]));
				return RESULT.ERROR;
			}
			if (ret[1] != (byte) 0x00) {
				Logger.error("reset receive data tail is error. " + Utils.toHex1(ret[1]));
				return RESULT.ERROR;
			}
			return RESULT.SUCCESS;
		} catch (Exception e) {
			Logger.error("read is exception. " + e.getMessage(), e);
			return RESULT.ERROR;
		} finally {
			heser.close();
		}

	} 
	*/
	
	/** M1消费 */
	public RESULT M1pay(int mony) {
		try {
			Logger.info("M1pay is start");
			String money=Integer.toHexString(mony);
			if(money.length()!=8){
				int j=8-money.length();
				for(int i=1;i<=j;i++){
					money="0"+money;
				}
			}
			byte[] Money=Utils.decodeHex(money);//00 00 00 00 正向
			byte[] DMoney = { Money[3], Money[2], Money[1], Money[0] };
			String DM=Utils.toHex(DMoney);
			
			long time=System.currentTimeMillis();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			String Time=formatter.format(time);
			
			SimpleDateFormat F = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String T=F.format(time);
			M1Util.setTradTime(T);       //M1Util添加数据
			M1Util.setTradMoney(DM);  //M1Util添加数据
			
			byte[] day=Utils.decodeHex(Time);
			
			byte[] send = { (byte) 0xAA, 0x0D, (byte) 0xD2, Money[3], Money[2], Money[1],
					Money[0], day[0], day[1], day[2], day[3], day[4], day[5], day[6], 0x00, (byte) 0xCC };
			send[send.length-2]=lrc(send);
			
			byte[] ret;
			int timeout = 1;
			int sleepTime = 500;
			try {
				ret = talkWith(send, timeout, sleepTime);
			} catch (TimeoutException e) {
				Logger.error("M1pay TimeoutException. " + e.getMessage(), e);
				return RESULT.TIMEOUT;
			} catch (Exception e) {
				Logger.error("M1pay Exception. " + e.getMessage(), e);
				return RESULT.ERROR;
			}
			/**
			 * 返回数据
			 * BB 00 CC AA
			 * 23 D2
			 * 96 02 82 64-----------卡号
			 * 74 35 09 61-----------发行流水号
			 * F2 E7 20 EC-----------认证码
			 * 20 15 07 14 14 38 00--交易时间
			 * 01 00 00 00-----------交易金额
			 * 00 00 03 E6-----------原额
			 * 00 08-----------------钱包交易序列号(本笔)
			 * AE 4A 53 42-----------TAC
			 * 60 CC
			 * AA0DD20200000020150714143800d7CC  ----消费2分
			 * 
			 */
			
			if (ret == null) {
				Logger.error("M1pay receive bytes is null");
				return RESULT.ERROR;
			}
			if (ret[0] != (byte) 0xBB) {
				Logger.error("M1pay receive data is error. " +Utils.toHex1(ret[0]));
				return RESULT.ERROR;
			}
			if (ret[2] != (byte) 0xCC) {
				Logger.error("M1pay receive data is error. " + Utils.toHex1(ret[2]));
				return RESULT.ERROR;
			}
			if (ret[3] != (byte) 0xAA) {
				Logger.error("M1pay receive data is error. " + Utils.toHex1(ret[3]));
				return RESULT.ERROR;
			}
			if (ret[4] != (byte) 0x23) {
				Logger.error("M1pay receive data lenth is error. " + Utils.toHex1(ret[4]));
				return RESULT.ERROR;
			}
			if (ret[5] != (byte) 0xD2) {
				Logger.error("M1pay receive data command code is error. " + Utils.toHex1(ret[5]));
				return RESULT.ERROR;
			}
			
			String tac=Utils.toHex1(ret[35])+Utils.toHex1(ret[36])+Utils.toHex1(ret[37])+Utils.toHex1(ret[38]);
			String walletTradeNo=Utils.toHex1(ret[33])+Utils.toHex1(ret[34]);
			int walletTN = Integer.parseInt(walletTradeNo, 16);
			HZSMKUtil.setWalletTradeNo(walletTN+"");
			String psam="0";
			M1Util.setTAC(tac);
			M1Util.setPSAMId(psam);
			
			//M1灰交易
			if (ret[1] != (byte) 0x00) {
				if (ret[1] == (byte) 0x12) {
					//交易后原额
					String AfterTradMoney=Utils.toHex1(ret[29]) + Utils.toHex1(ret[30]) + Utils.toHex1(ret[31]) + Utils.toHex1(ret[32]);
					int ATMoney = Integer.parseInt(AfterTradMoney, 16);
					String cardId = HZSMKUtil.getCardNo();
					if (RESULT.SUCCESS == (readCard())) {
						if (!cardId.equals(HZSMKUtil.getCardNo())) {
							Logger.error("M1pay not the same card. "+cardId+","+HZSMKUtil.cardNo);
							return RESULT.ERROR;
						}
						if (ATMoney != HZSMKUtil.getInitialAmount()) {
							Logger.info("M1pay success. "+ATMoney+","+HZSMKUtil.getInitialAmount());
							HZSMKUtil.setTradType("20");
							return RESULT.SUCCESS;
						} else {
							Logger.info("restart M1pay money. ");
							ret = talkWith(send, timeout, sleepTime);
							if (ret[1] == (byte) 0x00) {
								HZSMKUtil.setTradType("20");
								Logger.info("M1pay success .");
								return RESULT.SUCCESS;
							} else {
								Logger.info("M1pay fail ."); 
								return RESULT.ERROR;
							}
						}
					} else {
						Logger.error("read card is error. "+ Utils.toHex1(ret[1]));
						return RESULT.ERROR;
					}
				}
				Logger.error("M1pay  is fail. " + Utils.toHex1(ret[1]));
				return RESULT.ERROR;
			}
			HZSMKUtil.setTradType("00");
			return RESULT.SUCCESS;
		} catch (Exception e) {
			Logger.error("M1pay is exception. " + e.getMessage(), e);
			return RESULT.ERROR;
		}
	}
	
	/** CPU消费 */
	public RESULT CPUpay(int mony) {
		try {
			Logger.info("CPUpay is start");
			String money=Integer.toHexString(mony);
			if(money.length()!=8){
				int j=8-money.length();
				for(int i=1;i<=j;i++){
					money="0"+money;
				}
			}
			byte[] Money=Utils.decodeHex(money);//00 00 00 00 正向
			
			byte[] DMoney = { Money[3], Money[2], Money[1], Money[0] };
			String DM=Utils.toHex(DMoney);
			
			long time=System.currentTimeMillis();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			String Time=formatter.format(time);
			
			SimpleDateFormat F = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String T=F.format(time);
			
			M1Util.setTradTime(T);       //M1Util添加数据
			M1Util.setTradMoney(DM);	 //M1Util添加数据
			
			byte[] day=Utils.decodeHex(Time);
			
			byte[] send = { (byte) 0xAA, 0x0D, (byte) 0xD9, Money[0], Money[1], Money[2],
					Money[3], day[0], day[1], day[2], day[3], day[4], day[5], day[6], 0x00, (byte) 0xCC };
			send[send.length-2]=lrc(send);
			
			byte[] ret;
			int timeout = 1;
			int sleepTime = 500;
			try {
				ret = talkWith(send, timeout, sleepTime);
			} catch (TimeoutException e) {
				Logger.error("CPUpay TimeoutException. " + e.getMessage(), e);
				return RESULT.TIMEOUT;
			} catch (Exception e) {
				Logger.error("CPUpay Exception. " + e.getMessage(), e);
				return RESULT.ERROR;
			}
			/**
			 * 返回数据
			 * BB 00 CC AA
			 * 14 D9
			 * 10 00 00 00 99 80 16 05------应用序列号
			 * 00 01------------------------钱包交易序列号(上笔)
			 * 95 A7 33 6B------------------TAC
			 * 00 00 00 01------------------PSAM脱机号
			 * BD CC
			 * AA0DD90000000120150715105600B4CC  ----消费1分
			 * 
			 */
			if (ret == null) {
				Logger.error("CPUpay receive data is null");
				return RESULT.ERROR;
			}
			if (ret[0] != (byte) 0xBB) {
				Logger.error("CPUpay receive data is error. " +Utils.toHex1(ret[0]));
				return RESULT.ERROR;
			}
			if (ret[2] != (byte) 0xCC) {
				Logger.error("CPUpay receive data is error. " + Utils.toHex1(ret[2]));
				return RESULT.ERROR;
			}
			if (ret[3] != (byte) 0xAA) {
				Logger.error("CPUpay receive data is error. " + Utils.toHex1(ret[3]));
				return RESULT.ERROR;
			}
			if (ret[4] != (byte) 0x14) {
				Logger.error("CPUpay receive data lenth is error. " + Utils.toHex1(ret[4]));
				return RESULT.ERROR;
			}
			if (ret[5] != (byte) 0xD9) {
				Logger.error("CPUpay receive data command code is error. " + Utils.toHex1(ret[5]));
				return RESULT.ERROR;
			}
			
			String tac=Utils.toHex1(ret[16])+Utils.toHex1(ret[17])+Utils.toHex1(ret[18])+Utils.toHex1(ret[19]);
			String psam=Utils.toHex1(ret[20])+Utils.toHex1(ret[21])+Utils.toHex1(ret[22])+Utils.toHex1(ret[23]);
			String walletTradeNo=Utils.toHex1(ret[14])+Utils.toHex1(ret[15]);
			int walletTN = Integer.parseInt(walletTradeNo, 16);
			HZSMKUtil.setWalletTradeNo(walletTN+"");
			Long pa =Long.parseLong(psam, 16);//16进制转10进制   long类型
			M1Util.setTAC(tac);
			M1Util.setPSAMId(pa+"");
			
			//CPU灰交易
			if (ret[1] != (byte) 0x00) {
				if (ret[1] == (byte) 0x12) {
					//CPU消费验证
					CPUVerification(ret);
					if (M1Util.IsTrad.equals("01")) {
						//直接将上条灰记录的置为正常记录，并将取回的TAC填入并发送同时给控制层出货的命令。
						M1Util.setTAC(M1Util.getCtac());
						return RESULT.SUCCESS;
					}
					if (M1Util.IsTrad.equals("00")) {
						String id = HZSMKUtil.cardNo;
						String moneys = HZSMKUtil.getWalletBalance();
						if (RESULT.SUCCESS == (readCard())) {
							if(!id.equals(HZSMKUtil.cardNo)){
								Logger.error("CPUpay not the same card. "+id+","+HZSMKUtil.cardNo);
								return RESULT.ERROR;
							}
							if(!moneys.equals(HZSMKUtil.getWalletBalance())){
								M1Util.setPSAMId("90ABCDEF");
								Logger.info("CPUpay success money:"+moneys+",after:"+HZSMKUtil.walletBalance);
								HZSMKUtil.setTradType("26");
								return RESULT.SUCCESS;
							}
						    if (moneys.equals(HZSMKUtil.getWalletBalance())) {
							Logger.info("restart CPUpay money. ");
							ret = talkWith(send, timeout, sleepTime);
							if (ret[1] == (byte) 0x00) {
								HZSMKUtil.setTradType("26");
								Logger.info("CPUpay success .");
								return RESULT.SUCCESS;
							} else {
								Logger.info("CPUpay fail .");
								return RESULT.ERROR;
								}
							}
						} else {
							Logger.error("read card is error. "+ Utils.toHex1(ret[1]));
							return RESULT.ERROR;
						}
					}
				}
				Logger.error("CPUpay is fail. " + Utils.toHex1(ret[1]));
				return RESULT.ERROR;
			}
			HZSMKUtil.setTradType("06");
			return RESULT.SUCCESS;
		} catch (Exception e) {
			Logger.error("CPUpay is exception. " + e.getMessage(), e);
			return RESULT.ERROR;
		}

	}
	
	/**CPU消费验证*/
	private void CPUVerification(byte[] byt) {
		try{
			byte[] send = { (byte) 0xAA, 0x06, (byte) 0x5B, (byte) 0xD9, 0x06,byt[14], byt[15], (byte) 0xCC };
			byte[] Cret = null;
			int timeout = 1;
			int sleepTime = 500;
			try {
				Cret = talkWith(send, timeout, sleepTime);
			} catch (TimeoutException e) {
				Logger.error("read TimeoutException. " + e.getMessage(), e);
			} catch (Exception e) {
				Logger.error("read Exception. " + e.getMessage(), e);
			}
			if (Cret == null) {
				Logger.error("read card data is null");
			}
			String IsTrad = Utils.toHex1(Cret[7]);
			String Ctac = Utils.toHex1(Cret[7])+Utils.toHex1(Cret[7])+Utils.toHex1(Cret[7])+Utils.toHex1(Cret[7]);
			M1Util.setIsTrad(IsTrad);
			M1Util.setCtac(Ctac);
		}catch(Exception e){
			Logger.error("read is exception. " + e.getMessage(), e);
		}
	}

	/** 读取卡信息*/
	public RESULT readCard(){
		try {
			Logger.info("card read is start");
			//AA 02 D4 D6 CC
			byte[] send = { (byte) 0xAA, 0x02, (byte) 0xD4, (byte) 0xD6, (byte) 0xCC };
			byte[] ret;
			int timeout = 1;
			int sleepTime = 500;
			try {
				ret = talkWith(send, timeout, sleepTime);
			} catch (TimeoutException e) {
				Logger.error("read TimeoutException. " + e);
				return RESULT.ERROR;
			} catch (Exception e) {
				Logger.error("read Exception. " + e);
				return RESULT.ERROR;
			}
			
			/**
			 * BB 00 CC AA  头                   			00 00 cc 00     
			 * 43     		长度				43
			 * D4 			命令码			d4
			 * 0F 56 B4 12     卡号				0f 56 00 12
			 * 00 00 00 00     发行流水号			00 00 00 00
			 * 00 00 00 00    认证码				00 00 00 00
			 * 31 00		城市代码			31 00 
			 * 00 00 		行业代码			00 00
			 * 00 			M1启用标志			00
			 * 17 			卡类型, 市民卡 18	17
			 * 20 65 01 09	有效日期			20 65 00 09
			 * 20 15 01 09 	启用日期			20 15 01 09
			 * 20 15 01 19	加款日期			00 15 01 19
			 * 00 00 00 00 	M1上次充值后余额		00 00 00 00
			 * E8 03 00 00	钱包区金额（逆） 00 00 03 E8   e7 00 00 00
			 * 00 01 		钱包交易序列号		00 00
			 * 00 			M1黑名单标志		00
			 * 99 01 01		年检日期			99 01 01
			 * D7 68 42 DD   本次操作员卡号 		00 68 42 dd
			 * 39 			SAK值			39
			 * 10 00 00 00 99 80 16 05    应用序列号		00 00 00 00 00 80 16 05
			 * 00 			卡子类型			00
			 * 01 			CPU卡钱包启用标志	00
			 * 31 00 01 05 40 11     住建部PSAM卡号	31 00 01 05 00 11
			 * 09 						09
			 * CC						cc
			 * */
			if (ret == null) {
				Logger.error("read card bytes is null");
				return RESULT.ERROR;
			}
			if (ret[0] != (byte) 0xBB) {
				Logger.error("read card data head is error. " +Utils.toHex1(ret[0]));
				return RESULT.ERROR;
			}
			if (ret[1] != (byte) 0x00) {
				if (ret[1] == (byte) 0x17) {
					Logger.info("test position no response. " + Utils.toHex1(ret[1]));
					return RESULT.NORESPONSE;
				}
				Logger.error("test position is error. " + Utils.toHex1(ret[1]));
				return RESULT.ERROR;
			}
			if (ret[4] != (byte) 0x43) {
				Logger.error("read card data lenth is error. " + Utils.toHex1(ret[4]));
				return RESULT.ERROR;
			}
			if (ret[5] != (byte) 0xD4) {
				Logger.error("command code is error. " + Utils.toHex1(ret[5]));
				return RESULT.ERROR;
			}
//			if (ret[72] != (byte) 0xCC) {
//				Logger.error("read card data tail is error. " + Utils.toHex1(ret[1]));
//				return RESULT.ERROR;
//			}
			
			String cardNo = Utils.toHex1(ret[6]) + Utils.toHex1(ret[7])+ Utils.toHex1(ret[8]) + Utils.toHex1(ret[9]);
			String releaseNo = Utils.toHex1(ret[10]) + Utils.toHex1(ret[11])+ Utils.toHex1(ret[12]) + Utils.toHex1(ret[13]);
			String certifyCode = Utils.toHex1(ret[14]) + Utils.toHex1(ret[15])+ Utils.toHex1(ret[16]) + Utils.toHex1(ret[17]);
			String cityCode = Utils.toHex1(ret[18]) + Utils.toHex1(ret[19]);
			String industryCode = Utils.toHex1(ret[20]) + Utils.toHex1(ret[21]);
			String m1UseFlag = Utils.toHex1(ret[22]);
			String cardType = Utils.toHex1(ret[23]);
			String validDate = Utils.toHex1(ret[24]) + Utils.toHex1(ret[25]) + Utils.toHex1(ret[26]) + Utils.toHex1(ret[27]);
			String useDate = Utils.toHex1(ret[28]) + Utils.toHex1(ret[29]) + Utils.toHex1(ret[30]) + Utils.toHex1(ret[31]);
			String addMoneyDate = Utils.toHex1(ret[32]) + Utils.toHex1(ret[33]) + Utils.toHex1(ret[34]) + Utils.toHex1(ret[35]);
			String m1AddMoneyBalance = Utils.toHex1(ret[36]) + Utils.toHex1(ret[37]) + Utils.toHex1(ret[38]) + Utils.toHex1(ret[39]);
			//逆向金额
			String walletBalance = Utils.toHex1(ret[40]) + Utils.toHex1(ret[41]) + Utils.toHex1(ret[42]) + Utils.toHex1(ret[43]);
//			String walletTradeNo = Utils.toHex1(ret[44]) + Utils.toHex1(ret[45]);
			String m1BlackFlag = Utils.toHex1(ret[46]);
			String checkDate = Utils.toHex1(ret[47]) + Utils.toHex1(ret[48]) + Utils.toHex1(ret[49]);
			String nowOperatorNo = Utils.toHex1(ret[50]) + Utils.toHex1(ret[51]) + Utils.toHex1(ret[52]) + Utils.toHex1(ret[53]);
			String sakValue = Utils.toHex1(ret[54]);
			String appNo = Utils.toHex1(ret[55]) + Utils.toHex1(ret[56])+ Utils.toHex1(ret[57]) + Utils.toHex1(ret[58])
					+ Utils.toHex1(ret[59]) + Utils.toHex1(ret[60])+ Utils.toHex1(ret[61]) + Utils.toHex1(ret[62]);
			String subCardType = Utils.toHex1(ret[63]);
			String brushSeq = Utils.toHex1(ret[64]);
			String psamCardNo = Utils.toHex1(ret[65]) + Utils.toHex1(ret[66]) + Utils.toHex1(ret[67]) + Utils.toHex1(ret[68])
					+ Utils.toHex1(ret[69]) + Utils.toHex1(ret[70]);
			
			//M1上次充值后余额
			int m1ABalance =Integer.parseInt(m1AddMoneyBalance, 16); 
			//正向金额
			String initialAmount=Utils.toHex1(ret[43]) + Utils.toHex1(ret[42]) + Utils.toHex1(ret[41]) + Utils.toHex1(ret[40]);
			int amount = Integer.parseInt(initialAmount, 16); //16进制转10进制
//			int sakVe = Integer.parseInt(sakValue, 16);
			int subCT = Integer.parseInt(subCardType, 16);
			
			HZSMKUtil.setCarId(cardNo);
			HZSMKUtil.setCardNo(cardNo);
			HZSMKUtil.setReleaseNo(releaseNo);
			HZSMKUtil.setCertifyCode(certifyCode);
			HZSMKUtil.setCityCode(cityCode);
			HZSMKUtil.setIndustryCode(industryCode);
			HZSMKUtil.setM1UseFlag(m1UseFlag);
			HZSMKUtil.setCardType(cardType);
			HZSMKUtil.setValidDate(validDate);
			HZSMKUtil.setUseDate(useDate);
			HZSMKUtil.setAddMoneyDate(addMoneyDate);
			HZSMKUtil.setM1AddMoneyBalance(m1ABalance+"");
			HZSMKUtil.setWalletBalance(walletBalance);
			
			HZSMKUtil.setM1BlackFlag(m1BlackFlag);
			HZSMKUtil.setCheckDate(checkDate);
			HZSMKUtil.setNowOperatorNo(nowOperatorNo);
			HZSMKUtil.setSakValue(sakValue);
			HZSMKUtil.setAppNo(appNo);
			HZSMKUtil.setSubCardType(subCT+"");
			HZSMKUtil.setBrushSeq(brushSeq);
			HZSMKUtil.setPsamCardNo(psamCardNo);
			HZSMKUtil.setInitialAmount(amount);
			return RESULT.SUCCESS;
		} catch (Exception e) {
			Logger.error("read is exception. " + e.getMessage(), e);
			return RESULT.ERROR;
		}
		
	}

	/** 与串口通信 */
	private byte[] talkWith(byte[] cmd, int timeout, int sleepTime)throws Exception {
		try {
			OutputStream out = com.getOutputStream();
			InputStream in = com.getInputStream();

			out.write(cmd);
			Logger.info("IO send : " + DeviceUtils.byteArray2HASCII(cmd));

			long startTime = System.currentTimeMillis();
			while (in.available() < 0) {
				long endTime = System.currentTimeMillis();
				if (endTime - startTime > timeout) {
					Logger.warn("IO read time out");
					throw new TimeoutException();
				}
			}

			Thread.sleep(sleepTime);
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			while (in.available() > 0) {
				bao.write(in.read());
			}
			byte ret[] = bao.toByteArray();
			Logger.info("IO receive: " + DeviceUtils.byteArray2HASCII(ret));
			
			return ret;

		} catch (Exception e) {
			Logger.error("talkWith is exception. " + e.getMessage(), e);
			throw e;
		}
	}
	
	/**
	 * 计算LRC
	 * @param cmd 命令指令 
	 */
	public static byte lrc(byte[] cmd){
		byte b = (byte)0x00;
		if(cmd.length<2){
			return b; 
		}
		b = cmd[1];
		for(int i=2,j=cmd.length-1;i<j;i++){
			b ^= cmd[i];
		}
		return b;
	}

	private static class TimeoutException extends RuntimeException {
		private static final long serialVersionUID = -3132351707162593440L;

	}

	public void PreservationData(final HZSMKTrade hzsmkdt) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Logger.info("HZSMKTradeHandler Runnable start....");
				HZSMKDao.getInstance().insertOne(hzsmkdt);// 入库操作
				Logger.info("HZSMKTradeHandler Runnable end....");
			}
		}).start();
		
	}

}
