package com.ubox.card.device.bjszykt.pubwork;

import java.util.HashMap;

import com.ubox.card.device.bjszykt.recharge.RechargeInfo;
import com.ubox.card.device.bjszykt.server.bean.KCQTradeResponse;
import com.ubox.card.util.Utils;
import com.ubox.card.util.logger.Logger;

public class PubWorker {
    public static String batchNB;
    /**
     * 工作休眠
     *
     * @param mills 休眠时间,单位ms
     */
    public static void workSleep(int mills) {
        try {
            Thread.sleep(mills);
        } catch(InterruptedException e) {
            Logger.warn(">>>>WARN: sleep interrupted");
        }
    }

    /**
     * 解析"签到指令反馈"
     *
     * @param backBts 签到指令反馈数据
     */
    public static void parseSignReply(byte[] backBts) {
        LocalContext.CACHE_SAM = new String(backBts, 9, 12);      // SAM卡号
        LocalContext.CACHE_RANDOM = new String(backBts, 21, 16);  // 随机数(广义BCD码,取值范围0~F)

        Logger.info("Sign Reply: sam=" + LocalContext.CACHE_SAM + ", Random=" + LocalContext.CACHE_RANDOM);
    }

    /**
     * 解析"读版本指令"
     * @param bts 读版本指令反馈数据
     */
    public static void parseVersion(byte[] bts) {
        String version = new String(bts, 9, 34);
        String SAM     = new String(bts, 43, 12);
        LocalContext.CACHE_SAM = SAM;      // SAM卡号

        Logger.info("readVersion: version=" + version + ", sam=" + LocalContext.CACHE_SAM);
    }

