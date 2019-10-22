package com.ubox.card.device.bjszykt.server;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ubox.card.device.bjszykt.volley.AuthFailureError;
import com.ubox.card.device.bjszykt.volley.Request;
import com.ubox.card.device.bjszykt.volley.RequestQueue;
import com.ubox.card.device.bjszykt.volley.Response;
import com.ubox.card.device.bjszykt.volley.VolleyError;
import com.ubox.card.device.bjszykt.volley.toolbox.StringRequest;

/**
 * Description:
 * Created by WangPeng
 * Date: 2015-09-11
 */
public class Tools {


    public static String getSignValue(Map<String, String> map) {
        //整个请求参数对 key/value排序
        List<String> keys = new ArrayList<String>();
        Iterator<String> keyIter = map.keySet().iterator();
        while (keyIter.hasNext()) {
            keys.add(keyIter.next());
        }

        // 升序操作(对所有post参数根据参数名做ascii排序)
        Collections.sort(keys, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });

        //把上排序好的参数key、value拼接成字符串：key1=value1key2=value2key3=value3
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < keys.size(); i++) {
            try {
                String key = "";
                String value = "";
                if (keys.get(i) != null) {
                    key = URLEncoder.encode(keys.get(i), "utf-8");
                }
                if (map.get(key) != null) {
                    value = URLEncoder.encode(map.get(key), "utf-8");
                }
                stringBuffer.append(key + "=" + value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String token = "123";
        String s = stringBuffer.toString() + "_" + token;
        String sign = sha1(s);
        map.put("sign", sign);

        return sign;
    }

    /**
     * sha1算法
     * @param str
     * @return
     */
    public static String sha1(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }

        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

        try {
            MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
            mdTemp.update(str.getBytes());

            byte[] md = mdTemp.digest();
            int j = md.length;
            char buf[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
                buf[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(buf);
        } catch (Exception e) {
            return null;
        }
    }

    public static void writeCard(String cardId, boolean isSuccess) {

    }

    public static void readCard(String cardId, boolean isSuccess) {

    }

    /**
     * 发送消息到机器内得server
     * @param vmId          售货机编号
     * @param clientSeq     流水号。 保证(vmId+clientSeq)唯一
     * @param msgType       消息类型
     * @param createTime    创建时间 (YYYY-MM-DD HH:mm:ss)
     * @param data          数据
     * @param resultCode    200 成功,非 200 失败。 成功只是代表通讯正常。 判断业务处理是否成功,需 要处理 data 数据。
     * @param resultMsg     文本消息
     */
    public static void sendDataToServer(String vmId, String clientSeq,
                         String msgType, String createTime, String data,
                         int resultCode, String resultMsg) {


    }
    @SuppressWarnings("unused")
	private void sendData(RequestQueue requestQueue) {
//        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                "http://ave.bolyartech.com/params.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        })
        {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return super.getBody();
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("param2", "14");
                params.put("param1", "02");

                Tools.getSignValue(params);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }
}
