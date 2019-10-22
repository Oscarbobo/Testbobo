package com.ubox.card.device.quickpass.rule;

/**
 * 活动开始时间：5月22日－7月22日
 * 活动-->活动规则：
 * 活动1：持银联金融IC卡客户及绑定银行卡的NFC手机客户，通过闪付支付时，享受每瓶0.01元价格购买售价3元及以下商品。每日每台限20瓶，每张IC卡每日限参与活动一次，先到先得。
 * 活动2：持银联金融IC卡客户及绑定银行卡的NFC手机客户，通过闪付方式支付时，享受以9折价格购买参与机器中所有商品。每张IC卡每日限享受一次优惠。
 * ps：同一IC卡每日可同时参与上述专享优惠活动1和活动2各一次。
 * 
 * @author weipeipei
 * 
 */
public class QUICKPASSRuleResponse {
	int ruleMoney;//营销规则后实际金额
	int money;//营销规则前金额
	int count;//购买次数
	String cardNo;//卡号
	boolean discount;//是否符合活动
	boolean record;//是否需要记录文件
	int ruleType;//活动类型，1:1分钱, 2:9折
	
	/**
	 * @return the ruleType
	 */
	public int getRuleType() {
		return ruleType;
	}

	/**
	 * @param ruleType the ruleType to set
	 */
	public void setRuleType(int ruleType) {
		this.ruleType = ruleType;
	}

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
