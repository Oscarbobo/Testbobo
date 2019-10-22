package com.ubox.card.device.hkkj;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;


import com.ubox.card.BuildConfig;
import com.ubox.card.bean.external.Shipment;
import com.ubox.card.config.CardJson;
import com.ubox.card.util.OKHttp;
import com.ubox.card.util.SharedPreferencesHelper;
import com.ubox.card.util.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * Created by qinrui on 2019/4/29.
 */
public class StatusReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        if ("com.ubox.card.vendout".equals(action)) {
            if (null!=extras) {
                String cardMsg = extras.getString("cardMsg");
                Logger.error("下位机cardMsg:"+cardMsg);
                try {
                    JSONObject jsonObject = new JSONObject(cardMsg);
                    final String state = jsonObject.optString("state");
                    if (!TextUtils.isEmpty(state)) {
                        Shipment.getInstance().setStatus(jsonObject.optString("state"));
                        try {
                            final OKHttp okHttp = new OKHttp();
                            Logger.error("持久化----->"+Shipment.getInstance().toString());
                            SharedPreferencesHelper.putObject(context,"shipment",Shipment.getInstance());
                            okHttp.post(CardJson.UpShipping, orderShipment(Shipment.getInstance()), new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    Logger.info("error-----》》》 :"+e.getMessage());
                                    Shipment shipment = (Shipment) SharedPreferencesHelper.getObject(context, "shipment");
                                    okHttp.post(CardJson.UpShipping, orderShipment(shipment));
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    Logger.info("response :"+response.toString());

                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logger.info("error:"+jsonObject.toString());
                        }
                    }else{

                        Shipment.getInstance().setStatus("");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    //上传出货日志
    private  String orderShipment(Shipment shipment)  {
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("orderId",shipment.getOrderId());
            jsonObject.put("productId",shipment.getProductId());
            jsonObject.put("productName",shipment.getProductName());
            jsonObject.put("shipmentQty",shipment.getShipmentQty());
            if ("0".equals(shipment.getStatus())){
            jsonObject.put("statusDesc","出货成功");

            }else if ("2".equals(shipment.getStatus())) {
             jsonObject.put("statusDesc","出货失败");

            }

            jsonObject.put("status", getState());
            Logger.info("json param:"+jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();

    }

    private static String getState() {
        String status ="";
        if (!TextUtils.isEmpty(Shipment.getInstance().getStatus())) {
            // TODO: 2019/4/29  下位机传递过来的状态 0 代表成功，2代表失败
            if ("0".equals(Shipment.getInstance().getStatus())) {
                //TODO 同步服务端状态码 0 失败 1 代表成功
                status="1";
            }else if ("2".equals(Shipment.getInstance().getStatus())) {

                status ="0";
            }
        }
        return status;

    }
}
