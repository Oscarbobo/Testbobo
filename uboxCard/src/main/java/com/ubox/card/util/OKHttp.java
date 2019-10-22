package com.ubox.card.util;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by qinrui on 2019/4/25.
 */
public class OKHttp {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60,TimeUnit.SECONDS)
            .readTimeout(90,TimeUnit.SECONDS).build();
   public String post(String url, String json)  {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
       Response response = null;
       try {
           response = client.newCall(request).execute();
           if  (response.isSuccessful()) {
               return response.body().string();
           }
       } catch (IOException e) {
           e.printStackTrace();
       }
       return "";

    }




    public void post(String url, String json, Callback callback)
    {
        RequestBody requestBody =RequestBody.create(JSON,json);
        Request request =new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);


    }

    public String json(){
        Map<String,Object> linkedMap=new LinkedHashMap<String, Object>();

        linkedMap.put("cardNo","b152585c");
        linkedMap.put("cardInfo","00000000000000000000000000000b12");
        linkedMap.put("totalDue","2.5");
        linkedMap.put("vmid","99900602");
        linkedMap.put("deviceName","test");
        linkedMap.put("productId","3");
        linkedMap.put("productName","可口可乐芬达橙味汽水330ml");
        linkedMap.put("quantity","1");
        linkedMap.put("orderBillingType","TRADE");
        linkedMap.put("sign","N2Tn2mJDwYcL9unFlbcRUR6s28VhHxxppxpJjP9RDLVuDkM1yUfunYBXO+gc5uY4zHnqc\\/\\/o8lqBX0d17\\/kXXV5ZBY6s1O3wquULijveqjMV5IqZ3YWLJ7NqSv7TOHywuChY2fTggEHkX3DaOvN76Hxh732gZF2YefaxTNpM3+pExSg5f0fhsdkx7DOxShuXFwD4jc2SjAf1DO0jj3\\/x5ZgX5+BdtSl7M05EDQX+odh2qKwrAbMfMneGHKslv5\\/JUQRDksA1W2XCE5ItiHkQz1ZHrqZ0LHUg6LqJluOpFuKKBH\\/YeGd5z46XEGY7nQ0MoQRdWGssOEvfjYzPGcld+g==");
        String jsonStr=new Gson().toJson(linkedMap);

        return jsonStr;
    }
}
