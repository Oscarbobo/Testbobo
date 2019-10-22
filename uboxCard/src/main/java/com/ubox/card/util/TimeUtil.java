package com.ubox.card.util;

import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;

public class TimeUtil {
	
	
	@SuppressLint("SimpleDateFormat")
	public static String getCurrentTime(){
		SimpleDateFormat dateFm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		return dateFm.format(new java.util.Date());
	}
	
	@SuppressLint("SimpleDateFormat")
	public static String getCurrentDate(){
		SimpleDateFormat dateFm = new SimpleDateFormat("yyyyMMddHHmmss"); 
		return dateFm.format(new java.util.Date());
	}
}