    /**
     * 解析"签到申请应答"
     *
     * @param packetMessage 签到申请应答反馈数据
     */
    public static void parseSignApplyReplay(byte[] packetMessage) {

        /** 解析签到申请应答报文 */
        byte[] sysDatetime  = new byte[7];
        byte[] ISAM         = new byte[6];
        int    programName;
        int    responseCode;
        byte[] encText      = new byte[8];
        byte[] limitTime    = new byte[7];
        byte[] batchNO      = new byte[4];
        byte[] posIcSeq     = new byte[4];
        byte[] posAccSeq    = new byte[4];
        byte[] posCommSeq   = new byte[4];
        byte[] macBuf       = new byte[128];

        System.arraycopy(packetMessage, 8 , sysDatetime,  0,   7);// 系统时间yyyyMMddHHmmss
        System.arraycopy(packetMessage, 15, ISAM,         0,   6);// SAM卡号
        programName     = packetMessage[21];                      // 机具程序代码
        responseCode    = packetMessage[22];                      // 交易应答码
        System.arraycopy(packetMessage, 23, encText,      0,   8);// SAM卡随机数产生的密文
        System.arraycopy(packetMessage, 31, limitTime,    0,   7);// 授权截至时间
        System.arraycopy(packetMessage, 38, batchNO,      0,   4);// 批次号
        System.arraycopy(packetMessage, 42, posIcSeq,     0,   4);// 终端IC交易流水号
        System.arraycopy(packetMessage, 46, posAccSeq,    0,   4);// 终端账户交易流水号
        System.arraycopy(packetMessage, 50, posCommSeq,   0,   4);// 通讯流水号
        System.arraycopy(packetMessage, 54, macBuf,       0, 128);// 工作密钥密文

        batchNB = PubUtils.BA2HS(batchNO);

        Logger.info(
                "\n>>>> Analysis Sign Applay Replay <<<<" +
                        "\n>>>> SysDatetime:     " + PubUtils.BA2HS(sysDatetime) +
                        "\n>>>> ISAM:            " + PubUtils.BA2HS(ISAM) +
                        "\n>>>> ProgramName:     " + PubUtils.BA2HS(new byte[]{(byte) programName}) +
                        "\n>>>> responseCode:    " + PubUtils.BA2HS(new byte[]{(byte) responseCode}) +
                        "\n>>>> EncText:         " + PubUtils.BA2HS(encText) +
                        "\n>>>> LimitTime:       " + PubUtils.BA2HS(limitTime) +
                        "\n>>>> batchNO:         " + PubUtils.b2iLt(batchNO, 4) +
                        "\n>>>> PosIcSeq:        " + PubUtils.b2iLt(posIcSeq, 4) +
                        "\n>>>> PosAccSeq:       " + PubUtils.b2iLt(posAccSeq, 4) +
                        "\n>>>> PosCommSeq:      " + PubUtils.b2iLt(posCommSeq, 4) +
                        "\n>>>> maxBuf:          " + PubUtils.BA2HS(macBuf)
        );

        /** 缓存签到申请应答数据*/
        LocalContext.CACHE_ENC_TEXT      = PubUtils.BA2HS(encText);
        LocalContext.CACHE_LIMIT_TIME    = PubUtils.BA2HS(limitTime);
        LocalContext.CACHE_BATCH_NO      = String.valueOf(PubUtils.b2iLt(batchNO, 4));
        LocalContext.CACHE_POS_IC_SEQ    = String.valueOf(PubUtils.b2iLt(posIcSeq, 4));
        LocalContext.CACHE_POS_ACC_SEQ   = String.valueOf(PubUtils.b2iLt(posAccSeq, 4));
        LocalContext.CACHE_POS_COMM_SEQ  = String.valueOf(PubUtils.b2iLt(posCommSeq, 4));
        LocalContext.CACHE_MAC_BUF       = PubUtils.BA2HS(macBuf);
        // 提供给充值申请应答
        RechargeInfo.CACHE_POS_COMM_SEQ  = posCommSeq;

        /** LimitTime和BatchNO本地持久化 */
        String perLT = PubUtils.BA2HS(limitTime);
        String perBN = String.valueOf(PubUtils.b2iLt(batchNO, 4));

        HashMap<String, String> kvs = new HashMap<String, String>();
        kvs.put("LimitTime"    , perLT);
        kvs.put("batchNO"      , perBN);
        kvs.put("POS_IC_SEQ"   , PubUtils.BA2HS(posIcSeq));

        if(PubUtils.configsPerisit(kvs) == 0) {
            Logger.info(
                    "\n>>>> SUCCESS:Persist LimitTime + batchNO success <<<<" +
                            "\n>>>> LimitTime    :" + perLT +
                            "\n>>>> batchNO      :" + perBN  +
                            "\n>>>> POS_IC_SEQ   :" + LocalContext.CACHE_POS_IC_SEQ
            );
        }else {
            Logger.warn(">>>> FAIL: Persist LmitTime + batchNO + POS_IC_SEQ");
        }

    }


