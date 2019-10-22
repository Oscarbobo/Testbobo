/*
 * Copyright (c) 2012 友宝在线. 
 * All Rights Reserved. 保留所有权利.
 */
package com.ubox.card.bean.db;


/**
 * 
 * @author gaolei
 * @version 2012-3-20
 */

public class VCardInfoObj {
	private String innerCode;
	private String appType;
	private String softVersion;
	private String clientTime;
	private String os;
	
	/**
	 * @return the os
	 */
	public String getOs() {
		return os;
	}
	/**
	 * @param os the os to set
	 */
	public void setOs(String os) {
		this.os = os;
	}
	/**
	 * @return the innerCode
	 */
	public String getInnerCode() {
		return innerCode;
	}
	/**
	 * @param innerCode the innerCode to set
	 */
	public void setInnerCode(String innerCode) {
		this.innerCode = innerCode;
	}
	/**
	 * @return the appType
	 */
	public String getAppType() {
		return appType;
	}
	/**
	 * @param appType the appType to set
	 */
	public void setAppType(String appType) {
		this.appType = appType;
	}
	/**
	 * @return the softVersion
	 */
	public String getSoftVersion() {
		return softVersion;
	}
	/**
	 * @param softVersion the softVersion to set
	 */
	public void setSoftVersion(String softVersion) {
		this.softVersion = softVersion;
	}
	/**
	 * @return the clientTime
	 */
	public String getClientTime() {
		return clientTime;
	}
	/**
	 * @param clientTime the clientTime to set
	 */
	public void setClientTime(String clientTime) {
		this.clientTime = clientTime;
	}
}
