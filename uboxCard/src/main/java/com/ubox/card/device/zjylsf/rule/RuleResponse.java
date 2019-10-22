/**
 * 
 */
package com.ubox.card.device.zjylsf.rule;


/**
 * 营销规则响应对象
 * @author gaolei
 * @version 2015年3月20日
 * 
 */
public class RuleResponse {
	int ruleMoney;//营销规则后实际金额
	int money;//营销规则前金额
	int count;//购买次数
	String cardNo;//卡号
	boolean discount;//是否符合活动
	boolean record;//是否需要记录文件
	
	/**
	 * @return the ruleMoney
	 */
	public int getRuleMoney() {
		return ruleMoney;
	}

	/**
	 * @param ruleMoney the ruleMoney to set
	 */
	public void setRuleMoney(int ruleMoney) {
		this.ruleMoney = ruleMoney;
	}

	/**
	 * @return the money
	 */
	public int getMoney() {
		return money;
	}

	/**
	 * @param money the money to set
	 */
	public void setMoney(int money) {
		this.money = money;
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

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
	 * @return the discount
	 */
	public boolean isDiscount() {
		return discount;
	}

	/**
	 * @param discount the discount to set
	 */
	public void setDiscount(boolean discount) {
		this.discount = discount;
	}

	/**
	 * @return the record
	 */
	public boolean isRecord() {
		return record;
	}

	/**
	 * @param record the record to set
	 */
	public void setRecord(boolean record) {
		this.record = record;
	}

}
