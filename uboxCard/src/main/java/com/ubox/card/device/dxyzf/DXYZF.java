package com.ubox.card.device.dxyzf;

import java.util.Arrays;

import com.ubox.card.bean.external.Card;
import com.ubox.card.bean.external.CostRep;
import com.ubox.card.bean.external.ExtResponse;
import com.ubox.card.business.CancelProcesser;
import com.ubox.card.business.Converter;
import com.ubox.card.business.DeviceWorkProxy;
import com.ubox.card.config.CardConst;
import com.ubox.card.device.Device;
import com.ubox.card.util.Utils;
import com.ubox.card.util.device.DeviceUtils;
import com.ubox.card.util.logger.Logger;

public class DXYZF extends Device {

	private final ProtocalWorker worker = new ProtocalWorker();
	
	@Override
	public void init() {
		// nothing
	}

	@Override
	public ExtResponse cardInfo(String json) {
		// nothing
		return null;
	}
	
	private final int TIME_OUT     = 30000; // 超时时间
	private final int FIND_SUCCESS = 0; 	// 找到卡片
	private final int FIND_FAIL    = 1;     // 未找到卡片
	private final int FIND_CANCEL  = 2;     // 取消刷卡

	@Override
	public ExtResponse cost(String json) {
        ExtResponse ext = Converter.cJSON2ExtRep(json);
        CostRep     rep = Converter.costReq2costRep(Converter.cJSON2CostReq(json));
        
        if(!worker.inquiry()) { // 设备查询失败
        	ext.setResultCode(CardConst.EXT_INIT_FAIL);
        	ext.setResultMsg("找不到设备");
        	return Converter.genCostRepJSON(rep, ext);
        }
        
        int findCode = findCard(TIME_OUT,ext.getSerialNo()+"");
        if(findCode != FIND_SUCCESS) { // 寻卡失败
        	if(findCode == FIND_CANCEL) {
        		return CancelProcesser.cancelProcess(json); // 撤销交易
        	} else {
        		ext.setResultCode(CardConst.EXT_READ_CARD_FAIL);
        		ext.setResultMsg("寻卡失败 ");
        		return Converter.genCostRepJSON(rep, ext);
        	}
        }
        
        if(Utils.isCancel(ext.getSerialNo()+"")) { 
        	return CancelProcesser.cancelProcess(json); // 撤销交易
        }
        
        int money    = rep.getProduct().getSalePrice();
        String seq   = DeviceWorkProxy.ORDER_NO; 
        String[] ret = cost(money, TIME_OUT, seq);
        
        if(ret[0] != DXYZFContext.SUCCESS) {
        	
        	if(ret[0].equals(DXYZFContext.BALANCE_LESS)) {
        		ext.setResultCode(CardConst.EXT_BALANCE_LESS);
        		ext.setResultMsg("余额不足");
        	} else {
        		ext.setResultCode(CardConst.EXT_CONSUEM_FAIL);
        		ext.setResultMsg("扣款失败");
        	}
        	
        	return Converter.genCostRepJSON(rep, ext);
        }
        
        Card card = rep.getCards()[0];
        card.setCardNo(ret[1]);
        card.setPosId(ret[3]);
        card.setCardBalance(null);
        
        rep.setOrderNo(seq); // card交易流水号
        rep.setThirdOrderNo(ret[4]); // POS机交易流水号
        
        ext.setResultCode(CardConst.EXT_SUCCESS);
        ext.setResultMsg("扣款成功");
        
		return Converter.genCostRepJSON(rep, ext);
	}
	
	/**
	 * 寻找卡片
	 * @param timeout 等待时间,单位 ms
	 * @return 0-找到卡片,1-超时,2-取消
	 */
	private int findCard(int timeout,String serNo) {
		int sleep = 500;
		int times = 0;
		while(times <= timeout) {

			if(Utils.isCancel(serNo)) {
				return FIND_CANCEL ; // 撤销交易
			}

			String status = worker.prepare();
			if(status.equals(DXYZFContext.SUCCESS)) {
				return FIND_SUCCESS; // 感应到卡片
			} else if(status.equals(DXYZFContext.FAIL)) {
				return FIND_FAIL; // 设备反馈命令失败
			} else {
				try {
					Thread.sleep(sleep); // 没有感应到卡片
				} catch (InterruptedException e) { 
				}
			}

			times += sleep;
		}
		
		// 跳出寻卡轮询,则寻卡超时
		return FIND_FAIL; 
	}
	
