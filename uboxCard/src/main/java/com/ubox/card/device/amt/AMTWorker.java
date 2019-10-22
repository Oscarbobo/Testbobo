package com.ubox.card.device.amt;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.ubox.card.core.serial.IcCom;
import com.ubox.card.util.Utils;
import com.ubox.card.util.device.DeviceUtils;
import com.ubox.card.util.logger.Logger;

public class AMTWorker {
	
    private static final byte[] RE_CONFIRM  = { 0x02, 0x02, (byte)0xAA, (byte)0xA8, 0x03 };
    private static final byte[] RE_COST     = { 0x02, 0x04, (byte)0xD1, 0x00, 0x00, 0x00, 0x03 };
    private static final byte[] RE_COMPLETE = { 0x02, 0x02, 0x00, 0x02, 0x03 };
    private static final byte[] RE_CANCEL   = { 0x02, 0x02, (byte)0xD2, (byte)0xD0, 0x03 };
    
    private static final String COST_WRITE     = "Device cost     write : ";
    private static final String COST_READ      = "Device cost     read  : ";
    private static final String CONFIRM_READ   = "Device confirm  read  : ";
    private static final String COMPLETE_WRITE = "Device complete write : ";
    private static final String COMPLETE_READ  = "Device complete read  : ";
    private static final String CANCEL_WRITE   = "Device cancel   write : ";
    private static final String CANCEL_READ    = "Device cancel   read  : ";
	
	static final int SUCCESS = 0;
	static final int FAIL    = 1;
	static final int CANCEL  = 2;
	
	private IcCom com = new IcCom(9600,IcCom.DATABITS_8,IcCom.STOPBITS_1,IcCom.PARITY_NONE);
	
	/**
	 * 扣款
	 * <br>ps:澳门通设备有内置20s超时时间,且能够取消交易
	 * @param money 扣款金额,单位分
	 * @return 0-成功,非0-失败
	 */
	int cost(int money,String serNo) {
		try {
			com.open();

			// 发送消费命令
			int resultInt = deviceCost(consume(money/10),serNo);
			if(resultInt == SUCCESS) {
				// 消费成功,发送"完成消费命令"
				if(!deviceComplete(serNo)){ 
					Logger.warn("Cost deviceComplete fail");
				}
				
			}
			return resultInt;
		} catch(Exception e) {
			Logger.error("Cost error. "+e.getMessage(), e);
			return FAIL;
		} finally {
			com.close();
		}
	}
	
    /**
     * 设备完成消费
     * @return true-成功;false-失败
     * @throws IOException
     */
    boolean deviceComplete(String serNo) throws Exception {
    	try{
	        byte[] cmd = { 0x02, 0x02, (byte)0xDD, (byte)0xDF, 0x03 };
	        Logger.info(COMPLETE_WRITE + DeviceUtils.byteArray2HASCII(cmd));
	        com.getOutputStream().write(cmd);
	
	        if(!readConfirm(3000,serNo)){
	            return false;
	        }
	        
	        Map<String,Object> map = readIO(2000, true, serNo);
	        if(Integer.parseInt(map.get("code")+"")!=SUCCESS){
	        	return false;
	        }
	        byte[] copt = (byte[])map.get("data");
	        Logger.info(COMPLETE_READ + DeviceUtils.byteArray2HASCII(cmd));
	
	        if(copt.length != RE_COMPLETE.length){
	        	Logger.error("copt.length fail : " + copt.length+ ", RE_COMPLETE.length"+ RE_COMPLETE.length);
	        	return false;
	    	}
	        
	        for(int i = 0; i < RE_COMPLETE.length; ++i){
	            if(copt[i] != RE_COMPLETE[i]){
	            	Logger.error(serNo+" receive deviceComplete data content is error."+copt[i] +"!="+ RE_COMPLETE[i]);
	                return false;
	            }
	        }
	        return true;
	    } catch (Exception e) {
			Logger.error("Exception . "+e.getMessage(),e);
			return false;
		}
    }
	
    /**
     * 设备扣款
     * @param cmd 扣款命令
     * @return true-扣款成功;false-扣款失败
     * @throws IOException
     */
    int deviceCost(byte[] cmd,String serNo) {
    	try {
    		Logger.info(COST_WRITE + DeviceUtils.byteArray2HASCII(cmd));
            com.getOutputStream().write(cmd);

            if(!readConfirm(5000,serNo) ){
            	Logger.error("receive cost confirm is not success. ");
            	return FAIL;
            }
            
            Map<String,Object> map = readIO(25000, true, serNo);
            if(Integer.parseInt(map.get("code")+"")!=SUCCESS){
            	return Integer.parseInt(map.get("code")+"");
            }
            byte[] cost = (byte[])map.get("data");
            Logger.info(COST_READ + DeviceUtils.byteArray2HASCII(cost));

            /** 校验 "消费命令" */
            if(cost.length != RE_COST.length){
            	Logger.error("cost.length fail : " + cost.length+ ", RE_COST.length"+ RE_COST.length);
                return FAIL;
            }
            if(cost[2] != RE_COST[2]) {
                Logger.error("Cost fail, code=0x" + DeviceUtils.byte2HASCII(cost[2]));
                return FAIL;
            }
            
            if(cost[3] != cmd[3] || cost[4] != cmd[4]) { // 发送的扣款金额与反馈的实际扣款金额不符
                Logger.error("Cost error, money not equal.");
                if(!deviceComplete(serNo)){ 
                	Logger.warn("Cost deviceComplete fail");
                }
                return FAIL;
            }
            return SUCCESS;
		} catch (Exception e) {
			Logger.error("Exception . "+e.getMessage(),e);
			return FAIL;
		}
        
    }
	
