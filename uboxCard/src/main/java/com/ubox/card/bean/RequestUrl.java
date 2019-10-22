package com.ubox.card.bean;

/**
 * Created by qinrui on 2019/4/19.
 *请求Url地址
 */
public interface RequestUrl {

    /**
     * 刷卡下单
     */
    String swipCardOrder ="http://testmengniu.aicebox.com/productOrder";
    /**
     * 上传出货日志信息
     */
    String UpShipping ="http://testmengniu.aicebox.com/orderShipment/uploadShipmentInfo";

}
