/**
 * 
 */
package com.ubox.card.util;

/**
 * @author miral.gu
 * @date 2012-11-29 下午6:10:59
 * 
 */
public class RSCalcUtils {

	/**
	 * 异或校验
	 * 
	 * @param data
	 * @return
	 */
	public static byte calcParityBit(byte[] data) {
		if (null == data) {
			throw new NullPointerException();
		}

		byte pb = data[0];
		for (int i = 1, len = data.length; i < len; i++) {
			pb ^= data[i] & 0xFF;
		}

		return pb;
	}

	/**
	 * BCD码转换成int,小端方式
	 * 
	 * @param bcd
	 * @return
	 */
	public static String BCD2StringLE(byte... bcd) {
		String reval = "";

		if (null == bcd)
			return reval;

		for (byte b : bcd) {
			String tmp = Integer.toHexString(b & 0xFF);
			reval = (tmp.length() > 1 ? tmp : "0" + tmp) + reval;
		}

		return reval;
	}

	/**
	 * BCD码转换成int,大端方式
	 * 
	 * @param bcd
	 * @return
	 */
	public static String BCD2StringBE(byte... bcd) {
		String reval = "";

		if (null == bcd)
			return reval;

		for (byte b : bcd) {
			String tmp = Integer.toHexString(b & 0xFF);
			reval += tmp.length() > 1 ? tmp : "0" + tmp;
		}

		return reval;
	}

	/**
	 * int转换成BCD码,大端方式
	 * 
	 * @param integer
	 * @return
	 */
	public static byte[] int2BCDBE(int integer) {
		if (integer < 0)
			return null;

		String tmp = String.valueOf(integer);
		if (tmp.length() % 2 != 0)
			tmp = "0" + tmp;

		char[] tmpcs = tmp.toCharArray();
		byte[] reval = new byte[tmpcs.length / 2];

		// big endian
		for (int len = reval.length, i = 0; i < len; i++) {
			char t0 = tmpcs[i * 2];
			char t1 = tmpcs[i * 2 + 1];

			byte b0 = (byte) (t0 - '0');
			byte b1 = (byte) (t1 - '0');

			reval[i] = (byte) ((b0 << 4) | b1);
		}

		return reval;
	}

	/**
	 * int转换成BCD码,小端方式
	 * 
	 * @param integer
	 * @return
	 */
	public static byte[] int2BCDLE(int integer) {
		if (integer < 0)
			return null;

		String tmp = String.valueOf(integer);
		if (tmp.length() % 2 != 0)
			tmp = "0" + tmp;

		char[] tmpcs = tmp.toCharArray();
		byte[] reval = new byte[tmpcs.length / 2];

		// little endian
		for (int i = 0, len = reval.length; i < len; i++) {
			char t0 = tmpcs[len * 2 - 1 - (i * 2)];
			char t1 = tmpcs[len * 2 - 2 - (i * 2)];

			byte b0 = (byte) (t0 - '0');
			byte b1 = (byte) (t1 - '0');

			reval[i] = (byte) (b1 << 4 | b0);
		}

		return reval;
	}

	/**
	 * byte数组转换成long,小端模式
	 * 
	 * @param barr
	 * @return
	 */
	public static long barr2longLE(byte[] barr) {
		long reval = 0;

		int len = barr.length;
		if (null == barr || barr.length > 8)
			return reval;

		for (int i = 0; i < len; i++) {
			reval += (barr[i] & 0xFF) << (8 * i);
		}

		return reval;
	}

}
