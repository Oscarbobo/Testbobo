package com.ubox.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import android.text.TextUtils;

public class UboxLog {

	private static UboxLog instance = new UboxLog();;

	public static UboxLog get() {
		return instance;
	}

	private String currentDate;
	private String tagName; // 区分TAG
	private HashMap<String, FileOutputStream> logFileHash;
	private File logDir = new File("/mnt/sdcard/Ubox/log/");

	private UboxLog() {
		logFileHash = new HashMap<String, FileOutputStream>();
	}

	/**
	 * 打log到 /Ubox/log/tag/yyyyMMdd.log文件当中
	 * 
	 * @param tag
	 * @param info
	 */
	public void log(String tag, String info) {
		SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss:SSS");

		SimpleDateFormat mDayFormat = new SimpleDateFormat("yyyyMMdd");

		try {

			String today = mDayFormat.format(new Date(System
					.currentTimeMillis()));
			// 如果日期发生变化，关掉相关的tag文件
			if (TextUtils.isEmpty(tagName) || !tagName.equals(tag)
					|| TextUtils.isEmpty(currentDate)
					|| !currentDate.equals(today)) {
				currentDate = today;
				tagName = tag;

				File tagDir = new File(logDir + "/" + tagName);
				if (!tagDir.exists()) {
					tagDir.mkdirs();
				}
				File logFile = new File(tagDir, currentDate + ".log");
				if (!logFile.exists()) {
					logFile.createNewFile();
				}
				FileOutputStream fos = new FileOutputStream(
						logFile.getAbsolutePath(), true);
				FileOutputStream old = logFileHash.put(tag, fos);
				if (old != null) {
					old.flush();
					old.close();
				}
			}

			String time = mDateFormat.format(new Date(System.currentTimeMillis()));
			FileOutputStream outStream = logFileHash.get(tag);
			if (outStream != null) {
				outStream.write((time + " " + info + "\n").getBytes());
				outStream.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeLogFile() {
		Collection<FileOutputStream> list = logFileHash.values();
		for (FileOutputStream os : list) {
			try {
				os.flush();
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
