package com.ubox.card.device.ynylsf.ynfutils;

public class YNfuUtil {

    public static void backCover(char[] src, char[] dest) {
        if(src == null || dest == null) throw new IllegalArgumentException("Argument is NULL");

        for(int si = src.length, di = dest.length; si > 0 && di > 0; si --, di --) {
            dest[di - 1] = src[si - 1];
        }
    }

    /**
     * 字节数组转换成H-ASCII字符串
     *
     * @param ba 字节数组
     * @return H-ASCII字符串
     */
    public static String BA2HS(byte[] ba) {
        if(ba == null) throw new IllegalArgumentException("hs is NULL");

        StringBuilder sb = new StringBuilder(ba.length * 2);
        String tmp;
        for(byte b : ba) {
            tmp = Integer.toHexString(b & 0xFF);
            if(tmp.length() < 2) sb.append('0');
            sb.append(tmp);
        }

        return sb.toString();
    }

    /**
     * Byte转换成字符串
     * @param b
     * @return
     */
    public static String byte2HASCII(byte b) {
        String tmp = Integer.toHexString(b & 0xFF);

        if(tmp.length() < 2) {
            return "0" + tmp;
        } else {
            return tmp;
        }
    }

}
