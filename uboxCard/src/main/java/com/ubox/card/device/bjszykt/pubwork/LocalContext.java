package com.ubox.card.device.bjszykt.pubwork;

import com.ubox.card.config.CardConst;
import com.ubox.card.config.CardJson;
import com.ubox.card.util.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PropertyResourceBundle;

public class LocalContext {

    public static final String LINE_SEPARATOR = "\n";

    //=================================VCARD工作状态
    public static volatile int VCARD_STATUS = 0;

    public static final int VCRAD_READY     = 1;
    public static final int SIGN_WORK       = 2; // 签到工作中
    public static final int SCHEDULE_WORK   = 3; // 定时工作
    public static final int TRAN_DATA_ERROR = 5;

    //================================= Work params ===
    public static String oprId;
    public static String posId;
    public static String unitId;
    public static String mchntId;
    public static String programName;
    public static String workPath;
    public static String batchNo ;

    public static final String BLACK_LIST           = "params_blacklist.dat";
    public static final String STORED_CARD_TYPE     = "params_storedCardType.dat";//充值卡类型参数
    public static final String CONSUM_CARD_TYPE     = "params_consumeCardType.dat";
    public static final String RETURN_CARD          = "params_returnCard.dat";
    public static final String COMMUNICATION_PARAM  = "params_communicat.dat";
    public static final String OPERATION_PARAM      = "params_oper.dat";
    public static final String AUTO_DATA            = "params_auto.dat";
    public static final String CARD_ATTR            = "params_cardAttr.dat";//卡片属性参数
    public static final String REG_BLACK_LIST       = "params_regBlacklist.dat";
    public static final String TER_SERVICE          = "params_terService.dat";
    public static final String TOTAL_NUM_CARD       = "params_totalNumCard.dat";
    public static final String UPDATA_BLACK_LIST    = "params_updataBlacklist.dat";
    public static final String TRANSACTION_DATA     = "trans_data.dat";
    public static final String GREY_LIST            = "params_greylist.dat";//灰名单

    static {
        oprId           = "000000";
        posId           = CardJson.vmId;
        unitId          = "75140012";
        mchntId         = "003100000001";
        programName     = "1"; // 一个byte,-128~127之间的值
//        programName     = "0";//TODO
        workPath        = CardConst.VCARD_PATH + File.separator + "resource" + File.separator + "bjszykt";

        try {
        	File workpathFile = new File(workPath);
        	if(!workpathFile.exists()) workpathFile.mkdirs();
        	
            PropertyResourceBundle rb = new PropertyResourceBundle(new FileInputStream(workPath + File.separator + "configs.ini"));
            batchNo = rb.getString("batchNO");
        } catch(Exception e) {
        	Logger.warn(">>>>FAIL: get batchNO fail");
            batchNo = "1";
        }

        Logger.info(">>>> LocalContext configs <<<<" +
                "\n>>>> oprId            :" + oprId +
                "\n>>>> posId            :" + posId +
                "\n>>>> unitId           :" + unitId +
                "\n>>>> mchntId          :" + mchntId +
                "\n>>>> ProgramName      :" + programName +
                "\n>>>> workPath         :" + workPath +
                "\n>>>> batchNo          :" + batchNo
        );
    }

    // ================================== VCARD SERVER ==========
    //正式环境
    public static final String URL_BASE             = "http://bjszykt.vm-pay.uboxol.com:" + CardJson.httpPort + "/vcardServer";
//    public static final String URL_BASE             = "http://106.39.95.5:7081/vcardServer";
    public static final String URL_SIGN             = URL_BASE + "/client/bjszykt/signIn/apply";
    public static final String URL_SIGN_OUT         = URL_BASE + "/client/bjszykt/signout/request";
    public static final String URL_QUERY            = URL_BASE + "/client/bjszykt/paramList/query";
    public static final String URL_PARAM            = URL_BASE + "/client/bjszykt/paramList/request";
    public static final String URL_PARAM_DOWNLOAD   = URL_BASE + "/client/bjszykt/paramList/download";
    public static final String URL_PARAM_OVER       = URL_BASE + "/client/bjszykt/paramList/over";
    public static final String URL_DATA             = URL_BASE + "/client/bjszykt/data/request";
    public static final String URL_DATA_UPLOAD      = URL_BASE + "/client/bjszykt/data/transfer";
    public static final String URL_DATA_OVER        = URL_BASE + "/client/bjszykt/data/over";
    public static final String URL_VERIFY           = URL_BASE + "/client/bjszykt/signIn/verify";

    public static final String SIGN_KEY             = "ubox@bjszykt";
    public static final String INPUT_CHARSET        = "UTF-8";


    // ===================================== DATA CACHES

    public static volatile String CACHE_SAM;              // SAM卡号
    public static volatile String CACHE_RANDOM;           // 随机数

    public static volatile String CACHE_CSN;              // 卡序列号
    public static volatile String CACHE_PUB_BALANCE;      // 交易前余额
    public static volatile String CACHE_CARD_COUNT;      // 交易前余额

    public static volatile String CACHE_ENC_TEXT;         // SAM卡随机数产生的密文
    public static volatile String CACHE_LIMIT_TIME;       // 授权截至时间
    public static volatile String CACHE_BATCH_NO;         // 批次号
    public static volatile String CACHE_POS_IC_SEQ;       // 终端IC交易流水号
    public static volatile String CACHE_POS_ACC_SEQ;      // 终端账户交易流水号
    public static volatile String CACHE_POS_COMM_SEQ;     // 通讯流水号
    public static volatile String CACHE_MAC_BUF;          // 工作密钥密文

    public static final HashMap<Long, String>   CACHE_BLACK_LIST  = new HashMap<Long, String>(5000); // 黑名单
    public static final ArrayList<String>       CACHE_CONSUME     = new ArrayList<String>(10); // 可消费卡类型
    public static final ArrayList<String>       SOR_CARD_TYPE     = new ArrayList<String>(20); // 充值卡类型
    public static final ArrayList<String>       CARD_ATTR_LIST     = new ArrayList<String>(20); // 卡片属性定义参数
    public static final ArrayList<String>       CARD_GREY_LIST     = new ArrayList<String>(20); // 灰名单

}
