package com.ubox.card.device.ynylsf.compos;


import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.ubox.card.core.serial.RS232Worker;
import com.ubox.card.device.ynylsf.rs232.YNYLSFSerial;
import com.ubox.card.device.ynylsf.ynfutils.YNfuUtil;
import com.ubox.card.util.logger.Logger;

public class BusyPass {

    public static final String SUCCESS    = "00";
    public static final String FAIL       = "01";
    public static final String TEST_FAIL  = "02";
    public static final String CHECK_FAIL = "03";
    public static final String ACK_NAK    = "04";
    
    private static final int TIME_OUT     = 20000; // 超时时间

    private static final YNYLSFSerial rs232 = new YNYLSFSerial(9600, RS232Worker.DATABITS_8, RS232Worker.STOPBITS_1, RS232Worker.PARITY_NONE);
    /**
     * 余额查询实现
     *
     * @return 余额查询返回: [0]-解析状态,[1]-商户代码,[2]-终端号,[3]-卡号,[4]-卡余额,[5]-交易流水号,[6]-交易批次号
     */
    public static String[] query() {
        if(!test()) {
        	return new String[] { TEST_FAIL };
        }
        return analysisPrint(ComClass.genBalanceInquiry(), 31000); // 查询等待时间31s
    }

    /**
     * 扣款
     *
     * @param money 扣款金额,单位:分
     * @return 扣款返回: [0]-解析状态,[1]-商户代码,[2]-终端号,[3]-卡号,[4]-卡余额,[5]-交易流水号,[6]-交易授权号
     */
    public static String[] cost(int money) {
        if(!test()) {
        	return new String[] { TEST_FAIL };
        }

        char[] cm  = new char[] { '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0' };
        char[] scm = String.valueOf(money).toCharArray();
        YNfuUtil.backCover(scm, cm);

        return analysisPrint(ComClass.genConsume(new String(cm).getBytes()), 20000); // 消费等待时间02
    }
    
    /**
     * 离线扣款
     * @param money 扣钱金额,单位:分
     * @return [0]-解析状态, [1]-商户代码, [2]-终端号, [3]-卡号, [4]-交易金额, [5]-交易流水号, [6]-脱机上送交易记录, [7]-交易日期时间
     */
    public static String[] consumeOffline(int money) {
        char[] cm  = new char[] { '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0' };
        char[] scm = String.valueOf(money).toCharArray();
        YNfuUtil.backCover(scm, cm);
        
        byte[] consume = ComClass.genConsume(new String(cm).getBytes());
        byte[] resp    = rs232wr(consume, TIME_OUT); // 脱机消费        
        
        /* * * * * 解析POS响应数据 * * * */
        if(0 != ComClass.checkPkt(resp)) {
			Logger.warn(">>>>ERROR: Check LRC fail");
            return new String[] { CHECK_FAIL };
        }

        byte[][] contDomain = ComClass.splitOffline(ComClass.isolatedCont(resp));

        if(contDomain[0][0] == ComClass.NAK) {
            analysisANK(contDomain);
            return new String[] { ACK_NAK };
        } else if(contDomain[0][0] == ComClass.ACK) {
            byte[] record = contDomain[3];
            Logger.info("Offline record: " + YNfuUtil.BA2HS(record));

            byte[] posId   = Arrays.copyOfRange(record, 2, 10);
            byte[] mchntId = Arrays.copyOfRange(record, 10, 25);
            byte[] posSeq  = Arrays.copyOfRange(record, 25, 28);
            byte[] time    = Arrays.copyOfRange(record, 28, 31);
            byte[] date    = Arrays.copyOfRange(record, 31, 33);

            int len = Integer.valueOf(YNfuUtil.byte2HASCII(record[49]));
            byte[] account = Arrays.copyOfRange(record, 50, 60);

            int idx = 50 + ((len + 1) / 2);
            byte[] amount = Arrays.copyOfRange(record, idx + 3, idx + 9);

            Logger.info("Offline info: " +
                        "posId   = " + YNfuUtil.BA2HS(posId)   + "," +
                        "mchntId = " + YNfuUtil.BA2HS(mchntId) + "," +
                        "posSeq  = " + YNfuUtil.BA2HS(posSeq)  + "," +
                        "account = " + YNfuUtil.BA2HS(account) + "," +
                        "amount  = " + YNfuUtil.BA2HS(amount)  + "," +
                        "time    = " + YNfuUtil.BA2HS(time)    + "," +
                        "date    = " + YNfuUtil.BA2HS(date)
            );

            String[] ret = new String[9];
            ret[0] = SUCCESS;
            ret[1] = new String(mchntId);
            ret[2] = new String(posId);
            ret[3] = YNfuUtil.BA2HS(account).substring(0, len);
            ret[4] = String.valueOf(Integer.valueOf(YNfuUtil.BA2HS(amount)));
            ret[5] = String.valueOf(Integer.valueOf(YNfuUtil.BA2HS(posSeq)));
            ret[6] = YNfuUtil.BA2HS(record);
            ret[7] = String.valueOf(Integer.valueOf(YNfuUtil.BA2HS(date))) + String.valueOf(Integer.valueOf(YNfuUtil.BA2HS(time)));

            return ret;
        } else {
            Logger.error(">>>>ERROR: Unknow CODE. CODE=" + YNfuUtil.BA2HS(contDomain[0]) + "h");
        }

        return null;
    }

