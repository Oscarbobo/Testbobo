package com.ubox.card.device.hkkj;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.szt.serialport.entity.READERINFO;
import com.ubox.card.CardApplication;
import com.ubox.card.bean.RequestUrl;
import com.ubox.card.bean.external.Card;
import com.ubox.card.bean.external.CostRep;
import com.ubox.card.bean.external.ExtResponse;
import com.ubox.card.bean.external.Order;
import com.ubox.card.bean.external.Shipment;
import com.ubox.card.business.CancelProcesser;
import com.ubox.card.business.Converter;
import com.ubox.card.config.CardConst;
import com.ubox.card.config.CardJson;
import com.ubox.card.device.Device;
import com.ubox.card.device.bjszykt.network.NetUtils;
import com.ubox.card.util.Base64Utils;
import com.ubox.card.util.OKHttp;
import com.ubox.card.util.Utils;
import com.ubox.card.util.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.PrivateKey;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.ubox.card.device.hkkj.SHA256withRsaUtils.sign;



public class HKKJ extends Device {

    private static HKKJER worker=new HKKJER();
    private static Order order=new Order();
    private static OKHttp okHttp=new OKHttp();
    private static  Shipment instance;

    @Override
    public void init() {

    }

    @Override
    public ExtResponse cardInfo(String json) {
        return null;
    }


