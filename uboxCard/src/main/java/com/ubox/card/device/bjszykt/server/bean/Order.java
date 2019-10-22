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
public class Order {
	private String orderNo;//订单号
	private Integer orderAmt;//订单⾦金额,单位分
	private String orderDate;//订单⽇日期
	private String orderTime;//订单时间
	/**
	 * @return the orderNo
	 */
	public String getOrderNo() {
		return orderNo;
	}
	/**
	 * @param orderNo the orderNo to set
	 */
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	/**
	 * @return the orderAmt
	 */
	public Integer getOrderAmt() {
		return orderAmt;
	}
	/**
	 * @param orderAmt the orderAmt to set
	 */
	public void setOrderAmt(Integer orderAmt) {
		this.orderAmt = orderAmt;
	}
	/**
	 * @return the orderDate
	 */
	public String getOrderDate() {
		return orderDate;
	}
	/**
	 * @param orderDate the orderDate to set
	 */
	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}
	/**
	 * @return the orderTime
	 */
	public String getOrderTime() {
		return orderTime;
	}
	/**
	 * @param orderTime the orderTime to set
	 */
	public void setOrderTime(String orderTime) {
		this.orderTime = orderTime;
	}
	
	
}
