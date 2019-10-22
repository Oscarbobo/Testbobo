package com.ubox.card.device.bjszykt.network.paramdownload;

import com.ubox.card.device.bjszykt.pubwork.LocalContext;
import com.ubox.card.device.bjszykt.pubwork.PubUtils;
import com.ubox.card.util.logger.Logger;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class CmCardTypeWorker extends DownloadWorker {

    public CmCardTypeWorker() {
        super(LocalContext.CONSUM_CARD_TYPE, 64, "Consume Card Type", new byte[]{(byte) 0x20, 0x00, 0x00, 0x00});
    }

    @Override
    protected void analyisData(byte[] parmData) {
        /** 消费可用卡类型参数格式类 */
        class ConsumCardType {
            public final int    physicalType;
            public final int    logicType;
            public final byte[] cardTypeName;
            public final int    cardAttr;
            public final int    treatment;

            ConsumCardType(int physicalType, int logicType, byte[] cardTypeName, int cardAttr, int treatment) {
                this.physicalType   = physicalType;
                this.logicType      = logicType;
                this.cardTypeName   = cardTypeName;
                this.cardAttr       = cardAttr;
                this.treatment      = treatment;
            }

            String logStr() {
                StringBuilder sb = new StringBuilder(30);
                try {
                    sb.append("\n>>>> physicalType   : 0x").append(PubUtils.BA2HS(new byte[]{(byte) physicalType}));
                    sb.append("\n>>>> logicType      : 0x").append(PubUtils.BA2HS(new byte[]{(byte) logicType}));
                    sb.append("\n>>>> cardTypeName   : ").append(new String(cardTypeName, "GBK"));
                    sb.append("\n>>>> cardAttr       : 0x").append(PubUtils.BA2HS(new byte[]{(byte) cardAttr}));
                    sb.append("\n>>>> treatment      : 0x").append(PubUtils.BA2HS(new byte[]{(byte) treatment}));
                } catch(UnsupportedEncodingException e) {
                    Logger.warn(">>>> FAIL:character conversion");
                }

                return sb.toString();
            }
        }

        /* 逻辑操作 */
        try {
            StringBuilder sb = new StringBuilder(300);
            sb.append("\n>>>> Consume Card Type <<<<");

            int dataLen = parmData.length;
            int index   = 0;
            int step    = 32;
            while((index + step) <= dataLen) {
                sb.append("\n -----------------")
                  .append(
                          new ConsumCardType(
                              parmData[index],
                              parmData[index + 1],
                              Arrays.copyOfRange(parmData, index + 2, index + 16),
                              parmData[index + 18],
                              parmData[index + 19]
                          ).logStr()
                );

                index +=step;
            }

            if(index < dataLen)  sb.append("\n------ ERROR DATA ------\n").append(PubUtils.BA2HS(Arrays.copyOfRange(parmData, index, dataLen - 1)));

            Logger.info(sb.toString());
        } catch(Exception e) {
            Logger.warn(">>>> FAIL: Analyze Consume Card Type");
        }
    }
}
