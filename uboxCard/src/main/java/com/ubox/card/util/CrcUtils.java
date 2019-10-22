/*
 * Copyright (c) 2011 友宝中国. 
 * All Rights Reserved. 保留所有权利.
 */
package com.ubox.card.util;

/**
 * @author daizheng
 * @date 2011-11-19 下午01:39:32 
 * @Description
 */
public class CrcUtils {

	public static int calCrc(char[] data, int length) {
		int crc = 0xFFFF;
		int num = 0;
		while (length-- != 0) {
			crc = crc ^ ((int)data[num++] << 8);
			for (int i = 0; i < 8; i++) {
				if ((crc & 0x8000) == 0x8000) {
					crc = (crc << 1) ^ 0x1021;
				} else {
					crc = crc << 1;
				}
			}
		}
		return (crc & 0xFFFF);
	}
	
	public static char[] byteA2charA(byte[] data,int len){
		if(data == null || len < 0 || data.length < len){
			return new char[0];
		}
		char[] ca = new char[len];
		for(int i=0;i<len;i++){
			ca[i] = (char)data[i];
		}
		return ca;
	}
	
	public static int calCrc(int[] data) {
		int crc = data[0];
		int i = 0;
		while(i < data.length - 1) {
			crc = crc ^ data[i + 1];
			i++;
		}
		return crc;
	}

}
