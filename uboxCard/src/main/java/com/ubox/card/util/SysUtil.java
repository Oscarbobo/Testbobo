package com.ubox.card.util;


public class SysUtil {

	public static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) { }
	}
	
}
