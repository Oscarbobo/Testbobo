package com.ubox.card.device.wht;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ubox.card.util.Utils;


public class WhtUtils {
	
	//crc算法
	public static byte getCrc8(byte[] data_N, int length) {
		byte cFcs;
		int i, j;
		cFcs = (byte) 0xc7;

		for (i = 0; i < length; i++) {
			cFcs = (byte) (cFcs ^ data_N[i]);
			for (j = 0; j < 8; j++) {
					if ((byte) (cFcs & 0x80) != (byte) 0x00)
					cFcs = (byte) ((cFcs << 1) ^ 0x1D);
				else
					cFcs = (byte) (cFcs << 1);
			}
		}
		return (cFcs);
	}
	
	/*
	 * 计算时间
	 */
	public  static byte[] isCurrDay() {
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String nowDate = df.format(new Date());

		byte bYear = Utils.makeByte1(Integer.parseInt(nowDate.substring(0, 2),
				16));
		byte bYear1 = Utils.makeByte1(Integer.parseInt(nowDate.substring(2, 4),
				16));
		byte bMonth = Utils.makeByte1(Integer.parseInt(nowDate.substring(4, 6),
				16));
		byte bDay = Utils.makeByte1(Integer.parseInt(nowDate.substring(6, 8),
				16));
		byte bHour = Utils.makeByte1(Integer.parseInt(nowDate.substring(8, 10),
				16));
		byte bMinute = Utils.makeByte1(Integer.parseInt(nowDate.substring(10,
				12), 16));
		byte bSecond = Utils.makeByte1(Integer.parseInt(nowDate.substring(12,
				14), 16));

		byte[] bResult = { bYear, bYear1, bMonth, bDay, bHour, bMinute, bSecond };

		return bResult;
	}
}