    @Override
    public ExtResponse cost(String json) {
        Logger.info(">>>>> cost:"+json+ "ThreadName---> "+Thread.currentThread().getName());
        ExtResponse ext = Converter.cJSON2ExtRep(json);
        CostRep rep = Converter.costReq2costRep(Converter.cJSON2CostReq(json));
        instance = Shipment.getInstance();
        String resultJson="";
        try {
            JSONObject jsonObj = new JSONObject(json);

            order.setVmid(CardJson.vmId);
            JSONObject date = jsonObj.getJSONObject("data");
            JSONObject product=date.getJSONObject("product");
            order.setProductId(product.getInt("proId"));
            order.setProductName(product.getString("proName"));
            order.setTotalDue(product.getDouble("salePrice"));
            order.setOrderBillingType("TRADE");
            //order.setTerminalId(CardJson.terminalId);

            instance.setShipmentQty("1");
            instance.setProductId(product.getString("proId"));
            instance.setProductName(product.getString("proName"));
//            shipment.setProductId(product.getString("proId"));
//            shipment.setProductName(product.getString("proName"));
//            shipment.setShipmentQty("1");

            Logger.info("商品名称:"+order.getProductName());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //1.打开串口
        openSerial(ext,rep);

        //2.循环读卡
        findCard(ext,rep,json);

        return Converter.genCostRepJSON(rep, ext);
    }


    /**
     * 打开串口
     */


    public static ExtResponse openSerial(ExtResponse ext,CostRep rep) {
        long startTime = System.currentTimeMillis();
        try {
            if(HKKJER.RESULT.SUCCESS != worker.open()){
                ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
                ext.setResultMsg("打开串口失败");
                return Converter.genCostRepJSON(rep, ext);
            }
        } catch (Exception e) {
            Logger.error("cost is exception. "+e.getMessage(), e);
            ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
            ext.setResultMsg("打开串口失败!");
            Logger.info("<<< cost time:"+(((double) (System.currentTimeMillis() - startTime)) / 1000.0d));
            return Converter.genCostRepJSON(rep, ext);
        }
        return  null;
    }

    /**
     * 2.寻卡
     */

    public static ExtResponse findCard(ExtResponse ext, final CostRep rep, String json) {
        //2.循环读卡
        try {
            int timeout = 20;//寻卡超时时间（秒）
            long curTime = System.currentTimeMillis();
            while (true){

                //处理中断功能
                if (com.ubox.card.util.Utils.isCancel(ext.getSerialNo() + "") == true) {
                    Logger.info("<<<<<<<<<<<<<<<   Cost is cancle. ");
                    return CancelProcesser.cancelProcess(json);
                }

                //处理超时
                if (System.currentTimeMillis() - curTime >= (1000 * timeout)) {
                    ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
                    ext.setResultMsg("读卡超时");
                    Logger.info("<<<<<<<<<<<<<<<   Read card time out. ");
                    worker.close();
                    return Converter.genCostRepJSON(rep, ext);
                }

                //休息会
                Thread.sleep(100);

                //调用底层寻卡,查看是否有卡
                HKKJER.RESULT status=worker.findCard(ext.getSerialNo()+"");
                if (status== HKKJER.RESULT.SUCCESS){
                    Logger.info("find card successful.");

                    //防冲突
                    byte[] receiveBytes=worker.conflictPrevention();
                    if (receiveBytes!=null){
                        Logger.info("防冲突");
                        order.setCardNo(String.valueOf(worker.map.get("cardNo")));
                        //选卡
                        HKKJER.RESULT status1=worker.selectCard();
                        if (status1== HKKJER.RESULT.SUCCESS){
                            Logger.info("Card selection successful.");
                            //验证卡片密码
                            HKKJER.RESULT status2=worker.verifyPassword();
                            if (status2== HKKJER.RESULT.SUCCESS){
                                Logger.info("Password Validation Successful.");
                                //读取卡信息：1扇区0块
                                byte[] receiveBytes1=worker.readCard();
                                if (receiveBytes1!=null){
                                    Logger.info("read cardID successful.");
                                    //读取卡信息：1扇区1块
                                    byte[] receiveBytes2=worker.readCardInfo();
                                    if (receiveBytes2!=null){
                                        Logger.info("read cardTemporary successful.");
                                        String test= (String) worker.map.get("test");
                                        String cardTemporary = worker.map.get("cardTemporary").toString();
                                        String cardNO=worker.map.get("cardNO").toString()+test;
                                        Logger.error("拼接卡号："+cardNO);
                                        String num=worker.map.get("cardNO").toString()+cardTemporary;
                                        order.setCardNo(cardNO);
                                        Logger.error("拼接卡信息："+num);
                                        order.setCardInfo(num);
                                        break;
                                    }else {
                                        Logger.error("read cardTemporary error");
                                        ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
                                        ext.setResultMsg("读卡信息失败");
                                        continue;
                                    }
                                }else {
                                    Logger.error("read card error");
                                    ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
                                    ext.setResultMsg("读卡失败");
                                    continue;
                                }
                            }else {
                                Logger.error("Password error. code:"+status2);
                                ext.setResultMsg("密码错误");
                                ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
                                continue;
                            }


                        }else {
                            ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
                            ext.setResultMsg("选卡失败");
                            Logger.info("Card selection error");
                            continue;
                        }

                    }else {
                        ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
                        ext.setResultMsg("防冲突错误");
                        Logger.info("Conflict prevention error");
                        continue;
                    }

                }else {
                    if(status == HKKJER.RESULT.CANCEL){
                        return CancelProcesser.cancelProcess(json);
                    }
                    if (status!= HKKJER.RESULT.TIMEOUT){
                        continue;
                    }
                }
            }



            //格式化费用
            int payMoney = rep.getProduct().getSalePrice();
            DecimalFormat df = new DecimalFormat("0.00");
            worker.map.put("cardBalance",df.format((float)payMoney/100));
            order.setTotalDue(Double.parseDouble(df.format((float)payMoney/100)));

            //刷卡下单,发起网络请求

            String resp=okHttp.post(CardJson.swipCardOrder, signParam(order));

            Logger.error("response:"+ resp);
            String s = responOrder(resp, instance);

            // TODO: 2019/4/30 刷卡下单 将出货状态重置
            instance.setStatus("");
            instance.setOrderId(s);
            Log.d(HKKJ.class.getSimpleName()," Shipment： "+instance.toString());
            //接收响应
            Map<String, Object> respMap = response(resp);
            if (null!=respMap) {
                if(respMap.get("resultCode").equals("0")) { // 扣款成功
                    Card card = rep.getCards()[0];
                    card.setCardNo(String.valueOf(worker.map.get("cardNo")));
                    ext.setResultCode(CardConst.EXT_SUCCESS);
                    ext.setResultMsg("扣款成功");
                } else {
                    ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
                    ext.setResultMsg((String) respMap.get("resultMsg"));
                    /*instance.setStatusDesc("出货失败");
                    instance.setStatus("0");*/
                }
            }

        }catch (Exception e){
            Logger.error("cost is exception. "+e.getMessage(), e);
            ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
            ext.setResultMsg("扣款异常，请检查网络！");
        }
        return Converter.genCostRepJSON(rep, ext);
    }


    private static Map<String, Object> response(String resp)  {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            JSONObject jsonObj = new JSONObject(resp);
            map.put("resultCode",jsonObj.optString("resultCode"));
            map.put("resultMsg", jsonObj.optString("resultMsg"));
            if (map.get("resultCode").equals("0")) {
                map.put("ts", jsonObj.optString("ts"));
                map.put("orderId", jsonObj.optString("orderId"));
                map.put("orderNo", jsonObj.optString("orderNo"));
                map.put("uid", jsonObj.optString("uid"));
                map.put("cardId", jsonObj.optString("cardId"));
                map.put("cardNo", jsonObj.optString("cardNo"));
                map.put("totalDue", jsonObj.optString("totalDue"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.error("response " +e.getLocalizedMessage());

        }

        return map;


    }

    //加密参数
    private static String signParam(Order order) throws Exception {
        PrivateKey privateKey = RSA.getPrivateKey( RSA.PRIVATE_KEY );

        JSONObject jsonSign=new JSONObject();
        Gson gson=new Gson();

        Map<String,Object> linkedMap=new LinkedHashMap<String, Object>();

        linkedMap.put("cardNo",order.getCardNo());
        linkedMap.put("cardInfo",order.getCardInfo());
        linkedMap.put("totalDue",order.getTotalDue());
        linkedMap.put("vmid",order.getVmid());
        linkedMap.put("deviceName","");
        linkedMap.put("productId",order.getProductId());
        linkedMap.put("productName",order.getProductName());
        linkedMap.put("quantity",1);
        linkedMap.put("orderBillingType","TRADE");
        String jsonStr=new Gson().toJson(linkedMap);

        Log.e(HKKJ.class.getSimpleName(),"sign---"+jsonStr);

        JSONObject object=new JSONObject();

        byte[] sing_byte = sign(privateKey, jsonStr);
        String s = String.valueOf(Base64Utils.encode(sing_byte));
        Logger.info("加密后的java--"+s);
        try {
            object.put("cardInfo",order.getCardInfo());
            object.put("cardNo",order.getCardNo());
            object.put("sign",s);
            object.put("totalDue",order.getTotalDue()+"");
            object.put("vmid",order.getVmid());
            object.put("deviceName","");
            object.put("productId",order.getProductId()+"");
            object.put("productName", order.getProductName());
            object.put("quantity",1+"");
            object.put("orderBillingType","TRADE");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(HKKJ.class.getSimpleName(),"error--"+e.getMessage());
        }
        Logger.error("下单接口传参："+object.toString());
        return object.toString();
    }
    //获取接口返回的orderId
    private static String responOrder(String resp,Shipment shipment)  {
     String orderId="";
        try {
             JSONObject  jsonObj = new JSONObject(resp);
             orderId =jsonObj.optString("orderId");
//            shipment.setOrderId(jsonObj.optString("orderId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        shipment.setOrderId(orderId);
        return shipment.getOrderId();

    }

}
