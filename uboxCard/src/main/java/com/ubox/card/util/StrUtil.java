package com.ubox.card.util;

import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtil {
	
	/**
	 * 去除字符串里面的空白字符
	 * @param str 字符串
	 * @return 去除空白的字符串
	 */
	public static String trim(String str) {
		// str = str.replace("\n", "");
		// str = str.replace("\r\n", "");
		Pattern p = Pattern.compile("\\s*|\t|\r|\n");
        Matcher m = p.matcher(str);
        return  m.replaceAll("");
	}
	
	/**
	 * 生成流水号
	 * @return 流水号
	 */
	@SuppressLint("SimpleDateFormat")
	public static String genSeq() {
		Random random = new Random();
		String str = "000"+random.nextInt(999);
		str = str.substring(str.length()-3,str.length());
		
		SimpleDateFormat dateFm = new SimpleDateFormat("yyMMddHHmmss"); 
		return dateFm.format(new java.util.Date())+str;
		
	}
   
	/**
	 * 生成随机的订单号
	 * @return 最大值int
	 */
//	public static String genOrderNo() {
//		int randMax = (int)((Math.random() * (Integer.MAX_VALUE - 100000) + 99999));
//		return String.valueOf(randMax);
//	}
}