    /**
     * 解析"订单查询应答"
     *
     * @param packetMessage 订单查询应答反馈数据
     */
    public static void parseRechargeSearchReplay(byte[] packetMessage, KCQTradeResponse bean) {

        try {

            // 解析充值申请应答报文
//            byte[] messageType = new byte[2];
            int ver = 0;
            byte[] sysDatetime  = new byte[7];
            byte[] OprId  = new byte[3];
            byte[] POSID  = new byte[20];
            byte[] ISAM         = new byte[6];
            byte[] batchNO      = new byte[4];
            int responseCode;
            byte[] CardNo     = new byte[8];
            byte[] OrderNum    = new byte[2];

            // orderInfo数组中的数据
            byte[] OrderNo = new byte[4];
            byte[] OrderSaveAmt = new byte[4];
            byte[] OrderDate = new byte[4];
            byte[] OrderTime = new byte[3];

            ver = packetMessage[7];
            System.arraycopy(packetMessage, 8 , sysDatetime,  0,   7);// 系统时间yyyyMMddHHmmss
            System.arraycopy(packetMessage, 15, OprId,        0,   3);// 操作员代码
            System.arraycopy(packetMessage, 18, POSID,        0,   20);// pos机id
            System.arraycopy(packetMessage, 38, ISAM,         0,   6);// SAM卡号
            System.arraycopy(packetMessage, 44, batchNO,      0,   4);// 批次号
            responseCode = packetMessage[48];
            // 这里判断应答码是否为0
            if (0 != responseCode) {
                bean.setCode(500);
                bean.setMsg("订单查询失败");
            }
            System.arraycopy(packetMessage, 49, CardNo,       0,   8);// 号卡
            System.arraycopy(packetMessage, 57, OrderNum,     0,   2);// 快充券数量

            int iOrderNum = PubUtils.b2iLt(OrderNum, 2);
            byte[] OrderInfo    = new byte[15 * iOrderNum];
            System.arraycopy(packetMessage, 59, OrderInfo,    0,   15 * iOrderNum);// 快充券信息循环
//        System.arraycopy(packetMessage, 59, OrderInfoTest,0, 15 * iOrderNum);// 快充券信息循环

            Logger.info(
                    "\n>>>> parseRechargeSearchReplay <<<<" +
                            "\n>>>> ver:             " + PubUtils.BA2HS(new byte[]{(byte) ver}) +
                            "\n>>>> SysDatetime:     " + PubUtils.BA2HS(sysDatetime) +
                            "\n>>>> oprId:           " + PubUtils.BA2HS(OprId) +
                            "\n>>>> posId:           " + PubUtils.BA2HS(POSID) +
                            "\n>>>> ISAM:            " + PubUtils.BA2HS(ISAM) +
                            "\n>>>> batchNO:         " + PubUtils.b2iLt(batchNO, 4) +
                            "\n>>>> responseCode:    " + responseCode +
                            "\n>>>> cardNo:          " + PubUtils.BA2HS(CardNo) +
                            "\n>>>> OrderNum:        " + iOrderNum +
                            "\n>>>> OrderInfo:       " + PubUtils.BA2HS(OrderInfo)
            );

            System.arraycopy(OrderInfo, 0, OrderNo, 0, 4);
            System.arraycopy(OrderInfo, 4, OrderSaveAmt, 0, 4);
            System.arraycopy(OrderInfo, 8, OrderDate, 0, 4);
            System.arraycopy(OrderInfo, 12, OrderTime, 0, 3);
            Logger.info(
                    "\n>>>> OrderInfo <<<<" +
                            "\n>>>> OrderNo:             " + PubUtils.BA2HS(OrderNo) +
                            "\n>>>> OrderSaveAmt:        " + PubUtils.b2iLt(OrderSaveAmt, 4) +
                            "\n>>>> OrderDate:           " + PubUtils.BA2HS(OrderDate) +
                            "\n>>>> OrderTime:           " + PubUtils.BA2HS(OrderTime)
            );

            /** 缓存充值申请应答数据*/
            RechargeInfo.CACHE_OPRID        = String.valueOf(PubUtils.BA2HS(OprId));
            RechargeInfo.CACHE_POSID        = String.valueOf(PubUtils.BA2HS(POSID));
            RechargeInfo.CACHE_SAM          = String.valueOf(PubUtils.BA2HS(ISAM));
            RechargeInfo.CACHE_BATCHNO      = String.valueOf(PubUtils.b2iLt(batchNO, 4));
            RechargeInfo.CACHE_CARDNO       = String.valueOf(PubUtils.BA2HS(CardNo));
            RechargeInfo.CACHE_ORDERNUM     = String.valueOf(iOrderNum);
            RechargeInfo.CACHE_ORDERINFO    = String.valueOf(PubUtils.BA2HS(OrderInfo));

            // 数据结构内的数据
            RechargeInfo.CACHE_ORDERNO      = String.valueOf(PubUtils.b2iLt(OrderNo, 4));
            RechargeInfo.CACHE_ORDERSAVEAMT = String.valueOf(PubUtils.b2iLt(OrderSaveAmt , 4));
            RechargeInfo.CACHE_ORDERDATE    = String.valueOf(PubUtils.BA2HS(OrderDate));
            RechargeInfo.CACHE_ORDERTIME    = String.valueOf(PubUtils.BA2HS(OrderTime));
            // 订单号和订单金额传给下一个操作
            RechargeInfo.CACHE_BYTE_ORDERNO = OrderNo;
            RechargeInfo.CACHE_BYTE_ORDER_SAVE_AMT = OrderSaveAmt;
        }catch (Exception e) {
            Logger.error(e.getMessage(), e);
        }

    }


