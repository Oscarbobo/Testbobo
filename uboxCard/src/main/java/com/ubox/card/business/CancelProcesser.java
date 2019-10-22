package com.ubox.card.business;

import com.alibaba.fastjson.JSON;
import com.ubox.card.bean.BeanConst;
import com.ubox.card.bean.external.ExtResponse;

public class CancelProcesser {

    /**
     * 撤销交易处理 
     * 
     * @param msgJson 交易请求信息
     * @return 交易失败响应
     */
    public static ExtResponse cancelProcess(String msgJson) {
        ExtResponse response = JSON.parseObject(msgJson, ExtResponse.class);
        response.setResultCode(BeanConst.ERROR_CODE_CANCEL);
        response.setResultMsg(BeanConst.ERROR_MSG_CANCEL);
        
//        String cancelRet = JSON.toJSONString(response);
            
        return response;
    }
}
