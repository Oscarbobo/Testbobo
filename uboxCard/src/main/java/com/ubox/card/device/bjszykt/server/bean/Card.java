/**
 * 
 */
package com.ubox.card.device.bjszykt.server.bean;

/**
 * 
 * @author gaolei
 * @version 2015年9月18日
 * 
 */
public class Card {
	private String cardNo;//卡号
	private Integer cardMoney;//充值后卡余额,单位分
	private String cardType;//卡类型
	private Integer cardBalance;//充值后卡余额,单位分
	private String validDate;//卡有效期
	/**
	 * @return the cardNo
	 */
	public String getCardNo() {
		return cardNo;
	}
	/**
	 * @param cardNo the cardNo to set
	 */
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}
	/**
	 * @return the cardBalance
	 */
	public Integer getCardBalance() {
		return cardBalance;
	}
	/**
	 * @param cardBalance the cardBalance to set
	 */
	public void setCardBalance(Integer cardBalance) {
		this.cardBalance = cardBalance;
	}
	/**
	 * @return the validDate
	 */
	public String getValidDate() {
		return validDate;
	}
	/**
	 * @param validDate the validDate to set
	 */
	public void setValidDate(String validDate) {
		this.validDate = validDate;
	}

	public Integer getCardMoney() {
		return cardMoney;
	}

	public void setCardMoney(Integer cardMoney) {
		this.cardMoney = cardMoney;
	}
	public String getCardType() {
		return cardType;
	}
	public void setCardType(String cardType) {
		this.cardType = cardType;
	}
}