    /**
     * 解析"充值申请应答"
     *
     * @param packetMessage 充值申请应答反馈数据
     */
    public static void parseRechargeApplyReplay(byte[] packetMessage) {

        try {

            // 解析充值申请应答报文
            byte[] sysDatetime  = new byte[7];
            byte[] OprId  = new byte[3];
            byte[] POSID  = new byte[20];
            byte[] ISAM         = new byte[6];
            byte[] batchNO      = new byte[4];

            // 拿到密文后通过模块指令获得明文
            byte[] CipherDataMAC     = new byte[4];
            byte[] CipherDataLen    = new byte[2];

            System.arraycopy(packetMessage, 8 , sysDatetime,  0,   7);// 系统时间yyyyMMddHHmmss
            System.arraycopy(packetMessage, 15, OprId,        0,   3);// 操作员代码
            System.arraycopy(packetMessage, 18, POSID,        0,   20);// pos机id
            System.arraycopy(packetMessage, 38, ISAM,         0,   6);// SAM卡号
            System.arraycopy(packetMessage, 44, batchNO,      0,   4);// 批次号

            System.arraycopy(packetMessage, 48, CipherDataMAC,0,   4);// 密文mac
            System.arraycopy(packetMessage, 52, CipherDataLen,0,   2);// 密文长度
            int len = PubUtils.b2iLt(CipherDataLen, 2);
            byte[] MacBuf    = new byte[len];
            System.arraycopy(packetMessage, 54, MacBuf,       0,   len);// 密文

            Logger.info(
                    "\n>>>> Analysis parseRechargeApplyReplay <<<<" +
                            "\n>>>> SysDatetime:     " + PubUtils.BA2HS(sysDatetime) +
                            "\n>>>> oprId:           " + PubUtils.BA2HS(OprId) +
                            "\n>>>> posId:           " + PubUtils.BA2HS(POSID) +
                            "\n>>>> ISAM:            " + PubUtils.b2iLt(ISAM, 6) +
                            "\n>>>> batchNO:         " + PubUtils.b2iLt(batchNO, 4) +
                            "\n>>>> CipherDataMAC:   " + PubUtils.BA2HS(CipherDataMAC) +
                            "\n>>>> CipherDataLen:   " + len +
                            "\n>>>> MacBuf:          " + PubUtils.BA2HS(MacBuf)
            );

            /** 缓存充值申请应答数据*/
            RechargeInfo.CACHE_OPRID           = String.valueOf(PubUtils.BA2HS(OprId));
            RechargeInfo.CACHE_POSID           = String.valueOf(PubUtils.BA2HS(POSID));
            RechargeInfo.CACHE_SAM             = String.valueOf(PubUtils.b2iLt(ISAM, 6));
            RechargeInfo.CACHE_BATCHNO         = String.valueOf(PubUtils.b2iLt(batchNO, 4));
            RechargeInfo.CACHE_CIPHER_DATA_MAC = Utils.toHex(CipherDataMAC);
            RechargeInfo.CACHE_CIPHER_DATA_LEN = String.valueOf(len);
            RechargeInfo.CACHE_MACBUF          = Utils.toHex(MacBuf);
        }catch (Exception e) {
            Logger.error(e.getMessage(), e);
        }
    }