	/**
	 * 扣款
	 * @param money 扣款金额,单位 分
	 * @param timeout 等待时间,单位 ms
	 * @param seq card交易流水号
	 * @return 扣款结果:[0]=状态码,[1]=卡号(手机号),[2]=扣款金额,[3]=POSID,[4]=POS流水号,[5]=扣款时间
	 */
	private String[] cost(int money, int timeout, String seq) {
		byte[] amount = genAmount(money);
		byte[] swn    = genSwn(seq);
		byte[] cmd 	  = { 
				0x02, 
				0x11, (byte)0xA2, /* 长度, 命令 */
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /* 交易金额  */
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /* 系统流水号 */
				0x00, /* CRC校验码 */
				0x03 
		};	
		
		System.arraycopy(amount, 0, cmd, 3, 6);
		System.arraycopy(swn, 0, cmd, 9, 10);
		cmd[19] = genCRC(cmd);
		
		byte[] cost = worker.cost(cmd, timeout);
		if(cost == null) {
			return new String[] { DXYZFContext.FAIL };
		}
		
		try {
			byte status = cost[2];
			
			if(status == 0x00) { // 扣款成功 
				String[] costRet  = new String[6];
				String[] parseRet = parseCost(cost);
				
				costRet[0] = DXYZFContext.SUCCESS; // 状态码
				costRet[1] = parseRet[1]; // 卡号(手机号)
				costRet[2] = parseRet[2]; // 扣款金额
				costRet[3] = parseRet[3]; // POSID
				costRet[4] = parseRet[4]; // POS流水号
				costRet[5] = parseRet[5]; // 扣款时间
				
				return costRet;
			}
			
			if(status == 0x01) { // 未找到卡片或者手机
				Logger.warn("status=0x" + DeviceUtils.byte2HASCII(status));
				return new String[] { DXYZFContext.NO_PHONE };
			}
			
			if(status == 0x02) { // 扣款成功,保存流水失败
				Logger.warn("status=0x" + DeviceUtils.byte2HASCII(status));
				
				String[] costRet  = new String[6];
				String[] parseRet = parseCost(cost);
				
				costRet[0] = DXYZFContext.SUCCESS; // 状态码
				costRet[1] = parseRet[1]; // 卡号(手机号)
				costRet[2] = parseRet[2]; // 扣款金额
				costRet[3] = parseRet[3]; // POSID
				costRet[4] = parseRet[4]; // POS流水号
				costRet[5] = parseRet[5]; // 扣款时间
				
				return costRet;
			}
			
			if(status == 0x03) { // 余额不足
				Logger.info("status=0x" + DeviceUtils.byte2HASCII(status));
				return new String[] { DXYZFContext.BALANCE_LESS };
			}
			
			if(status == 0x04) { // 其他错误 
				Logger.warn("status=0x" + DeviceUtils.byte2HASCII(status));
				return new String[] { DXYZFContext.FAIL };
			}
			
			Logger.warn("Unkonw status=0x" + DeviceUtils.byte2HASCII(status));
			return new String[] { DXYZFContext.FAIL };
		} catch(Exception e) {
			Logger.error("cost fail", e);
			return new String[] { DXYZFContext.FAIL };
		}
	}
	
	/**
	 * 生成amount
	 * @param money 消费金额
	 * @return
	 */
	private byte[] genAmount(int money) {
		char[] cm = String.valueOf(money).toCharArray();
		char[] ca = new char[12];
		for(int i = 0; i < ca.length; i ++) {
			ca[i] = '0';
		}
		
		int minLen = Math.min(ca.length, cm.length);
		int mIndex = cm.length - 1;
		int aIndex = ca.length - 1; 
		for(int i = 0; i < minLen; i++) {
			ca[aIndex --] = cm[mIndex --];
		}
		
		return DeviceUtils.hASCII2ByteArray(new String(ca));
	}
	
