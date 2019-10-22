package com.ubox.card.device.hzsmk;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;

import com.ubox.card.bean.db.HZSMKTradeObj.HZSMKTrade;
import com.ubox.card.bean.external.Card;
import com.ubox.card.bean.external.CostRep;
import com.ubox.card.bean.external.ExtResponse;
import com.ubox.card.business.CancelProcesser;
import com.ubox.card.business.Converter;
import com.ubox.card.config.CardConst;
import com.ubox.card.config.CardJson;
import com.ubox.card.device.Device;
import com.ubox.card.util.Utils;
import com.ubox.card.util.logger.Logger;

public class HZSMK extends Device {
	
	private HZSMKer hzwoker = HZSMKer.getInstance();
	private static int serialNo=0;

	@Override
	public void init() {
//		if (HZSMKer.RESULT.SUCCESS != hzwoker.resetCard()) {
//			Logger.error("resetCare failure");
//		}
//		hzwoker.blackCard();
// 黑名单处理
		try {
			HZSMKBlackList.initBlackList();
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}

	@Override
	public ExtResponse cardInfo(String json) {
		return null;
	}

	@SuppressLint({ "SimpleDateFormat", "UseValueOf" })
	@Override
	public ExtResponse cost(String json) {
		Logger.info(">>>> cost is start : " + json);
		ExtResponse ext = Converter.cJSON2ExtRep(json);
		CostRep     rep = Converter.costReq2costRep(Converter.cJSON2CostReq(json));
		String proid=rep.getProduct().getProId()+"";
		String proname=rep.getProduct().getProName();
		String proprice=rep.getProduct().getProPrice()+"";
		
		HZSMKUtil.setProId(proid);
		HZSMKUtil.setProName(proname);
		HZSMKUtil.setProPrice(proprice);
		
		String resultJson = null;
		long startTime = System.currentTimeMillis();

		try {
			if (HZSMKer.RESULT.SUCCESS != hzwoker.open()) {
				ext.setResultCode(CardConst.EXT_BALANCE_LESS);
				ext.setResultMsg("打开串口失败！");
//				resultJson = Converter.genCostRepJSON(rep, ext);
				Logger.info("<<< cost time:"+ (System.currentTimeMillis() - startTime) / 1000.00+ "s, data:" + resultJson);
				return Converter.genCostRepJSON(rep, ext);
			}
		} catch (Exception e) {
			ext.setResultCode(CardConst.EXT_BALANCE_LESS);
			ext.setResultMsg("打开串口失败！");
//			resultJson = Converter.genCostRepJSON(rep, ext);
			Logger.info("<<< cost time:"+ (System.currentTimeMillis() - startTime) / 1000.00+ "s, data:" + resultJson);
			return Converter.genCostRepJSON(rep, ext);
		}
		
		try{
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
				if (System.currentTimeMillis() - curTime >= (1000*timeout)){
					ext.setResultCode(CardConst.EXT_BALANCE_LESS);
					ext.setResultMsg("读卡 "+timeout+" 秒超时");
//					resultJson = Converter.genCostRepJSON(rep, ext);
	                Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson);
	        		return Converter.genCostRepJSON(rep, ext);
				}
				
				Thread.sleep(100);
				
				if (HZSMKer.RESULT.SUCCESS == hzwoker.readCard()) {//读卡成功
					break;
				} 
				if (HZSMKer.RESULT.NORESPONSE == hzwoker.readCard()) {//没读到卡，继续读卡
					continue;
				}
				if (HZSMKer.RESULT.ERROR == hzwoker.readCard()) {//读卡失败
					ext.setResultCode(CardConst.EXT_BALANCE_LESS);
					ext.setResultMsg("读卡失败！");
//					resultJson = Converter.genCostRepJSON(rep, ext);
					Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson);
					return Converter.genCostRepJSON(rep, ext);
				}
			}
			//判断余额
			if(HZSMKUtil.getInitialAmount() < rep.getProduct().getSalePrice()){
				double d = new Double(HZSMKUtil.getInitialAmount())/100;
		    	Format f = new DecimalFormat("0.00");
				ext.setResultCode(CardConst.EXT_BALANCE_LESS);
				ext.setResultMsg("余额不足, 当前余额为："+f.format(d)+" 元");
//				resultJson = Converter.genCostRepJSON(rep, ext);
				Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson+". cost money" + 
							rep.getProduct().getSalePrice()+"," + HZSMKUtil.getInitialAmount());
				return Converter.genCostRepJSON(rep, ext);
			}
			//判断有效日期
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
			String Time=formatter.format(new java.util.Date());
			int nowDate = Integer.parseInt(Time);
			
			int date = Integer.parseInt(HZSMKUtil.getValidDate());
			if(date < nowDate){
				ext.setResultCode(CardConst.EXT_BALANCE_LESS);
				ext.setResultMsg("此卡已过期！");
//				resultJson = Converter.genCostRepJSON(rep, ext);
				Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson + ". current time :" + 
							nowDate+"," + date);
				return Converter.genCostRepJSON(rep, ext);
			}
			//判断钱包区金额
			int moneys = 500000;
			if(HZSMKUtil.getInitialAmount() > moneys){
				ext.setResultCode(CardConst.EXT_BALANCE_LESS);
				ext.setResultMsg("卡金额不合法！");
//				resultJson = Converter.genCostRepJSON(rep, ext);
				Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson+". money :" + 
							HZSMKUtil.getInitialAmount());
				return Converter.genCostRepJSON(rep, ext);
			}
			//判断城市代码
			if(!HZSMKUtil.getCityCode().equals("3100")){
				ext.setResultCode(CardConst.EXT_BALANCE_LESS);
				ext.setResultMsg("城市代码不合法！");
//				resultJson = Converter.genCostRepJSON(rep, ext);
				Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson+". citycode : " + 
							HZSMKUtil.getCityCode());
				return Converter.genCostRepJSON(rep, ext);
			}
			//判断为M1卡,进行M1消费
			int money = rep.getProduct().getSalePrice();
			String RNo = HZSMKUtil.getReleaseNo().substring(0, 1);//发行流水号最高位大于0
			int number = Integer.parseInt(RNo);
			String car = "ffffffff"+HZSMKUtil.getCardNo();
			
			if (HZSMKUtil.getSakValue().equals("08")||HZSMKUtil.getSakValue().equals("18")||HZSMKUtil.getSakValue().equals("88")||
			    HZSMKUtil.getSakValue().equals("00")||HZSMKUtil.getSakValue().equals("09")||HZSMKUtil.getSakValue().equals("04")||
			    HZSMKUtil.getSakValue().equals("28")){// 判断为M1卡,进行M1消费
				if(HZSMKUtil.cardType.equals("18")||(HZSMKUtil.cardType.equals("04") && number > 0)){
					if(false == HZSMKBlackList.isM1Black(car)){//判断卡号为黑名单
						if (HZSMKer.RESULT.SUCCESS == hzwoker.M1pay(money)) {
							ext.setResultCode(CardConst.EXT_SUCCESS);
							ext.setResultMsg("M1消费成功");
						} else {
							ext.setResultCode(CardConst.EXT_BALANCE_LESS);
							ext.setResultMsg("M1卡消费失败！");
//							resultJson = Converter.genCostRepJSON(rep, ext);
					        Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson);
							return Converter.genCostRepJSON(rep, ext);
						}
					} else {
						ext.setResultCode(CardConst.EXT_BALANCE_LESS);
						ext.setResultMsg("为M1黑名单卡！");
//						resultJson = Converter.genCostRepJSON(rep, ext);
						Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson + ". card :" +
									HZSMKUtil.getCardNo());
						return Converter.genCostRepJSON(rep, ext);
					}
				}else {
					ext.setResultCode(CardConst.EXT_BALANCE_LESS);
					ext.setResultMsg("M1卡类型不正确！");
//					resultJson = Converter.genCostRepJSON(rep, ext);
				    Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson + ". cardtype :"
				        		+ HZSMKUtil.cardType);
					return Converter.genCostRepJSON(rep, ext);
				}
			} else if (HZSMKUtil.getSakValue().equals("20")||HZSMKUtil.getSakValue().equals("29")||
					   HZSMKUtil.getSakValue().equals("39")) {// 判断为CPU卡,进行CPU消费
				if(HZSMKUtil.cardType.equals("17")||HZSMKUtil.cardType.equals("23")||
				   HZSMKUtil.cardType.equals("24")||HZSMKUtil.cardType.equals("25")){
					if (false == HZSMKBlackList.isCpuBlack(HZSMKUtil.getAppNo())) {//判断应用序列号
						if (HZSMKer.RESULT.SUCCESS == hzwoker.CPUpay(money)) {
							ext.setResultCode(CardConst.EXT_SUCCESS);
							ext.setResultMsg("CPU消费成功");
						} else {
							ext.setResultCode(CardConst.EXT_BALANCE_LESS);
							ext.setResultMsg("CPU卡消费失败！");
//							resultJson = Converter.genCostRepJSON(rep, ext);
							Logger.info("<<< cost time:"+ (System.currentTimeMillis() - startTime)/ 1000.00 + "s, data:" + resultJson);
							return Converter.genCostRepJSON(rep, ext);
						}
					} else {
						ext.setResultCode(CardConst.EXT_BALANCE_LESS);
						ext.setResultMsg("为CPU黑名单卡!");
//						resultJson = Converter.genCostRepJSON(rep, ext);
						Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson + ". card :" +
									HZSMKUtil.getCardNo());
						return Converter.genCostRepJSON(rep, ext);
					}
				} else {
					ext.setResultCode(CardConst.EXT_BALANCE_LESS);
					ext.setResultMsg("CPU卡类型不正确!");
//					resultJson = Converter.genCostRepJSON(rep, ext);
				    Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson + ". cardtype :"
				        		+ HZSMKUtil.cardType);
					return Converter.genCostRepJSON(rep, ext);
				}
			} else {
				ext.setResultCode(CardConst.EXT_BALANCE_LESS);
				ext.setResultMsg("SAK值不合法!");
//				resultJson = Converter.genCostRepJSON(rep, ext);
				Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson + ". SAK :" + 
						HZSMKUtil.getSakValue());
				return Converter.genCostRepJSON(rep, ext);
			}
			
		} catch (Exception e) {
			Logger.error("cost is exception. "+e.getMessage()+"::", e);
			ext.setResultCode(CardConst.EXT_BALANCE_LESS);
    		ext.setResultMsg("扣款失败!");
//    		resultJson = Converter.genCostRepJSON(rep, ext);
            Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson);
    		return Converter.genCostRepJSON(rep, ext);
		}finally{
			hzwoker.close();
		}
		//上传数据
		serialNo++;
		HZSMKUtil.setPTNo(serialNo+"");
		
		HZSMKTrade trade = new HZSMKTrade();
		trade.setBrushSeq(rep.getOrderNo());
		trade.setVmId(CardJson.vmId);
		trade.setClientTime(M1Util.getTradTime());
		trade.setCardDesc(HZSMKUtil.getInitialAmount()+" tenth");
		trade.setProductId(HZSMKUtil.getProId());
		trade.setProductName(HZSMKUtil.getProName());
		trade.setTradeType(HZSMKUtil.getTradType());
		trade.setCardNo(HZSMKUtil.getCardNo());
		trade.setReleaseNo(HZSMKUtil.getReleaseNo());
		trade.setCertifyCode(HZSMKUtil.getCertifyCode());
		trade.setCityCode(HZSMKUtil.getCityCode());
		trade.setIndustryCode(HZSMKUtil.getIndustryCode());
		trade.setM1UseFlag(HZSMKUtil.getM1UseFlag());
		trade.setCardType(HZSMKUtil.getCardType());
		trade.setValidDate(HZSMKUtil.getValidDate());
		trade.setUseDate(HZSMKUtil.getUseDate());
		trade.setAddMoneyDate(HZSMKUtil.getAddMoneyDate());
		trade.setM1AddMoneyBalance(HZSMKUtil.getM1AddMoneyBalance());
		trade.setWalletBalance(HZSMKUtil.getInitialAmount()+"");
		trade.setWalletTradeNo(HZSMKUtil.getWalletTradeNo());
		trade.setM1BlackFlag(HZSMKUtil.getM1BlackFlag());
		trade.setCheckDate(HZSMKUtil.getCheckDate());
		trade.setNowOperatorNo(HZSMKUtil.getNowOperatorNo());
		trade.setSakValue(HZSMKUtil.getSakValue());
		trade.setAppNo(HZSMKUtil.getAppNo());
		trade.setSubCardType(HZSMKUtil.getSubCardType());
		trade.setCpuWalletUseFlag(HZSMKUtil.getBrushSeq());
		trade.setPsamCardNo(HZSMKUtil.getPsamCardNo());
		trade.setTradeTime(M1Util.getTradTime());
		trade.setTradeMoney(rep.getProduct().getSalePrice()+"");
		trade.setBalance(HZSMKUtil.getInitialAmount()+"");
		trade.setTac(M1Util.getTAC());
		trade.setPsamOfflineTradeNo(M1Util.getPSAMId());
		trade.setPosTradeNo(HZSMKUtil.getPTNo());
		trade.setProductPrice(HZSMKUtil.getProPrice());

		hzwoker.PreservationData(trade);
		
		Card card = rep.getCards()[0];
        card.setCardDesc(M1Util.getTAC());// 物理卡号|tac
        card.setPosId(CardJson.vmId);// POS机终端号
        card.setCardNo(HZSMKUtil.getCarId());// 卡号
        card.setCardBalance(HZSMKUtil.getInitialAmount() - rep.getProduct().getSalePrice());// 卡余额
        rep.setThirdOrderNo(HZSMKUtil.getPTNo());// 交易流水号
    	
//		resultJson = Converter.genCostRepJSON(rep, ext);
        Logger.info("<<< cost time:"+(System.currentTimeMillis()-startTime)/1000.00+"s, data:"+resultJson);
		return Converter.genCostRepJSON(rep, ext);
	}

}
