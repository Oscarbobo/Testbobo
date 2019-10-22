package com.ubox.card.device.whtlf;

import java.util.Map;

import com.ubox.card.bean.external.Card;
import com.ubox.card.bean.external.CostRep;
import com.ubox.card.bean.external.ExtResponse;
import com.ubox.card.business.CancelProcesser;
import com.ubox.card.business.Converter;
import com.ubox.card.business.DeviceWorkProxy;
import com.ubox.card.config.CardConst;
import com.ubox.card.device.Device;
import com.ubox.card.util.Utils;
import com.ubox.card.util.logger.Logger;

public class WHTLF extends Device{
	
	private WHTLFer worker = WHTLFer.getInstance();
	
	@Override
	public void init() {
		DeviceWorkProxy.MAYBECANCEL = true;
	}

	@Override
	public ExtResponse cardInfo(String json) {
		// do nothing
		return null;
	}

	@Override
	public ExtResponse cost(String json) {
		Logger.info(">>>> cost : "+json);
        ExtResponse ext = Converter.cJSON2ExtRep(json);
        CostRep     rep = Converter.costReq2costRep(Converter.cJSON2CostReq(json));
        String resultJson = "";
        
        long startTime = System.currentTimeMillis();
        
        //打开串口
        try {
			if(WHTLFer.RESULT.SUCCESS != worker.open()){
				ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
        		ext.setResultMsg("打开串口失败");
//        		resultJson = Converter.genCostRepJSON(rep, ext);
                Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson);
        		return Converter.genCostRepJSON(rep, ext);
			}
		} catch (Exception e) {
			Logger.error("cost is exception. "+e.getMessage(), e);
			ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
    		ext.setResultMsg("打开串口失败");
//    		resultJson = Converter.genCostRepJSON(rep, ext);
    		Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson);
    		return Converter.genCostRepJSON(rep, ext);
		}
        
        try {
        	//测试设备
        	if(WHTLFer.RESULT.SUCCESS != worker.test()){
				ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
        		ext.setResultMsg("测试设备失败");
//        		resultJson = Converter.genCostRepJSON(rep, ext);
                Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson);
        		return Converter.genCostRepJSON(rep, ext);
        	}
        	
        	//读卡
        	Map<String,Integer> map = null;
			int timeout = 10;//寻卡超时时间（秒）
			long curTime = System.currentTimeMillis();
			while(true){
				//处理中断功能
				if (Utils.isCancel(ext.getSerialNo()+"") == true) {
//					resultJson = CancelProcesser.cancelProcess(json);
					Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson);
					return CancelProcesser.cancelProcess(json);
				}
				
				//处理超时
				if (System.currentTimeMillis() - curTime >= (1000*timeout)) {
					ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
					ext.setResultMsg("读卡"+timeout+"秒 超时");
//					resultJson = Converter.genCostRepJSON(rep, ext);
	                Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson);
	        		return Converter.genCostRepJSON(rep, ext);
				}
				
				//休息会
				Thread.sleep(100);
				
				//调用底层寻卡
				map = worker.read();
				int code = map.get("code");//0:成功，1:无卡, 2:需要复位(PW), 3:没有应答, x:其他都是错误 
				if(code == 1 || code == 3){
					continue;
				}
				if(code == 0){
					break;
				}
				if(code == 2){
					//复位
					if(WHTLFer.RESULT.SUCCESS != worker.reset()){
						ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
						ext.setResultMsg("复位失败");
//						resultJson = Converter.genCostRepJSON(rep, ext);
		                Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson);
		        		return Converter.genCostRepJSON(rep, ext);
					}
				}else{
					ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
					ext.setResultMsg("读卡失败");
//					resultJson = Converter.genCostRepJSON(rep, ext);
	                Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson);
	        		return Converter.genCostRepJSON(rep, ext);
				}
			}

			//扣费
        	int money = rep.getProduct().getSalePrice();
        	WHTLFer.RESULT ret = worker.pay(money);
        	
        	if(WHTLFer.RESULT.SUCCESS == ret) { // 扣款成功
        		Card card = rep.getCards()[0];
        		
        		card.setCardBalance(map.get("balance")-money); 
        		ext.setResultCode(CardConst.EXT_SUCCESS);
        		ext.setResultMsg("扣款成功");
        	} else {
        		ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
        		ext.setResultMsg("扣款失败");
        	}
		} catch (Exception e) {
			Logger.error("cost is exception. "+e.getMessage(), e);
			ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
    		ext.setResultMsg("扣款失败");
		} finally{
			//关闭串口
			worker.close();
		}
        
//        resultJson = Converter.genCostRepJSON(rep, ext);
        Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson);
		return Converter.genCostRepJSON(rep, ext);
	}
}
