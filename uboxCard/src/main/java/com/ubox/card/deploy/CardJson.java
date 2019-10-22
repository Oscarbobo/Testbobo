package com.ubox.card.deploy;

public class CardJson {
	private Integer isCardDevice;
	private Integer appType;
	private String  cardName;
	
	public CardJson() {}

	public Integer getIsCardDevice() {
		return isCardDevice;
	}

	public void setIsCardDevice(Integer isCardDevice) {
		this.isCardDevice = isCardDevice;
	}

	public Integer getAppType() {
		return appType;
	}

	public void setAppType(Integer appType) {
		this.appType = appType;
	}

	public String getCardName() {
		return cardName;
	}

	public void setCardName(String cardName) {
		this.cardName = cardName;
	}

}