    /**
     * 生成消费指令
     * @param money 消费金钱
     * @return 消费指令
     */
    byte[] consume(int money) {
        byte[] cmd = { 0x02, 0x04, (byte)0xA1, (byte)((money >> 8) & 0xff), (byte)(money & 0xff), 0x00, 0x03 };
        cmd[5]     = calcOXL(cmd);

        return cmd;
    }

    /**
     * 计算校验位
     * @param cmd 待校验命令
     * @return 校验位
     */
    byte calcOXL(byte[] cmd) {
        int start = 1;
        int end   = cmd.length - 2;

        byte oxl = cmd[start];
        for(int i = 2; i < end; ++i) {
            oxl ^= cmd[i] & 0xff;
        }

        return oxl;
    }
	
	/**
	 * 刷卡取消
	 * @return true-刷卡取消,false-刷卡正常进行
	 * @throws Exception 
	 */
	private boolean doCancel(String serNo) {
		Logger.info("doCancel nb is : " + serNo);
		try {
			if(Utils.isCancel(serNo) ==true) {
				if(true ==deviceCancel(serNo)) {
					Logger.info("Cancel Success. serNo:"+serNo);
					return true;
				}
			}
			Logger.info("Cancel Fail. serNo:"+serNo);
			return false;
		} catch(Exception e) {
			Logger.error("exception Cancel fail. serNo:"+serNo+". "+e.getMessage(),e);
			return false;
		}
	}
	
	/**
	 * 设备取消命令
	 * @return true-取消成功;false-取消失败
	 */
	private boolean deviceCancel(String serNo) throws Exception {
        byte[] cmd = { 0x02, 0x02, (byte)0xA2, 0x00, 0x03 };
        Logger.info(CANCEL_WRITE + DeviceUtils.byteArray2HASCII(cmd));
        com.getOutputStream().write(cmd);
        
        if(!readConfirm(3000,serNo)){
        	return false;
        }
        
        Map<String,Object> map = readIO(1000,false,serNo);
        if(Integer.parseInt(map.get("code")+"")!=SUCCESS){
        	return false;
        }
        byte[] cancel = (byte[])map.get("data");
        Logger.info(CANCEL_READ + DeviceUtils.byteArray2HASCII(cancel));
        
        /** 校验 "取消命令" */
        if(cancel.length != RE_CANCEL.length){
        	Logger.error(serNo+" receive cancel data length is error."+cancel.length +"!="+ RE_CANCEL.length);
            return false;
        }
        
        for(int i = 0; i < RE_CANCEL.length; ++i){
            if(cancel[i] != RE_CANCEL[i]){
            	Logger.error(serNo+" receive cancel data content is error."+cancel[i] +"!="+ RE_CANCEL[i]);
                return false;
            }
        }
		
		return true;
	}
	
	private boolean readConfirm(int timeout,String serNo) throws Exception {
        Map<String,Object> map = readIO(1000,false,serNo);
        if(Integer.parseInt(map.get("code")+"")!=SUCCESS){
        	return false;
        }
        byte[] confirm = (byte[])map.get("data");
        Logger.info(CONFIRM_READ + DeviceUtils.byteArray2HASCII(confirm));

        /** 校验 "确认命令" **/
        if(confirm.length != RE_CONFIRM.length) {
        	Logger.error(serNo+" receive confirm data length is error."+confirm.length +"!="+ RE_CONFIRM.length);
            return false;
        }
        
        for(int i = 0; i < RE_CONFIRM.length; ++i) {
            if (confirm[i] != RE_CONFIRM[i]){
            	Logger.error(serNo+" receive confirm data content is error."+confirm[i] +"!="+ RE_CONFIRM[i]);
            	return false;
            }
        }
        
        return true;
	}
	
	/**
	 * 读取串口数据
	 * @param timeout    超时限制,单位ms
	 * @param needCancel 是否需要"取消功能"
	 * @return []-接收失败;非[]-接收成功
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	private Map<String,Object> readIO(int timeout,boolean isCancel,String serNo) {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("code", FAIL);
		map.put("data", new byte[]{});
		try {
			InputStream mIn = com.getInputStream();

	        int timeUsed = 0, step = 100;
	        while(mIn.available() <= 0) {
	            Thread.sleep(step);
	            timeUsed += step;

	            if(timeUsed > timeout) {
	                Logger.error("Read time out.");
	                return map;
	            }
	            if(true== isCancel && doCancel(serNo)){
	        		Logger.error("Read cancel.");
	        		map.put("code", CANCEL);
	        		return map;
	            }

	        }
	        
	        int    stx = mIn.read();
	        int    len = mIn.read();
	        byte[] ret = new byte[len + 3];

	        ret[0] = (byte)(stx & 0xff);
	        ret[1] = (byte)(len & 0xff);

	        for(int i = 0; i < len; ++i){
	            ret[i + 2] = (byte)(mIn.read() & 0xff);
	        }

	        ret[len + 2] = (byte)(mIn.read() & 0xff);

	        map.put("data", ret);
	        map.put("code", SUCCESS);
	        return map;
		} catch (Exception e) {
			Logger.error("Exception "+e.getMessage(),e);
			map.put("code", FAIL);
			return map;
		}
	}
	
}
