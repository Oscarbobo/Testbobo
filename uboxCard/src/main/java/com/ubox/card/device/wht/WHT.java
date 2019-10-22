package com.ubox.card.device.wht;

import com.ubox.card.bean.db.WHTTradeObj.WHTTrade;
import com.ubox.card.bean.external.Card;
import com.ubox.card.bean.external.CostRep;
import com.ubox.card.bean.external.ExtResponse;
import com.ubox.card.business.Converter;
import com.ubox.card.config.CardConst;
import com.ubox.card.db.dao.WHTTradeDao;
import com.ubox.card.device.Device;
import com.ubox.card.device.wht.blacklist.BlackList;
import com.ubox.card.util.logger.Logger;

public class WHT extends Device {
	
	private final WHTWorker sler = new WHTWorker();
	private static int SUCCESS = 0;
	@Override
	public void init() {
		// 终端黑名单定时与服务器进行同步，并写入文件，并更新到内存
		try {
			BlackList.initBlackList();
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}

	@Override
	public ExtResponse cardInfo(String json) {
		ExtResponse ext = Converter.cJSON2ExtRep(json);
		return ext;
	}

	@Override
	public ExtResponse cost(String json) {
//		String resultJson = "";
		ExtResponse ext = Converter.cJSON2ExtRep(json);
		CostRep     rep = Converter.costReq2costRep(Converter.cJSON2CostReq(json));
        try {

        	sler.open();
            
            if(sler.testComm(ext.getSerialNo()+"")!=0) {
            	ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
            	ext.setResultMsg("设备通信测试失败");
            	return Converter.genCostRepJSON(rep, ext);
            }
            
            int timeout = 10;
            long startTime = System.currentTimeMillis();
            String[] read = null;
            while(true){
            	if(System.currentTimeMillis() - startTime > timeout*1000){
            		ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
                	ext.setResultMsg("读取卡信息超时");
                	return Converter.genCostRepJSON(rep, ext);
            	}

            	read = sler.read(ext.getSerialNo()+"");
            	if("0".equals(read[0])) {
            		break;
            	}
            	if(!"1".equals(read[0])){
        			ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
        			ext.setResultMsg("读取卡信息失败");
        			return Converter.genCostRepJSON(rep, ext);
        		}
            }
            
            if(true == BlackList.isBlack(read[1])) { // 黑卡
            	ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
            	ext.setResultMsg("黑卡,不能交易");
            	return Converter.genCostRepJSON(rep, ext);
            }
            
            int      price = rep.getProduct().getSalePrice();
            Object[] status  = sler.cost(price,ext.getSerialNo()+"");
            
            if(SUCCESS != Integer.parseInt(status[0].toString())) {
            	ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
            	ext.setResultMsg("扣款失败");
            	return Converter.genCostRepJSON(rep, ext);
            }
            String  vcseq = rep.getOrderNo();//StrUtil.genOrderNo(); 
            
            Card card = rep.getCards()[0];
            
            card.setCardDesc(String.valueOf(status[3])+"|"+String.valueOf(status[10]));        // 物理卡号|tac
            card.setPosId(String.valueOf(status[5]));                                          // POS机终端号
            card.setCardNo(String.valueOf(status[2]));                                         // 卡号
            card.setCardBalance((Integer)status[1]);                                           // 卡余额
            rep.setThirdOrderNo(String.valueOf(status[5]));                                    // 交易流水号
        	
            ext.setResultCode(CardConst.EXT_SUCCESS);
            ext.setResultMsg("扣款成功");

            WHTTrade trade = new WHTTrade();
            trade.setId(vcseq);
			trade.setDevNo(String.valueOf(status[5]));
			trade.setDevFlag(String.valueOf(1));
			trade.setTradeTime(String.valueOf(status[11]));
			trade.setDevCount(String.valueOf(status[9]));
			trade.setCardNo(String.valueOf(status[2]));
			trade.setPhyNo(String.valueOf(status[3]));
			trade.setcMain(String.valueOf(status[6]));
			trade.setsMain(String.valueOf(status[7]));
			trade.setBdevNo(String.valueOf(status[5]));
			trade.setbTime(String.valueOf(status[11]));
			trade.setTradeFee(price);
			trade.setBalance((Integer)status[1]);
			trade.setTradeType(String.valueOf(13));
			trade.setTdevNo(String.valueOf(status[5]));
            trade.setThisTime(String.valueOf(status[11]));
            trade.setTicketOnCount("0");
            trade.setTicketOffCount(String.valueOf((Integer )status[8]+ 1));
            trade.setTac(String.valueOf(status[10]));
            trade.setIsTest("8");//8:正式,9:测试
			trade.setVmId(ext.getVmId());
			trade.setBrushSeq(vcseq);

            persist(trade);
            
//            resultJson = Converter.genCostRepJSON(rep, ext);
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		} finally{
			sler.close();
		}
        return Converter.genCostRepJSON(rep, ext);
	}
	
	/**
	 * 保存扣款记录
	 * @param cost 扣款记录
	 */
	private void persist(final WHTTrade whtdt) {

		/**
		 * DB文件太大,会导致I/O读写速度缓慢,影响刷卡流程.因此另起线程处理DB读写
		 */
		new Thread(new Runnable() {
			@Override
			public void run() {
				Logger.info("WHTDTTradeHandler Runnable start....");
				WHTTradeDao.getInstance().insertOne(whtdt);// 入库操作
				//Logger.info(whtdt.toString());// 记录日志

				Logger.info("WHTDTTradeHandler Runnable end....");
			}
		}).start();
	}
}
