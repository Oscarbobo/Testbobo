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
public class KCQTradeResponse {
	private int code;//操作结果码,非200失败
	private String msg;//操作结果信息
	private Card cards;//卡bean
	private Order orders;//订单bean
	
	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}
	/**
	 * @param code the code to set
	 */
	public void setCode(int code) {
		this.code = code;
	}
	/**
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}
	/**
	 * @param msg the msg to set
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}
	/**
	 * @return the cards
	 */
	public Card getCards() {
		return cards;
	}
	/**
	 * @param cards the cards to set
	 */
	public void setCards(Card cards) {
		this.cards = cards;
	}
	/**
	 * @return the orders
	 */
	public Order getOrders() {
		return orders;
	}
	/**
	 * @param orders the orders to set
	 */
	public void setOrders(Order orders) {
		this.orders = orders;
	}
	
	
}
