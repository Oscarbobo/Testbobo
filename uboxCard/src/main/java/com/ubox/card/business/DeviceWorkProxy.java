package com.ubox.card.business;

import com.alibaba.fastjson.JSON;
import com.ubox.card.bean.db.BrushCupBoardLogObj.BrushCupBoardLog;
import com.ubox.card.bean.external.CostRep;
import com.ubox.card.bean.external.ExtResponse;
import com.ubox.card.config.CardConst;
import com.ubox.card.config.CardJson;
import com.ubox.card.core.WorkPool;
import com.ubox.card.core.context.CardContext;
import com.ubox.card.db.dao.BrushCupBoardLogDao;
import com.ubox.card.device.Device;
import com.ubox.card.util.StrUtil;
import com.ubox.card.util.TimeUtil;
import com.ubox.card.util.logger.Logger;

import java.util.concurrent.ConcurrentHashMap;

public class DeviceWorkProxy extends Device {
    
    private final Device instance = CardContext.getCardInstance();
    public static String ORDER_NO = "";
    public static boolean COSTING = false;//扣款逻辑是否正在进行中
    public static boolean MAYBECANCEL = true;//是否支持取消，默认支持
    public static String SERNO = "";//缓存消息流水号
    public static ConcurrentHashMap<String, String> CANCELMAP = new ConcurrentHashMap<String, String>();//缓存交易流水号集合
    
    @Override
    public void init() { } //DO NOTHING

    @Override
    public ExtResponse cardInfo(String json) {
        String repJSON = pttJSON(json, CardConst.CARD_REP);

        try {
        	ExtResponse ext =instance.cardInfo(repJSON);
//        	String infoJSON = instance.cardInfo(repJSON);
        	return ext;
        } catch(Throwable tr) {
        	Logger.error("cardInfo error", tr);
        	tr.printStackTrace();
//        	return pExceptionJSON(repJSON);
        	return new ExtResponse();
        }
    }

    @Override
    public ExtResponse cost(String json) {
    	ORDER_NO = StrUtil.genSeq();
    	COSTING = true;
    	
    	String repJSON = "";
        
        try {
        	
        	ExtResponse ret = JSON.parseObject(json, ExtResponse.class);
            ret.setMsgType(CardConst.COST_REP);
            ret.setDeviceId(CardJson.appType);
            ret.setCreateTime(TimeUtil.getCurrentTime());

            repJSON = JSON.toJSONString(ret);
            
//        	String costRet = instance.cost(repJSON);
            ExtResponse result = instance.cost(repJSON);

//        	ExtResponse result = JSON.parseObject(costRet, ExtResponse.class);
        	if(CardConst.SUCCESS_CODE == result.getResultCode().intValue()) 
        		costRecordsPersist(result); // 持久化交易数据

//        	return costRet;
        	return result;
        } catch(Throwable tr) {
        	Logger.error("cost error. "+tr.getMessage(), tr);
        	tr.printStackTrace();
//        	return pExceptionJSON(repJSON);
        	return new ExtResponse();
        } finally{
        	COSTING = false;
        }
    }
    
    /**
     * 让UVP显示文本信息
     * 
     * @param txt 文本信息
     */
    /*
    private void uvpTextShow(String txt) {
        ExtResponse textshow          = new ExtResponse();
        HashMap<String, Object> data  = new HashMap<String, Object>();
        
        data.put("value", txt);
        data.put("timeout", 15);
        
        textshow.setVmId(Config.vmId);
        textshow.setMsgType("txtShow");
        textshow.setDeviceId(Config.appType);
        textshow.setSerialNo(123123123123L);
        textshow.setCreateTime(TimeUtil.getTime2());
        textshow.setData(data);
        
        String textJSON = JSON.toJSONString(textshow);
        Logger.info(">>>> TextShow: " + textJSON);
        CardService.sendMsg(textJSON);
    }
    */
    
