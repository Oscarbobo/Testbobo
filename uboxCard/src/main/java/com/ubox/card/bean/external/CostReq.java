package com.ubox.card.bean.external;


public class CostReq {
	private String vendoutType;
	private Boolean isChoose = false;
	private Product product;
	private Card card;
	private String outOrderNo;

	public String getVendoutType() {
		return vendoutType;
	}

	public void setVendoutType(String vendoutType) {
		this.vendoutType = vendoutType;
	}

	public Boolean getIsChoose() {
		return isChoose;
	}

	public void setIsChoose(Boolean isChoose) {
		this.isChoose = isChoose;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	public String getOutOrderNo() {
		return outOrderNo;
	}

	public void setOutOrderNo(String outOrderNo) {
		this.outOrderNo = outOrderNo;
	}
}
