package com.ubox.card.util.logger;

import android.util.Log;
import com.ubox.card.deploy.Writer;
import com.ubox.util.UboxLog;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Logger {
	
	private static final String  PACKBOX_TAG    = "UboxCard";

	public static void info(String msg) {
		String im = ">>>>INFO: " + msg;
		com.ubox.util.log.Logger.info(PACKBOX_TAG, msg);
		Log.i(PACKBOX_TAG, im);
	}
	
	public static void warn(String msg) {
		String wm = ">>>>WARN: " + msg;
		com.ubox.util.log.Logger.warn(PACKBOX_TAG, msg);
		Log.w(PACKBOX_TAG, wm);
	}
	
	public static void error(String msg) {
		String em = ">>>>ERROR: " + msg;
		com.ubox.util.log.Logger.error(PACKBOX_TAG, msg);
		Log.e(PACKBOX_TAG, em);
	}
	
	public static void error(String msg, Throwable tr) {
		String em = ">>>>ERROR: " + msg;
		com.ubox.util.log.Logger.error(PACKBOX_TAG, msg);
		saveE(tr);
		Log.e(PACKBOX_TAG, em, tr);
	}

	public static void saveE(Throwable ex){
		StringBuffer sb = new StringBuffer();
		StringWriter writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		ex.printStackTrace(pw);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(pw);
			cause = cause.getCause();
		}
		pw.close();
		String result = writer.toString();
		sb.append(result);
		UboxLog.get().log(PACKBOX_TAG, sb.toString());//保存到日志文件
	}
	
	public static void debug(String msg) {
		Log.d(PACKBOX_TAG, ">>>>DEBUG: " + msg);
	}
}
