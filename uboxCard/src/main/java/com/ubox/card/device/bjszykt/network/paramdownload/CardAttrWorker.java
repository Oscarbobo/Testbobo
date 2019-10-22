package com.ubox.card.device.bjszykt.network.paramdownload;

import com.ubox.card.device.bjszykt.pubwork.LocalContext;
import com.ubox.card.device.bjszykt.pubwork.PubUtils;
import com.ubox.card.util.logger.Logger;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class CardAttrWorker extends DownloadWorker {

    public CardAttrWorker() {
        super(LocalContext.CARD_ATTR, 42, "Card Attribute", new byte[]{(byte) 0x01, 0x00, 0x00, 0x00});
    }

    @Override
    protected void analyisData(byte[] parmData) {
        /* 卡片属性操作类*/
        class CardAtt {
            public final int phCardType;
            public final int cardType;
            public final byte[] cardTypeName;
            public final byte[] cardTransAtt;

            CardAtt(int phCardType, int cardType, byte[] cardTypeName, byte[] cardTransAtt) {
                this.phCardType     = phCardType;
                this.cardType       = cardType;
                this.cardTypeName   = cardTypeName;
                this.cardTransAtt   = cardTransAtt;
            }

            String logSenc() {
                StringBuilder sb = new StringBuilder(30);
                try {
                    sb.append("\n>>>> phCardType   : 0x").append(PubUtils.BA2HS(new byte[]{(byte) phCardType}));
                    sb.append("\n>>>> cardType     : 0x").append(PubUtils.BA2HS(new byte[]{(byte) cardType}));
                    sb.append("\n>>>> cardTypeName : ").append(new String(cardTypeName, "GBK"));
                    sb.append("\n>>>> cardTransAtt : ").append(PubUtils.BA2HS(cardTransAtt));
                } catch(UnsupportedEncodingException e) {
                    Logger.warn(">>>> FAIL: character conversion");
                }

                return sb.toString();
            }
        }

        /* 逻辑操作 */
        try {
            StringBuilder sb = new StringBuilder(300);
            sb.append("\n>>>> Analyze Card Attribute <<<<");

            int dataLen = parmData.length;
            int index   = 0;
            int step    = 42;
            while((index + step) <= dataLen) {
                sb.append("\n -----------------")
                  .append(
                          new CardAtt(
                              parmData[index],
                              parmData[index + 1],
                              Arrays.copyOfRange(parmData, index + 2, index + 18),
                              Arrays.copyOfRange(parmData, index + 18, index + 42)
                          ).logSenc()
                );

                index +=step;
            }

            if(index < dataLen)  sb.append("\n------ ERROR DATA ------\n").append(PubUtils.BA2HS(Arrays.copyOfRange(parmData, index, dataLen - 1)));

            Logger.info(sb.toString());
        } catch(Exception e) {
            Logger.warn(">>>> FAIL: Analyze Card Attribute");
        }
    }
}
