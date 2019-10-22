package com.ubox.card.business;

import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.ubox.card.bean.external.Card;
import com.ubox.card.bean.external.CostRep;
import com.ubox.card.bean.external.CostReq;
import com.ubox.card.bean.external.ExtResponse;

public class Converter {

	/**
	 * 生成"cardRep"的应答JSON
	 * 
	 * @param cards 卡信息
	 * @param ext 应答对象
	 * @return "cardRep"的应答JSON
	 */
    public static String genCardRepJSON(Card[] cards, ExtResponse ext) {
        ext.getData().put("cards", cards);
        return JSON.toJSONString(ext);
    }

    /**
     * JSON字符串转换成应答对象
     * 
     * @param json JSON字符串
     * @return 应答对象
     */
    public static ExtResponse cJSON2ExtRep(String json) {
        return JSON.parseObject(json, ExtResponse.class);
    }

    /**
     * 从"costReq"的JSON中提取其data数据
     * 
     * @param json "costReq"的JSON字符串
     * @return 表示"costReq"的data数据对象
     */
    public static CostReq cJSON2CostReq(String json) {
        ExtResponse ext = JSON.parseObject(json, ExtResponse.class);
        return JSON.parseObject(JSON.toJSONString(ext.getData()), CostReq.class);
    }

    /**
     * "costReq"的data数据对象转换成"costRep"的data数据对象
     * 
     * @param req 表示"costReq"的data数据对象
     * @return 表示"costRep"的data数据对象
     */
    public static CostRep costReq2costRep(CostReq req) {
        CostRep rep = new CostRep();
        String seq = DeviceWorkProxy.ORDER_NO;
        rep.setOrderNo(seq);
        rep.setIsChoose(req.getIsChoose());
        rep.setOutOrderNo(req.getOutOrderNo());
        rep.setVendoutType(req.getVendoutType());
        rep.setProduct(req.getProduct());
        if(req.getCard() != null){
            rep.setCards(new Card[] { req.getCard() });
        }else{
            rep.setCards(new Card[] { new Card() });
        }

        return rep;
    }

    /**
	 * 生成"costRep"的应答JSON
	 * 
     * @param rep 表示"costRep"的data数据对象
     * @param ext 应答对象
     * @return "costRep"的应答JSON
     */
    @SuppressWarnings("unchecked")
    public static ExtResponse genCostRepJSON(CostRep rep, ExtResponse ext) {
    	//uvp 要求修改orderNo为空串
    	rep.setOrderNo("");
        ext.setData((Map<String, Object>)JSON.parse(JSON.toJSONString(rep)));
        return ext;
    }
}
