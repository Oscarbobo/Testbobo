package com.ubox.card.device.bjszykt.recharge;

import com.ubox.card.config.CardJson;
import com.ubox.card.device.bjszykt.pubwork.LocalContext;


/**
 * Description:
 * Created by WangPeng
 * Date: 2015-09-17
 */
public class RechargeInfo {

	//读卡信息
    public static String oprId;//操作员代码
    public static String posId;
    public static String sam;
    public static String unitId;
    public static String mchntId;
    public static String batchNO;
    public static String cardNo;
    public static String cardType;
    public static String cardPhyType;
    public static String befBal;
    public static String msg1 = "orderQuery";
    public static String msg2 = "orderApply";
    public static String msg3 = "orderConfirm";

    //充值申请信息
    public static String rechargeInfo;
    public static String rechargeInfoMac;
    public static String rechargeInfoMacBuf;
    public static String rechargeInfoPsamTransNo;
    
    //充值申请反馈信息
    public static String AnswerNB;
    public static String MasterNB;
    public static String MasterID;
    public static String Data;
    
    //充值确认信息
    public static String AnsNb;
    public static String Type;
    public static String MasId;
    
    //充值确认反馈信息
    public static String AnsNB;
    public static String MAC;
    public static String Ciphertext;
    
    /********************************************/
//    public static final String URL_BASE             = "http://106.39.95.5:7081/vcardServer";
    public static final String URL_RECHARGE_APPLY   = LocalContext.URL_BASE + "/client/bjszykt?vmId=" + CardJson.vmId;


    /********************************************/

    public static volatile String CACHE_OPRID;
    public static volatile String CACHE_POSID;
    public static volatile String CACHE_SAM;              // SAM卡号
    public static volatile String CACHE_BATCHNO;
    public static volatile String CACHE_CARDNO;
    public static volatile String CACHE_ORDERNUM;
    public static volatile String CACHE_ORDERINFO;

    /**
     * 订单号字节码
     */
    public static volatile byte[] CACHE_BYTE_ORDERNO;
    /**
     * 订单金额字节码
     */
    public static volatile byte[] CACHE_BYTE_ORDER_SAVE_AMT;
    public static volatile String CACHE_ORDERNO;
    public static volatile String CACHE_ORDERSAVEAMT;
    public static volatile String CACHE_ORDERDATE;
    public static volatile String CACHE_ORDERTIME;
    // 通讯流水号
    public static volatile byte[] CACHE_POS_COMM_SEQ;

    //订单充值交易确认应答
    public static volatile String CONFIRM_TIME;
    public static volatile String CONFIRM_OPRID;
    public static volatile String CONFIRM_POSID;
    public static volatile String CONFIRM_SAM;
    public static volatile String CONFIRM_MAC;
    public static volatile String CONFIRM_LEN;
    public static volatile String CONFIRM_MACBUF;
    public static volatile String CONFIRM_ORDERNO;

    // 充值申请应答缓存数据
    public static volatile String CACHE_CIPHER_DATA_MAC;
    public static volatile String CACHE_CIPHER_DATA_LEN;
    public static volatile String CACHE_MACBUF;

}
