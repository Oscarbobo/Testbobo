package com.ubox.card.device.zjylsf.compos;


import java.io.UnsupportedEncodingException;

import com.ubox.card.device.zjylsf.zjfutils.ZjfuUtil;
import com.ubox.card.util.logger.Logger;

public class Analysisor {

    static void analysisSettle(byte[][] data) throws UnsupportedEncodingException {
        Logger.info(
                ">>>> SETTLE INFO <<<<\n"
              + ">>>> cmd  = " + ZjfuUtil.BA2HS(data[0]) + "h\n"
              + ">>>> code = " + new String(data[1]) + "\n"
              + ">>>> msg  = " + new String(data[2], "GBK")+ "\n"
              + ">>>> code = " + new String(data[3]) + "h\n"
              + ">>>> 商户代码 = "  + new String(data[4]) + "\n"
              + ">>>> 终端号   = "  + new String(data[5]) + "\n"
              + ">>>> 商户中文名 = "        + new String(data[6], "GBK")+ "\n"
              + ">>>> 商户英文名 = "        + new String(data[7], "GBK")+ "\n"
              + ">>>> 交易批次号 = "        + new String(data[8])+ "\n"
              + ">>>> 交易日期和时间 = "        + new String(data[9])+ "\n"
              + ">>>> 内卡结帐平标志 = "        + new String(data[10])+ "\n"
              + ">>>> 内卡消费笔数 = "          + new String(data[11])+ "\n"
              + ">>>> 内卡消费金额 = "          + new String(data[12])+ "\n"
              + ">>>> 内卡退货笔数 = "          + new String(data[13])+ "\n"
              + ">>>> 内卡退货金额 = "          + new String(data[14])+ "\n"
              + ">>>> 内卡预授完成(联机)笔数 = "   + new String(data[15])+ "\n"
              + ">>>> 内卡预授完成(联机)金额 = "   + new String(data[16])+ "\n"
              + ">>>> 内卡预授完成(离线)笔数 = "   + new String(data[17])+ "\n"
              + ">>>> 内卡预授完成(离线)金额 = "   + new String(data[18])+ "\n"
              + ">>>> 内卡离线交易笔数 = "      + new String(data[19])+ "\n"
              + ">>>> 内卡离线交易金额 = "      + new String(data[20])+ "\n"
              + ">>>> 外卡结帐平标志 = "        + new String(data[21])+ "\n"
              + ">>>> 外卡消费笔数 = " + new String(data[22])+ "\n"
              + ">>>> 外卡消费金额 = " + new String(data[23])+ "\n"
              + ">>>> 外卡退货笔数 = " + new String(data[24])+ "\n"
              + ">>>> 外卡退货金额 = " + new String(data[25])+ "\n"
              + ">>>> 外卡预授完成(联机)笔数 = " + new String(data[26])+ "\n"
              + ">>>> 外卡预授完成(联机)金额 = " + new String(data[27])+ "\n"
              + ">>>> 外卡预授完成(离线)笔数 = " + new String(data[28])+ "\n"
              + ">>>> 外卡预授完成(离线)金额 = " + new String(data[29])+ "\n"
              + ">>>> 外卡离线交易笔数 = " + new String(data[30])+ "\n"
              + ">>>> 外卡离线交易金额 = " + new String(data[31])+ "\n"
              + ">>>> 备注 = " + new String(data[32])
        );

    }
}
