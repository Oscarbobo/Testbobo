package com.ubox.card.device.hkkj;

/**
 * Created by qinrui on 2019/4/24.
 */
public class EncodeBase64 {

    private String cardNo;              //卡号
    private double totalDue;            //订单金额
    private String vmid;                //VMS设备号
    private String deviceName;          //VMS设备名称
    private int productId;          //VMS商品ID
    private String productName;         //VMS商品名称
    private int quantity;           //下单数量
    private String orderBillingType;    //固定为"TRADE"

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public double getTotalDue() {
        return totalDue;
    }

    public void setTotalDue(double totalDue) {
        this.totalDue = totalDue;
    }

    public String getVmid() {
        return vmid;
    }

    public void setVmid(String vmid) {
        this.vmid = vmid;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getOrderBillingType() {
        return orderBillingType;
    }

    public void setOrderBillingType(String orderBillingType) {
        this.orderBillingType = orderBillingType;
    }
}