    /**
     * 结算
     *
     * @return 结算结果
     */
    public static String[] settle() {
        if(!test()) {
        	return new String[] { TEST_FAIL };
        }

        try {
            byte[] settleRec = rs232wr(ComClass.genSettle(), 20 * 60 * 1000); // 结算等待时间20分钟
            if(0 != ComClass.checkPkt(settleRec)) {
                Logger.warn(">>>>ERROR: Check LRC fail");
                return new String[] { CHECK_FAIL };
            }

            byte[][] contDomain = ComClass.splitCont(ComClass.isolatedCont(settleRec));
            if(contDomain[0][0] == ComClass.NAK) {
                analysisANK(contDomain);
                if("X1".equals(new String(contDomain[1]))) {
                    Logger.warn(">>>>WARN: No transactions");
                    return new String[] { SUCCESS };
                }

                return new String[] { ACK_NAK };
            } else if(contDomain[0][0] == ComClass.ACK) {
                Analysisor.analysisSettle(contDomain);

                String[] success = new String[5];
                success[0] = SUCCESS;
                success[1] = new String(contDomain[4]);  // 商户代码
                success[2] = new String(contDomain[5]);  // 终端号
                success[3] = new String(contDomain[8]);  // 交易批次号
                success[4] = new String(contDomain[9]);  // 交易日期和时间

                return success;
            } else {
                Logger.warn(">>>>ERROR: Unknow CODE. CODE=" + YNfuUtil.BA2HS(contDomain[0]) + "h");
            }
        } catch (Exception e) {
            Logger.error(">>>>ERROR: settle fail.", e);
        } finally {
            signIn(); // 设备结算之后进签到
        }

        return new String[] { FAIL };
    }

    /**
     * 设备进行签到
     */
    private static void signIn() {
        Logger.info(">>>> SIGN_IN START");
        test();
        byte[] sign = rs232wr(ComClass.genSign(), 70 * 1000); // 签到时间70s
        if(0 != ComClass.checkPkt(sign)) {
            Logger.warn(">>>>ERROR: Check LRC fail"); }
        else {
            byte[][] signtDomain = ComClass.splitCont(ComClass.isolatedCont(sign));
            analysisANK(signtDomain);
        }

        Logger.info(">>>> SIGN_IN OVER");
    }

    /**
     * 连接测试
     *
     * @return true-连接成功,false-连接失败
     */
    private static boolean test() {
        byte[] testRec = rs232wr(ComClass.genTEST(), 5000);
        if(0 != ComClass.checkPkt(testRec)) {
            Logger.warn(">>>>ERROR: Check LRC fail");
            return false;
        }

        byte[][] contDomain = ComClass.splitCont(ComClass.isolatedCont(testRec));
        analysisANK(contDomain);

        return (contDomain[0][0] == ComClass.ACK);
    }

    /**
     * 解析ACK或者NAK信息
     *
     * @param domain ACK或者NAK数据域
     */
    private static void analysisANK(byte[][] domain) {
        byte[] cmd  = domain[0]; // 指令代码
        byte[] code = domain[1]; // 代码
        byte[] msg  = domain[2]; // 提示信息

        try {
            Logger.info(
                    ">>>> ACk_NAK <<<< \n" +
                    ">>>> cmd  = " + YNfuUtil.BA2HS(cmd) + "\n" +
                    ">>>> code = " + new String(code) + "\n" +
                    ">>>> msg  = " + new String(msg, "GBK")
            );
        } catch (UnsupportedEncodingException e) { Logger.warn(">>>>FAIL: ANALYSIS ACK_NAK ERROR"); }
    }

    /**
     * 解析打印信息指令
     *
     * @param send 发送的指令信息
     * @param timeout 超时时间
     * @return 解析结果: [0]-解析状态,[1]-商户代码,[2]-终端号,[3]-卡号,[4]-卡余额,[5]-交易凭证号,[6]-交易批次号
     */
    private static String[] analysisPrint(byte[] send, int timeout) {
        byte[] receive = rs232wr(send, timeout);
        
        if(0 != ComClass.checkPkt(receive)) {
            Logger.warn(">>>>ERROR: Check LRC fail");
            return new String[] { CHECK_FAIL };
        }

        byte[][] contDomain = ComClass.splitCont(ComClass.isolatedCont(receive));
        
        if(contDomain[0][0] == ComClass.NAK) {
            analysisANK(contDomain);
            return new String[] { ACK_NAK };
        } else if(contDomain[0][0] == ComClass.ACK) {
            String[] success = new String[7];
            
            success[0] = SUCCESS;
            success[1] = new String(contDomain[4]);  // 商户代码
            success[2] = new String(contDomain[5]);  // 终端号
            success[3] = new String(contDomain[11]); // 卡号
            success[4] = new String(contDomain[33]); // 持卡人金额
            success[5] = new String(contDomain[16]); // 交易凭证号
            success[6] = new String(contDomain[35]); // 交易批次号

            return success;
        } else {
            Logger.warn(">>>>ERROR: Unknow CODE. CODE=" + YNfuUtil.BA2HS(contDomain[0]) + "h");
        }

        return new String[] { FAIL };
    }

    /**
     * 串口的数据发送和接收
     *
     * @param w 发送的数据
     * @param timeout 接收等待超时时间,单位:ms
     * @return 接收的数据,null表示数据接收失败
     */
    public static byte[] rs232wr(byte[] w, int timeout) {
        if(0 != rs232.open())  {
        	return null;
        }
        
        if(0 != rs232.write(w)) {
        	return null;
        }
        
        byte[] r = rs232.read(timeout);
        
        rs232.close();
        
        return r;
    }
}
