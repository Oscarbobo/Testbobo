package com.ubox.card.device.cqgyxx;

import com.ubox.card.bean.external.Card;
import com.ubox.card.bean.external.CostRep;
import com.ubox.card.bean.external.ExtResponse;
import com.ubox.card.business.CancelProcesser;
import com.ubox.card.business.Converter;
import com.ubox.card.config.CardConst;
import com.ubox.card.device.Device;
import com.ubox.card.util.logger.Logger;

import java.util.Map;



public class CQGYXX extends Device {
    private CQGYXXSER worker = new CQGYXXSER();
    @Override
    public void init() {}

    @Override
    public ExtResponse cardInfo(String json) {
        return null;
    }

    @Override
    public ExtResponse cost(String json) {
        Logger.info(">>>>> cost:"+json);
        ExtResponse ext = Converter.cJSON2ExtRep(json);
        CostRep rep = Converter.costReq2costRep(Converter.cJSON2CostReq(json));

        String resultJson = "";
        long startTime = System.currentTimeMillis();

        //1,打开串口
        try {
            if(CQGYXXSER.RESULT.SUCCESS != worker.open()){
                ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
                ext.setResultMsg("打开串口失败");
                Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson);
                return Converter.genCostRepJSON(rep, ext);
            }
        } catch (Exception e) {
            Logger.error("cost is exception. "+e.getMessage(), e);
            ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
            ext.setResultMsg("打开串口失败");
            Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson);
            return Converter.genCostRepJSON(rep, ext);
        }

        //2,循环读卡
        try {

            int timeout = 20;//寻卡超时时间（秒）
            long curTime = System.currentTimeMillis();
            while(true) {
                //2.1处理中断功能
                if (com.ubox.card.util.Utils.isCancel(ext.getSerialNo() + "") == true) {
                    return CancelProcesser.cancelProcess(json);
                }

                //2.2处理超时
                if (System.currentTimeMillis() - curTime >= (1000 * timeout)) {
                    ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
                    ext.setResultMsg("读卡超时:"+timeout);
                    return Converter.genCostRepJSON(rep, ext);
                }

                //休息会
                Thread.sleep(100);

                //2.3调用底层寻卡,查看是否有卡
                CQGYXXSER.RESULT status  = worker.readCard(ext.getSerialNo()+"");
                if(status == CQGYXXSER.RESULT.SUCCESS){
                    //读卡成功
                    Logger.info("find card successful.");
                   break;
                }else{
                    if(status == CQGYXXSER.RESULT.CANCEL){
                        return CancelProcesser.cancelProcess(json);
                    }
                    if(status != CQGYXXSER.RESULT.TIMEOUT){
                        Logger.error("find card exception. code:"+status);
                        ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
                        ext.setResultMsg("读卡失败");
                        return Converter.genCostRepJSON(rep, ext);
                    }
                }
            }



            //3,获取余额余额
            Map<String, Integer> balanceMap = worker.readCardMoney();
            Integer balance = balanceMap.get("balance");
            int balanceMoney = balance.intValue();


            //4,扣费
            //扣费
            int payMoney = rep.getProduct().getSalePrice();
            Map<String,Object> costMap =worker.pay(payMoney);

            //TODO 要不要先判断余额是否足够
            if(costMap.get("result").equals("00")){
                Logger.info("offline cost successful. "+costMap.toString());

                Card card = rep.getCards()[0];

                card.setCardNo(String.valueOf(costMap.get("cardNO")));//账号
                card.setPosId(String.valueOf(costMap.get("cardDev")));//pos机号
                card.setCardBalance((Integer)costMap.get("cardBalance"));  //余额
                card.setCardDesc(card.getCardBalance()+"|"+String.valueOf(costMap.get("costDate"))+"|"+String.valueOf(costMap.get("costTime")));
                rep.setThirdOrderNo(String.valueOf(costMap.get("costSeq")));

                ext.setResultCode(CardConst.EXT_SUCCESS);
                ext.setResultMsg("扣款成功");

            }else {
                Logger.warn("cost fail. " + costMap.toString());
                ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
                int resultCode = Integer.parseInt(costMap.get("result").toString());
                if (resultCode == 2) {
                    ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
                    ext.setResultMsg("余额不足");

                    Card card = rep.getCards()[0];
                    card.setCardBalance((Integer) costMap.get("cardBalance"));
                } else if (resultCode == 3) {
                    ext.setResultMsg("黑名单卡");
                } else if (resultCode == 4) {
                    ext.setResultMsg("无效卡");
                } else if (resultCode == 5) {
                    ext.setResultMsg("扣款失败");
                } else if (resultCode == 6) {
                    ext.setResultMsg("扣款失败，其他错误");
                } else {
                    ext.setResultMsg("扣款失败");
                }
            }

        } catch (Exception e) {
            Logger.error("cost is exception. "+e.getMessage(), e);
            ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
            ext.setResultMsg("扣款失败");
        } finally{
            //关闭串口
            worker.close();
        }

        Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson);
        return Converter.genCostRepJSON(rep, ext);
    }
}