    /**
     * 解析"订单充值交易确认应答"
     *
     * @param packetMessage 订单充值交易确认响应数据
     */
    public static void parseRechargeApplyConfirm(byte[] packetMessage) {
        try{
//          //解析充值申请应答报文
            byte[] sysDatetime  = new byte[7];
            byte[] OprId  = new byte[3];
            byte[] POSID  = new byte[20];
            byte[] ISAM         = new byte[6];
            byte[] CipherDataMAC  = new byte[4];
            byte[] CipherDataLen  = new byte[2];
            byte[] MacBuf     = new byte[16];
            byte[] OrderNo    = new byte[4];

            System.arraycopy(packetMessage, 8 , sysDatetime,  0,   7);// 系统时间yyyyMMddHHmmss
            System.arraycopy(packetMessage, 15, OprId,        0,   3);// 操作员代码
            System.arraycopy(packetMessage, 18, POSID,        0,   20);//pos机id
            System.arraycopy(packetMessage, 38, ISAM,         0,   6);// SAM卡号
            System.arraycopy(packetMessage, 44, CipherDataMAC,0,   4);// 密文MAC
            System.arraycopy(packetMessage, 48, CipherDataLen,0,   2);// 密文长度
            System.arraycopy(packetMessage, 50, MacBuf,	  	  0,   16);//密文
            System.arraycopy(packetMessage, 66, OrderNo, 	  0,   4);// 订单号

            Logger.info(
                    "\n>>>> Analysis Sign Applay Replay <<<<" +
                            "\n>>>> SysDatetime:     " + PubUtils.BA2HS(sysDatetime) +
                            "\n>>>> OprId:           " + PubUtils.BA2HS(OprId) +
                            "\n>>>> POSID:           " + PubUtils.BA2HS(POSID) +
                            "\n>>>> ISAM:            " + PubUtils.BA2HS(ISAM) +
                            "\n>>>> MAC:         	 " + PubUtils.BA2HS(CipherDataMAC) +
                            "\n>>>> LEN:          	 " + PubUtils.BA2HS(CipherDataLen) +
                            "\n>>>> MacBuf:       	 " + PubUtils.BA2HS(MacBuf) +
                            "\n>>>> OrderNo:     	 " + PubUtils.BA2HS(OrderNo)
            );

            /** 缓存订单充值交易确认应答*/
            RechargeInfo.CONFIRM_TIME       = PubUtils.BA2HS(OprId);
            RechargeInfo.CONFIRM_OPRID      = PubUtils.BA2HS(OprId);
            RechargeInfo.CONFIRM_POSID      = PubUtils.BA2HS(POSID);
            RechargeInfo.CONFIRM_SAM        = PubUtils.BA2HS(ISAM);
            RechargeInfo.CONFIRM_LEN      	= PubUtils.BA2HS(CipherDataLen);
            RechargeInfo.CONFIRM_ORDERNO    = PubUtils.BA2HS(OrderNo);
            RechargeInfo.CONFIRM_MAC      	= Utils.toHex(CipherDataMAC);
            RechargeInfo.CONFIRM_MACBUF     = Utils.toHex(MacBuf);
        }catch(Exception e){
            Logger.error(e.getMessage(),e);
        }


    }

    /**
     * 解析参数下载应答
     *
     * @param fdBytes 应答数据
     */
    public static void analyis5036(byte[] fdBytes) {
        byte[]  messagetype     = new byte[] {fdBytes[5], fdBytes[6]};
        int     ver             = fdBytes[7];
        byte[]  sysDatetime     = new byte[7];
        byte[]  posId           = new byte[20];
        byte[]  ISAM            = new byte[6];
        byte[]  paramId         = new byte[] {fdBytes[41], fdBytes[42], fdBytes[43], fdBytes[44]};
        int     responseCode    = fdBytes[45];
        byte[]  recordNum       = new byte[] {fdBytes[46], fdBytes[47], fdBytes[48], fdBytes[49]};
        byte[]  validData       = new byte[] {fdBytes[50], fdBytes[51], fdBytes[52], fdBytes[53]};

        System.arraycopy(fdBytes, 8,  sysDatetime,  0, 7);
        System.arraycopy(fdBytes, 15, posId,        0, 20);
        System.arraycopy(fdBytes, 35, ISAM,         0, 6);


        Logger.info(
                "\n>>>> Analysis ParamsDown Response <<<<" +
                        "\n>>>> MessageType:     " + PubUtils.BA2HS(messagetype) +
                        "\n>>>> Ver:             " + ver +
                        "\n>>>> SysDateTime:     " + PubUtils.BA2HS(sysDatetime) +
                        "\n>>>> PosId:           " + new String(posId) +
                        "\n>>>> ISAM:            " + PubUtils.BA2HS(ISAM) +
                        "\n>>>> ParamId:         0x" + PubUtils.BA2HS(paramId) +
                        "\n>>>> responseCode:    0x" + PubUtils.BA2HS(new byte[]{(byte) responseCode}) +
                        "\n>>>> RecordNum:       0x" + PubUtils.BA2HS(recordNum) + " = " + PubUtils.b2iLt(recordNum, 4) +
                        "\n>>>> ValidData:       " + PubUtils.BA2HS(validData)
        );
    }

