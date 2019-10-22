package com.ubox.card.device.bjszykt.pubwork;

public class Result {
    public static final int CODESUCCESS = 0;
    public static final int BYTESCODE   = 1;
    public static final int HDCODE      = 2;
    public static final int NETCODE     = 3;
    public static final int VSCODE      = 4;
    public static final int HARDWARE    = 5;
    
    public final int    codeType;// 0-没有错误, 1-byte流错误类型, 2-hdCode错误类型, 3-netCode类型, 4-VCardServer返回空字符串

    public final byte[] fdBytes;// BYTEs反馈信息

    public final int    bytesCode;
    public final String hdCode;
    public final int    netCode;


    public Result(int codeType, byte[] fdBytes, int bytesCode, String hdCode, int netCode) {
        this.codeType       = codeType;
        this.fdBytes        = fdBytes;
        this.bytesCode      = bytesCode;
        this.hdCode         = hdCode;
        this.netCode        = netCode;
    }

}