    /**
     * "卡信息响应"预处理
     * @param msgJSON "卡信息请求"
     * @return "卡信息响应"
     */
    private String pttJSON(String msgJSON, String msgType) {
        ExtResponse ret = JSON.parseObject(msgJSON, ExtResponse.class);
        
        ret.setMsgType(msgType);
        ret.setDeviceId(CardJson.appType);
        ret.setCreateTime(TimeUtil.getCurrentTime());

        return JSON.toJSONString(ret);
    }
    
    /**
     * 底层异常处理
     * @param original
     * @return 返回CVS的JSON
     */
//    private String pExceptionJSON(String original) {
//        ExtResponse ext = Converter.cJSON2ExtRep(original);
//        ext.setResultCode(201);
//        ext.setResultMsg("FAIL");
//        
//        return JSON.toJSONString(ext);
//    }

    /**
     * 持久化交易数据
     * 
     * @param result 交易数据
     */
    private void costRecordsPersist(ExtResponse result) {
        CostRep costRep = JSON.parseObject(JSON.toJSONString(result.getData()), CostRep.class);
        
        if(costRep.getOrderNo() == null || costRep.getOrderNo().equals("")) {
        	costRep.setOrderNo(DeviceWorkProxy.ORDER_NO);
        }
        
        WorkPool.executeTask(new CostPersit(result, costRep));
    } 
    
    /**
     * 扣款记录持久化任务
     */
    private static class CostPersit implements Runnable {
        private final ExtResponse ext;
        private final CostRep cost;
        
        CostPersit(ExtResponse ext, CostRep cost) {
            this.ext   = ext;
            this.cost  = cost;
        }
        
        @Override
        public void run() {
            Logger.info("BrushCupBoardLog[START]");
            BrushCupBoardLog persitData = new BrushCupBoardLog();
            String seq =  cost.getOrderNo();;
            
            try {
                persitData.setId(seq);   			    	   		       // card生成的订单号  - 不可空
                persitData.setVmId(ext.getVmId());     				       // 售货机ID - 不可空
                persitData.setMdseId(cost.getProduct().getProId());        // 商品ID
                persitData.setMdseName(cost.getProduct().getProName());    // 商品名称
                persitData.setMdsePrice(cost.getProduct().getProPrice());  // 商品价格
                persitData.setCostMoney(cost.getProduct().getSalePrice()); // 商品售卖价格-card实际扣款价格
                persitData.setCardNo(cost.getCards()[0].getCardNo());      // 卡编号或者卡序列号
                persitData.setBrushSeq(seq);                               // card生成的订单号  - 不可空
                persitData.setEmployeeNo(cost.getOutOrderNo());            // 谁让card系统去进行扣款，就记录谁的订单号（目前是UVP的订单号）
                persitData.setCostSeq(cost.getThirdOrderNo());             // 第三方交易订单号 - 刷卡合作商的订单号
                persitData.setPosId(cost.getCards()[0].getPosId());        // 刷卡设备编号 - 可空
                persitData.setCardType(cost.getCards()[0].getCardType());  // 卡类型
                persitData.setAppType(CardJson.appType);                   // 刷卡业务类型 - 不可空
                persitData.setCardDesc(cost.getCards()[0].getCardDesc());  // 卡描述 - 可空
                persitData.setBrushTime(TimeUtil.getCurrentTime());   	   // 刷卡时间 - 不可空
                persitData.setCreateTime(TimeUtil.getCurrentTime());       // 创建时间 - 不可空
                persitData.setCostTime(TimeUtil.getCurrentTime());         // 扣款时间 - 不可空
                persitData.setCostStatus(ext.getResultCode());             // 扣款结果码 - 不可空
                persitData.setIsValiDate(CardJson.isBeforeCost);           // 无用冗余字段
                
                BrushCupBoardLogDao.getInstance().insertOne(persitData);   // 入库操作
            } catch(Exception e) {
                Logger.error(">>>> COST-PERSIT ERROR.", e);
            }
                
            Logger.info("BrushCupBoardLog[END]");
        }
    }
}

