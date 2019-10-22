package com.ubox.card.bean.external;


public class CostRep {
	private boolean isChoose = false;
	private String orderNo; //刷卡唯一流水号
	private Product product;
	private Card[] cards;
	private String vendoutType;
	private String outOrderNo;
	private String thirdOrderNo;
	
	public boolean getIsChoose() {
		return isChoose;
	}
    
	public void setIsChoose(boolean isChoose) {
		this.isChoose = isChoose;
	}
    
	public String getOrderNo() {
		return orderNo;
	}
    
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
    
	public Product getProduct() {
		return product;
	}
    
	public void setProduct(Product product) {
		this.product = product;
	}
    
	public Card[] getCards() {
		return cards;
	}
    
	public void setCards(Card[] cards) {
		this.cards = cards;
	}
    
	public String getVendoutType() {
		return vendoutType;
	}
    
	public void setVendoutType(String vendoutType) {
		this.vendoutType = vendoutType;
	}
	
	public String getOutOrderNo() {
		return outOrderNo;
	}
    
	public void setOutOrderNo(String outOrderNo) {
		this.outOrderNo = outOrderNo;
	}
    
	public String getThirdOrderNo() {
		return thirdOrderNo;
	}
    
	public void setThirdOrderNo(String thirdOrderNo) {
		this.thirdOrderNo = thirdOrderNo;
	}
}
