package com.ubox.card.core.context;

import com.ubox.card.bean.external.ExtRequest;
import com.ubox.card.util.logger.Logger;

import java.util.concurrent.ArrayBlockingQueue;

public class CmnMessageBuffer {

    /** 查询和扣款信息缓存 */
    private static final ArrayBlockingQueue<ExtRequest> workQue = new ArrayBlockingQueue<ExtRequest>(1);
    
    /**
     * 获取工作信息
     * @return 成功返回对象, 失败返回null
     */
    public static ExtRequest takeMsg() {
        try {
            return workQue.take();
        } catch (InterruptedException e) {
            Logger.error(">>>> takeWorkMsg ERROR.", e);
            return null;
        } 
    }
    
    /**
     * 添加工作信息
     * @param msg 工作信息
     * @return 添加成功返回true,添加失败返回false
     */
    public static boolean putMsg(ExtRequest msg) {
        try{


            workQue.put(msg);
            return true;
        } catch(Exception e) {
            Logger.error(">>>> putWorkMsg ERROR", e);
            return false;
        }
    }
    
}
