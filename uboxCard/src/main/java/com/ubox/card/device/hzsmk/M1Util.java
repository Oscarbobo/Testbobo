package com.ubox.card.device.hzsmk;

public class M1Util {
	public static String tradTime;//交易时间
	public static String tradMoney;//交易金额
	public static String TAC;//TAC
	public static String PSAMId;//PSAM脱机交易序号
	
	public static String IsTrad;//消费验证有无01 00
	public static String Ctac; //消费验证 tac
	public static String getIsTrad() {
		return IsTrad;
	}
	public static void setIsTrad(String isTrad) {
		IsTrad = isTrad;
	}
	public static String getCtac() {
		return Ctac;
	}
	public static void setCtac(String ctac) {
		Ctac = ctac;
	}
	public static String getTradTime() {
		return tradTime;
	}
	public static void setTradTime(String tradTime) {
		M1Util.tradTime = tradTime;
	}
	public static String getTradMoney() {
		return tradMoney;
	}
	public static void setTradMoney(String tradMoney) {
		M1Util.tradMoney = tradMoney;
	}
	public static String getTAC() {
		return TAC;
	}
	public static void setTAC(String tAC) {
		TAC = tAC;
	}
	public static String getPSAMId() {
		return PSAMId;
	}
	public static void setPSAMId(String pSAMId) {
		PSAMId = pSAMId;
	}
}
