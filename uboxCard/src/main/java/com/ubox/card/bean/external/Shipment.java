package com.ubox.card.bean.external;

import java.io.Serializable;

/**
 * Created by qinrui on 2019/3/20.
 */
public class Shipment implements Serializable {
    private static String orderId;            //订单ID
    private static String status;             //出货状态：0：失败；1：成功
    private static String statusDesc;          //出货状态描述
    private static String productId;          //	VMS商品ID
    private static String productName;         //VMS商品名称
    private static String shipmentQty;        //出货数量


    public  Shipment() {

    }

    private static  Shipment mInstance;

    public static  Shipment getInstance(){
        if (mInstance ==null) {
            synchronized (Shipment.class) {
                if (mInstance==null) {
                    mInstance=new Shipment();
                }

            }
        }
        return mInstance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getShipmentQty() {
        return shipmentQty;
    }

    public void setShipmentQty(String shipmentQty) {
        this.shipmentQty = shipmentQty;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    @Override
    public String toString() {
        return "Shipment{}"+"productId:"+productId+" \n"+" orderId:"+orderId+" \n"+" productName："+productName+"\n"
                +" status:"+ status;
    }
}