	/**
	 * 生成系统流水号
	 * @param seq
	 * @return
	 */
	private byte[] genSwn(String seq) {
		char[] sm = seq.toCharArray();
		char[] wm = new char[20];
		for(int i = 0; i < wm.length; i++) {
			wm[i] = '0';
		}
		
		int minLen = Math.min(sm.length, wm.length);
		int sIndex = sm.length - 1;
		int wIndex = wm.length - 1;
		for(int i = 0; i < minLen; i++) {
			wm[wIndex --] = sm[sIndex --];
		}
		
		byte[] bm = new byte[] {
				Byte.parseByte(new String(new char[] {wm[0], wm[1]})),
				Byte.parseByte(new String(new char[] {wm[2], wm[3]})),
				Byte.parseByte(new String(new char[] {wm[4], wm[5]})),
				Byte.parseByte(new String(new char[] {wm[6], wm[7]})),
				Byte.parseByte(new String(new char[] {wm[8], wm[9]})),
				Byte.parseByte(new String(new char[] {wm[10], wm[11]})),
				Byte.parseByte(new String(new char[] {wm[12], wm[13]})),
				Byte.parseByte(new String(new char[] {wm[14], wm[15]})),
				Byte.parseByte(new String(new char[] {wm[16], wm[17]})),
				Byte.parseByte(new String(new char[] {wm[18], wm[19]})),
		};
		
		return bm;
	}
	
	/**
	 * 生成CRC
	 * @param cmd 命令
	 * @return CRC结果
	 */
	private byte genCRC(byte[] cmd) {
		byte orc = cmd[2]; // 校验起始位
		for(int idx = 3; idx < cmd.length - 2; idx ++) {
			orc ^= cmd[idx];
		}
		
		return orc;
	}
	
	/**
	 * 解析扣款应答单元
	 * @param cost
	 * @return [0]-status,[1]-CardNo,[2]-Amount,[3]-终端POS号,[4]-流水号,[5]-日期(yyyy-MM-dd HH:mm:ss)
	 */
	private String[] parseCost(byte[] cost) {
		byte   bStatus = cost[2];
		byte[] bCardNO = Arrays.copyOfRange(cost, 3, 13);
		byte[] bAmount = Arrays.copyOfRange(cost, 13, 19);
		byte[] bPOSID  = Arrays.copyOfRange(cost, 19, 27);
		byte[] bPOSSeq = Arrays.copyOfRange(cost, 27, 30);
		byte[] bDate   = Arrays.copyOfRange(cost, 30, 37);

		String sStatus = DeviceUtils.byte2HASCII(bStatus);
		String sCardNO = DeviceUtils.byteArray2HASCII(bCardNO);
		int iAmount    = Integer.parseInt(DeviceUtils.byteArray2HASCII(bAmount));
		String sPOSID  = new String(bPOSID);
		String sPOSSeq = String.valueOf(Integer.parseInt(DeviceUtils.byteArray2HASCII(bPOSSeq)));
		String sDate   = DeviceUtils.byteArray2HASCII(bDate);
		
		char[] csDate = sDate.toCharArray();
		String date   = new String(new char[] { csDate[0], csDate[1], csDate[2], csDate[3] })
						+ "-" + 
						new String(new char[] { csDate[4], csDate[5] })
		                + "-" + 
						new String(new char[] { csDate[6], csDate[7] })
						+ " " + 
						new String(new char[] { csDate[8], csDate[9] })
						+ ":" + 
						new String(new char[] { csDate[10], csDate[11] })
						+ ":" + 
						new String(new char[] { csDate[12], csDate[13]}); 
		
		Logger.info( "COST SUCCESS: " + "CardNO=" + sCardNO + ", Amount=" + iAmount + ", POSID=" + sPOSID + ", POSSeq=" + sPOSSeq + ", date=" + date );
		
		return new String[] { sStatus, sCardNO, String.valueOf(iAmount), sPOSID, sPOSSeq, date };
	}
}