    /**
     * 解析交易数据上送请求应答
     *
     * @param fdBytes 请求应答数据
     */
    public static void analyis5038(byte[] fdBytes) {
        byte[]  messagetype     = new byte[] {fdBytes[5], fdBytes[6]};
        int     ver             = fdBytes[7];
        byte[]  sysDatetime     = new byte[7];
        byte[]  messageID       = new byte[34];
        byte[]  posId           = new byte[20];
        byte[]  ISAM            = new byte[6];
        byte[]  messageLen      = new byte[4];
        int responseCode        = fdBytes[79];

        System.arraycopy(fdBytes,   8,      sysDatetime,     0,      7);
        System.arraycopy(fdBytes,   15,     messageID,       0,      34);
        System.arraycopy(fdBytes,   49,     posId,           0,      20);
        System.arraycopy(fdBytes,   69,     ISAM,            0,      6);
        System.arraycopy(fdBytes,   75,     messageLen,      0,      4);

        Logger.info(
                "\n>>>> Analysis data upload Response <<<<" +
                        "\n>>>> MessageType      :" + PubUtils.BA2HS(messagetype) +
                        "\n>>>> Ver              :" + ver +
                        "\n>>>> SysDatetime      :" + PubUtils.BA2HS(sysDatetime) +
                        "\n>>>> MessageID        :" + new String(messageID) +
                        "\n>>>> PosId            :" + new String(posId) +
                        "\n>>>> ISAM             :" + PubUtils.BA2HS(ISAM) +
                        "\n>>>> MessageLen       :" + PubUtils.BA2HS(messageLen) + "(HEX)=" + PubUtils.b2iLt(messageLen, 4) + "(DEC)" +
                        "\n>>>> responseCode     :0x" + PubUtils.BA2HS(new byte[]{(byte) responseCode})
        );
    }

    /**
     * 解析签退应答
     *
     * @param fdBytes 签退应答数据
     */
    public static void parseSignOutReplay(byte[] fdBytes) {
        try {
            byte[]  messageType  = new byte[] { fdBytes[5], fdBytes[6] };
            int     ver          = fdBytes[7];
            byte[]  sysDatetime  = new byte[7];
            byte[]  ISAM         = new byte[6];
            byte[]  batchNO      = new byte[4];
            int     replayCode   = fdBytes[25];

            System.arraycopy(fdBytes,   8,      sysDatetime,    0,      7);
            System.arraycopy(fdBytes,   15,     ISAM,           0,      6);
            System.arraycopy(fdBytes,   21,     batchNO,        0,      4);

            Logger.info(
                    "\n>>>> Analysis Signout apply Response <<<<" +
                            "\n>>>> messageType  : " + PubUtils.BA2HS(messageType) +
                            "\n>>>> ver          : " + ver +
                            "\n>>>> sysDatetime  : " + PubUtils.BA2HS(sysDatetime) +
                            "\n>>>> ISAM         : " + PubUtils.BA2HS(ISAM) +
                            "\n>>>> batchNO      : " + PubUtils.BA2HS(batchNO) +
                            "\n>>>> replayCode   : " + replayCode
            );

        } catch(Exception e) {
            Logger.error(">>>> FAIL:Analysis Signout apply fail", e);
        }
    }

}

