package com.ubox.card.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

import com.ubox.card.business.DeviceWorkProxy;
import com.ubox.card.util.logger.Logger;

public class Utils {
	/**
	 * @author lvjinhua May 10, 2010 5:55:44 PM
	 * @version 1.0
	 * @since   全部转换为字符串
	 * @param hash
	 * @return
	 */
	public static final String toHex(byte hash[]) {
		if (hash == null) {
			return "";
		}
		StringBuffer buf = new StringBuffer(hash.length * 2);
		int i;

		for (i = 0; i < hash.length; i++) {
			if (((int) hash[i] & 0xff) < 0x10) {
				buf.append("0");
			}
			buf.append(Long.toString((int) hash[i] & 0xff, 16));
		}
		return buf.toString();
	}

	/**
	 * @author lvjinhua May 10, 2010 5:56:18 PM
	 * @version 1.0
	 * @since
	 * @param datas
	 * @return
	 */
	public static String toStr(byte[] datas) {
		if (datas == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		if (datas != null && datas.length > 0) {
			for (byte d : datas) {
				sb.append((char) (d));
			}
		}
		return sb.toString();
	}

	public static int toInt(String v) {
		return Integer.valueOf(v);
	}

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// e.printStackTrace();
		}
	}
	/**十六进制转换为十进制*/
	public static int makeUint8(byte b) {
		return (0xff & b);
	}

	public static int makeUint16(byte b, byte c) {
		return (0xff & b) << 8 | (0xff & c);
	}
	
	public static int makeUint3(byte d,byte b, byte c) {
		return ((0xff & d) << 16)|((0xff & b) << 8) | (0xff & c);
	}
	
	public static int makeUint32(byte b, byte c, byte d, byte e) {
		return (((0xff & b) << 24) | ((0xff & c) << 16) | ((0xff & d) << 8) | ((0xff & e) << 0));
	}
	/**十进制转换为2进制*/
	public static byte[] makeByte4(int v) {
		byte[] buf = { (byte) ((v & 0xff000000) >> 32),
				(byte) ((v & 0xff0000) >> 16), (byte) ((v & 0xff00) >> 8),
				(byte) (v & 0xff) };
		return buf;
	}

	public static byte[] makeByte3(int v) {
		byte[] buf = { (byte) ((v & 0xff0000) >> 16),
				(byte) ((v & 0xff00) >> 8), (byte) (v & 0xff) };
		return buf;
	}

	public static byte[] makeByte2(int v) {
		byte[] buf = { (byte) ((v & 0xff00) >> 8), (byte) (v & 0xff) };
		return buf;
	}
	/**十进制转换为十六进制*/
	public static byte makeByte1(int v) {
		return (byte) (v & 0xff);
	}

	public static String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		try {
			t.printStackTrace(pw);
			return sw.toString();
		} finally {
			pw.close();
			try {
				sw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**INT数组转换为十六进制字符串*/
	public static final String toHex(int hash[]) {
		if (hash == null) {
			return "";
		}
		StringBuffer buf = new StringBuffer(hash.length * 2);
		int i;

		for (i = 0; i < hash.length; i++) {
			if ((hash[i] & 0xff) < 0x10) {
				buf.append("0");
			}
			buf.append(Long.toString(hash[i] & 0xff, 16));
		}
		return buf.toString();
	}

	public static List<String> readFile(String filePath, String sperator) {
		BufferedReader reader = null;
		List<String> list = new ArrayList<String>();
		try {
			if (TextUtils.isEmpty(filePath))
				return null;
			File tempFile = new File(filePath);
			if (!(tempFile.exists() && tempFile.length() > 0)) {
				return null;
			}

			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(tempFile), "gb2312"));
			String line = null;
			if ((line = reader.readLine()) != null) {
				if (line.contains(sperator)) {
					String[] str = line.split(sperator);
					for (int i = 0, j = str.length; i < j; i++) {
						list.add(str[i]);
					}
				}
			}
		} catch (Exception e) {
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (Exception e) {
			}
		}
		return list;
	}

	/**
	 * 截取字节数组
	 * 
	 * @param parentArray
	 * @param start
	 *            开始位置
	 * @param length
	 *            截取长度
	 * @return
	 * @author gaolei
	 */
	public static byte[] subArray(byte[] parentArray, int start, int length) {
		if (start < 0) {
			throw new ArrayIndexOutOfBoundsException(start);
		}
		if (length < 0) {
			throw new ArrayIndexOutOfBoundsException(length);
		}

		int parentArrayLength = parentArray.length;
		if (parentArrayLength < start) {
			throw new ArrayIndexOutOfBoundsException(start);
		}
		if (parentArrayLength < length - start || parentArrayLength < length) {
			throw new ArrayIndexOutOfBoundsException(length);
		}

		byte[] resultArray = new byte[length];

		for (int i = start; i < start + length; i++) {
			resultArray[i - start] = parentArray[i];
		}

		return resultArray;
	}

	/**
	 * 复制字节数组
	 * 
	 * @param parentArray
	 * @return
	 * @author gaolei
	 */
	public static byte[] subArray(byte[] parentArray) {
		return subArray(parentArray, 0, parentArray.length);
	}

	/**
	 * 截取字节数组
	 * 
	 * @param parentArray
	 * @param start
	 *            开始位置
	 * @return
	 * @author gaolei
	 */
	public static byte[] subArray(byte[] parentArray, int start) {
		return subArray(parentArray, start, parentArray.length - start);
	}

	/**
	 * 取反操作
	 * 
	 * @param buff
	 * @return
	 * @author gaolei
	 */
	public static byte[] reverse(byte[] buff) {
		byte[] resultByte = new byte[buff.length];
		for (int i = 0; i < buff.length; i++) {
			int b = 0;
			for (int j = 0; j < 8; j++) {
				int bit = (buff[i] >> j & 1) == 0 ? 1 : 0;
				b += (1 << j) * bit;
			}
			resultByte[i] = (byte) b;
		}
		return resultByte;
	}
	
	public static int getSerialId(String serialName) {
		int serialId = 6;
		if ((serialName == null) || ("".equals(serialName))) {
			return serialId;
		}

		String[] com = { "com1", "com2", "com3", "com4", "com5", "com6",
				"com7", "com8", "com9", "com10" };
		for (int i = 0; i < com.length; i++) {
			if (serialName.equalsIgnoreCase(com[i])) {
				serialId = i;
			}
		}
		return serialId;
	}
	
	/**byte 转换为 int 类型*/
	public static final String toHex1(byte hash) {
		StringBuffer buf = new StringBuffer("");

		if (((int) hash & 0xff) < 0x10) {
			buf.append("0");
		}
		buf.append(Long.toString((int) hash & 0xff, 16));

		return buf.toString();
	}

	/**
	 * 8字节数组转为Long @gaolei 2013-04-09
	 * 
	 * @param b
	 * @return
	 */
	public static long byteToLong(byte[] b) {
		if (b.length != 8) {
			return 0L;
		}

		return ((((long) b[0] & 0xff) << 56) | (((long) b[1] & 0xff) << 48)
				| (((long) b[2] & 0xff) << 40) | (((long) b[3] & 0xff) << 32)
				| (((long) b[4] & 0xff) << 24) | (((long) b[5] & 0xff) << 16)
				| (((long) b[6] & 0xff) << 8) | (((long) b[7] & 0xff) << 0));
	}

	/**
	 * short转换到字节数组 @gaolei 2013-04-09
	 * 
	 * @param number
	 * @return
	 */
	public static byte[] shortToByte(short number) {
		byte[] b = new byte[2];
		for (int i = 1; i >= 0; i--) {
			b[i] = (byte) (number % 256);
			number >>= 8;
		}
		return b;
	}

	/**
	 * 2字节到short转换 @gaolei 2013-04-09
	 * 
	 * @param b
	 * @return
	 * @throws DataTranslateException
	 */
	public static short byteToShort(byte[] b){
		if (b.length != 2){
			return 0;
		}

		return (short) ((((b[0] & 0xff) << 8) | b[1] & 0xff));
	}

	/**
	 * 整型转换到字节数组 @gaolei 2013-04-09
	 * 
	 * @param number
	 * @return
	 */
	public static byte[] intToByte(int number) {
		byte[] b = new byte[4];
		for (int i = 3; i >= 0; i--) {
			b[i] = (byte) (number % 256);
			number >>= 8;
		}
		return b;
	}

	/**
	 * 4字节数组到整型转换 @gaolei 2013-04-09
	 * 
	 * @param b
	 * @return
	 * @throws DataTranslateException
	 */
	public static int byteToInt(byte[] b){
		if (b.length != 4){
			return 0;
		}
		return (int) ((((b[0] & 0xff) << 24) | ((b[1] & 0xff) << 16)
				| ((b[2] & 0xff) << 8) | ((b[3] & 0xff) << 0)));
	}

	/**
	 * long转换到字节数组 @gaolei 2013-04-09
	 * 
	 * @param number
	 * @return
	 */
	public static byte[] longToByte(long number) {
		byte[] b = new byte[8];
		for (int i = 7; i >= 0; i--) {
			b[i] = (byte) (number % 256);
			number >>= 8;
		}
		return b;
	}

	/**
	 * 8字节数组到double转换 @gaolei 2013-04-09
	 * 
	 * @param b
	 * @return
	 * @throws DataTranslateException
	 */
	public static double byteToDouble(byte[] b) {
		if (b.length != 8){
			return 0;
		}
		long l;
		l = b[0];
		l &= 0xff;
		l |= ((long) b[1] << 8);
		l &= 0xffff;
		l |= ((long) b[2] << 16);
		l &= 0xffffff;
		l |= ((long) b[3] << 24);
		l &= 0xffffffffl;
		l |= ((long) b[4] << 32);
		l &= 0xffffffffffl;

		l |= ((long) b[5] << 40);
		l &= 0xffffffffffffl;
		l |= ((long) b[6] << 48);
		l &= 0xffffffffffffffl;

		l |= ((long) b[7] << 56);

		return Double.longBitsToDouble(l);
	}

	/**
	 * 4字节数组到float的转换 @gaolei 2013-04-09
	 * 
	 * @param b
	 * @return
	 * @throws DataTranslateException
	 */
	public static float byteToFloat(byte[] b) {
		if (b.length != 4){
			return 0;
		}

		int l;
		l = b[0];
		l &= 0xff;
		l |= ((long) b[1] << 8);
		l &= 0xffff;
		l |= ((long) b[2] << 16);
		l &= 0xffffff;
		l |= ((long) b[3] << 24);
		l &= 0xffffffffl;
		
		return Float.intBitsToFloat(l);
	}

	/**
	 * 字符串到字节数组转换 @gaolei 2013-04-09
	 * 
	 * @param s
	 * @return
	 */
	public static byte[] stringToByte(String s) {
		return s.getBytes();
	}

	/**
	 * 字节数组带字符串的转换 @gaolei 2013-04-09
	 * 
	 * @param b
	 * @return
	 */
	public static String byteToString(byte[] b) {
		return new String(b);

	}

	/**
	 * double类型的数值转化为整数。
	 * 主要应用：元单位的金额(##.00)，转化为分单位的金额(##)。
	 * @param d
	 * @return
	 */
	public static int doubleToInt(double d) {
		int re = 0;
		try {
			DecimalFormat decimalFormat = new DecimalFormat("###.00");
			String s = decimalFormat.format(d);
			String[] s2 = s.split("\\.");
			if (s2[1].length() > 2) {
				s2[1] = s2[1].substring(0, 2);
			}
			for (int i = s2[1].length(); i < 2; i++) {
				s2[1] = s2[1] + 0; 
			}
			re = Integer.parseInt(s2[0] + s2[1]);
		} catch (Exception e) {
			e.getStackTrace();
		}
		return re;
	}
	
	/**
	 * 16进制的字符串转16进制字节数组
	 * @param data
	 * @return
	 */
	public static byte[] decodeHex(String data){
		if(null == data){
			return null;
		}

		if((data.length() % 2) != 0){
			throw new RuntimeException(" Odd number of characters. "+data.length());
		}
		
		byte[] out = new byte[data.length()/2];
		try {
			for(int i=0,j=data.length();i<j;){
				String tmp = data.substring(i, i+2);
				i = i + 2;
				out[i/2-1] = Utils.intToByte(Integer.parseInt(tmp, 16))[3];
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return out;
	}
	
	/**
	 * 
	 * 字节数组转化为字符数组
	 * @param b
	 * @param offSet
	 * @param count
	 * @return
	 */
	public static char[] byteToChars(byte[] b,int offSet,int count){
		char[] ch = new char[count];
		for(int i = offSet; i < offSet + count; i++){
			ch[i-offSet] = (char)b[i];
		}
		return ch;
	}
	/**
	 * 字节数组转化为字符数组
	 * @param b
	 * @return
	 */
	public static char[] byteToChars(byte[] b){
		return byteToChars(b, 0, b.length);
	}
	
	/**
	 * 
	 * 字节数组转化为字符型(ASCII)的字符串
	 * @param b
	 * @param offSet 
	 * @param count 
	 * @return
	 */
	public static String byteToString(byte[] b,int offSet,int count){
		return String.valueOf(byteToChars(b, offSet, count));
	}
	
	/**
	 * 
	 * 16进制转2进制
	 * @return
	 */
	public static String hexString2binaryString(String hexString) {
		if (hexString == null || hexString.length() % 2 != 0)
			return null;
		String bString = "", tmp;
		try {
			for (int i = 0; i < hexString.length(); i++) {
				tmp = "0000"
						+ Integer.toBinaryString(Integer.parseInt(
								hexString.substring(i, i + 1), 16));
				bString += tmp.substring(tmp.length() - 4);
			}
		} catch (Exception e) {
			Logger.error("Exception : " + e.getMessage());
		}
		return bString;
	}
	
	/**
	 * 字节数组转化为字符型(ASCII)的字符串
	 * @param ch
	 * @return
	 */
	public static String charsToString(byte[] b){
		return String.valueOf(byteToChars(b));
	}
	
	/**
	 * int转换到字节数组
	 * @param moeny
	 * @return
	 */
	public static byte[] intTo12Byte(int money) {
		byte[] data = null;
		try{
			String moneyStr = "000000000000" + String.valueOf(money);
			String moneysubStr = moneyStr.substring(moneyStr.length() - 12,
					moneyStr.length());// 截取出12位的字符串

			data = new byte[12];
			for (int i = 0; i < moneysubStr.length(); i++) {
				char c = moneysubStr.charAt(i);
				data[i] = (byte) c;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return data;
	}
	
	/**
	 * int转换到字节数组
	 * @param moeny
	 * @return
	 */
	public static byte[] intToHASCIIByte(int len,int money) {
		byte[] data = null;
		try{
			String moneyStr ="";
			for(int m=0;m<len;m++){
				moneyStr = moneyStr+"0";
			}
			moneyStr = moneyStr + String.valueOf(money);
			String moneysubStr = moneyStr.substring(moneyStr.length() - len,
					moneyStr.length());// 截取出12位的字符串

			data = new byte[len];
			for (int i = 0; i < moneysubStr.length(); i++) {
				char c = moneysubStr.charAt(i);
				data[i] = (byte) c;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return data;
	}

	public static boolean isCancel (String serNo){
		if(true == DeviceWorkProxy.CANCELMAP.containsKey(serNo)){
			Logger.info(">>>>>> cancel is success. serNo:"+serNo+" <<<<<<");
			return true;
		}else{
			return false;
		}
	}

	public static String stringToHex(String str) {

		char[] chars = str.toCharArray();

		StringBuffer hex = new StringBuffer();
		for (int i = 0; i < chars.length; i++) {
			hex.append(Integer.toHexString((int) chars[i]));
		}

		return hex.toString();
	}


	/**
	 * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用
	 *
	 * @param src    byte数组
	 * @param offset 从数组的第offset位开始
	 * @return int数值
	 */

	public static int bytesToInt(byte[] ary, int offset) {
		int value;
		value = (int) ((ary[offset] & 0xFF)
				| ((ary[offset + 1] << 8) & 0xFF00)
				| ((ary[offset + 2] << 16) & 0xFF0000)
				| ((ary[offset + 3] << 24) & 0xFF000000));
		return value;
	}
}
