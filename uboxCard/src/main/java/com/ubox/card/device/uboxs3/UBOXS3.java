package com.ubox.card.device.uboxs3;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.ubox.card.bean.external.Card;
import com.ubox.card.bean.external.CostRep;
import com.ubox.card.bean.external.ExtResponse;
import com.ubox.card.business.CancelProcesser;
import com.ubox.card.business.Converter;
import com.ubox.card.config.CardConst;
import com.ubox.card.config.CardJson;
import com.ubox.card.device.Device;
import com.ubox.card.device.uboxs3.UBOXSER3.RESULT;
import com.ubox.card.util.Utils;
import com.ubox.card.util.logger.Logger;

public class UBOXS3 extends Device {

	private UBOXSER3 worker = new UBOXSER3();
	
	@Override
	public void init() {
	}

	@Override
	public ExtResponse cardInfo(String json) {
		return null;
	}

	@Override
	public ExtResponse cost(String json) {

		Logger.info(">>>>> cost:"+json);
        ExtResponse ext = Converter.cJSON2ExtRep(json);
        CostRep     rep = Converter.costReq2costRep(Converter.cJSON2CostReq(json));
        
        //打开串口
        try {
			worker.open();
		} catch (Exception e) {
			Logger.error("open is exception. "+e.getMessage(), e);
			ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
            ext.setResultMsg("打开串口异常");
            
            return Converter.genCostRepJSON(rep, ext);
		}
        
        /*
         * 指令交互
         */
        try {
        	//读卡
			int timeout = 20;//寻卡超时时间（秒）
			long curTime = System.currentTimeMillis();
			while(true){
				//处理中断功能
				if (com.ubox.card.util.Utils.isCancel(ext.getSerialNo()+"") == true) {
					Logger.info("<<<<<<<<<<<<<<<   Cost is cancle. ");
					return CancelProcesser.cancelProcess(json);
				}
				
				//处理超时
				if (System.currentTimeMillis() - curTime >= (1000*timeout)) {
					ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
					ext.setResultMsg("读卡超时");
					Logger.info("<<<<<<<<<<<<<<<   Read card time out. ");
					return Converter.genCostRepJSON(rep, ext);
				}
				
				//休息会
				Thread.sleep(100);
				
				//调用底层寻卡
				RESULT status  = worker.readCard(ext.getSerialNo()+"");
				if(status == RESULT.SUCCESS){
					Logger.info("find card successful.");
					break;
				}else{
					if(status == RESULT.CANCEL){
						return CancelProcesser.cancelProcess(json);
					}
					if(status != RESULT.TIMEOUT){
						continue;
					}
				}
			}
			
			String tmp = UBOXSER3.int2string(Long.parseLong(rep.getOrderNo()),20);
			Logger.info("<<<<<<<<<<< tmp : "+tmp);
			String tmp1 = "0000000000000000000000000000000000000000";
			String vmid = CardJson.vmId; 
			if(StringUtils.isNumeric(vmid)){
				tmp1 = tmp1+vmid;
				tmp1 = tmp1.substring(tmp1.length()-20, tmp1.length());
			}
			byte[] proid = Utils.intToByte(rep.getProduct().getProId());
			Logger.info("<<<<<<<<<<<<< Proid: " + rep.getProduct().getProId() + ". proid : " + Utils.toHex(proid));
			
			//休息会
			Thread.sleep(100);
			
			Map<String,Object> costMap =worker.cost(rep.getProduct().getSalePrice(),tmp,tmp1,proid);
			if(costMap.get("result").equals("00")){
				Logger.info("offline cost successful. "+costMap.toString());
				
	            Card card = rep.getCards()[0];

	            card.setPosId(String.valueOf(costMap.get("cardDev")));                                          
	            card.setCardNo(String.valueOf(costMap.get("cardNO")));                                        
	            card.setCardBalance((Integer)costMap.get("cardBalance"));
	            card.setCardDesc(card.getCardBalance()+"|"+String.valueOf(costMap.get("costDate")));
	            rep.setThirdOrderNo(String.valueOf(costMap.get("costSeq")));
				rep.getProduct().setSalePrice((Integer) costMap.get("cardCostMoney"));//当出现折扣时，没能显示折扣后的扣款金额（河南亚杰刷卡出现此问题）
	            
	            ext.setResultCode(CardConst.EXT_SUCCESS);
	            ext.setResultMsg("扣款成功");

			}else{
				Logger.warn("cost fail. "+costMap.toString());
				ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
				int resultCode = Integer.parseInt(costMap.get("result").toString());
				if(resultCode == 2){
					ext.setResultCode(CardConst.EXT_BALANCE_LESS);
					ext.setResultMsg("余额不足，当前余额： "+((Integer)costMap.get("cardBalance"))/100.00+" 元");	
					
					Card card = rep.getCards()[0];
		            card.setCardBalance((Integer)costMap.get("cardBalance")); 
				}else if(resultCode == 3){
					ext.setResultMsg("黑名单卡");
				}else if(resultCode == 4){
					ext.setResultMsg("无效卡");
				}else if(resultCode == 5){
					ext.setResultMsg("扣款失败");
				}else if(resultCode == 6){
					ext.setResultMsg("扣款失败，其他错误");
				}else {
					ext.setResultMsg("扣款失败");
				}
			}
		} catch (Exception e) {
			Logger.error("cost is exception. " + e.getMessage(), e);
			e.printStackTrace();
			ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
			ext.setResultMsg("扣款失败");

			return Converter.genCostRepJSON(rep, ext);
		} finally {
			// 关闭串口
			worker.close();
		}

		return Converter.genCostRepJSON(rep, ext);
	}
}
