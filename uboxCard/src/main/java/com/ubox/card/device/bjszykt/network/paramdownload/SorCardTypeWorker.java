package com.ubox.card.device.bjszykt.network.paramdownload;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.ubox.card.device.bjszykt.pubwork.LocalContext;
import com.ubox.card.device.bjszykt.pubwork.PubUtils;
import com.ubox.card.util.logger.Logger;

public class SorCardTypeWorker extends DownloadWorker {

    public SorCardTypeWorker() {
        super(LocalContext.STORED_CARD_TYPE, 68, "Store Card", new byte[] {0x40, 0x00, 0x00, 0x00});
    }

    @Override
    protected void analyisData(byte[] parmData) {
    	/* 充值卡类型操作类*/
    	class CardAtt {
            public final byte[] phCardType;
            public final byte[] cardType;
            public final byte[] cardTypeName;
            public final byte[] fRLeast;//首次充值最小额度
            public final byte[] sTBase;//单笔交易基数额度
            public final byte[] sTBit;//单笔交易最大额度
            public final byte[] bCBalance;//最大卡内余额
            public final byte[] addDate;//卡有效期顺延时间、0x00表示不顺延

            CardAtt(byte[] phCardType, byte[] cardType, byte[] cardTypeName, byte[] fRLeast,byte[] sTBase,byte[] sTBit,
            		byte[] bCBalance,byte[] addDate) {
                this.phCardType     = phCardType;
                this.cardType       = cardType;
                this.cardTypeName   = cardTypeName;
                this.fRLeast   		= fRLeast;
                this.sTBase   		= sTBase;
                this.sTBit   		= sTBit;
                this.bCBalance   	= bCBalance;
                this.addDate   		= addDate;
            }

            String logSenc() {
                StringBuilder sb = new StringBuilder(30);
                try {
                    sb.append("\n>>>> phCardType 	: 0x").append(PubUtils.BA2HS(phCardType));
                    sb.append("\n>>>> cardType    	: 0x").append(PubUtils.BA2HS(cardType));
                    sb.append("\n>>>> cardTypeName	: ").append(new String(cardTypeName, "GBK"));
                    sb.append("\n>>>> fRLeast		: ").append(PubUtils.BA2HS(fRLeast));
                    sb.append("\n>>>> sTBase 		: ").append(PubUtils.BA2HS(sTBase));
                    sb.append("\n>>>> sTBit 		: ").append(PubUtils.BA2HS(sTBit));
                    sb.append("\n>>>> bCBalance 	: ").append(PubUtils.BA2HS(bCBalance));
                    sb.append("\n>>>> addDate 		: ").append(PubUtils.BA2HS(addDate));
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
            int step    = 68;
            while((index + step) <= dataLen) {
                sb.append("\n -----------------")
                  .append(
                          new CardAtt(
//                              parmData[index],
//                              parmData[index + 1],
                              Arrays.copyOfRange(parmData, index ,  index + 1),
                              Arrays.copyOfRange(parmData, index + 1,  index + 3),
                              Arrays.copyOfRange(parmData, index + 3,  index + 36),
                              Arrays.copyOfRange(parmData, index + 36, index + 40),
                              Arrays.copyOfRange(parmData, index + 40, index + 44),
                              Arrays.copyOfRange(parmData, index + 44, index + 52),
                              Arrays.copyOfRange(parmData, index + 52, index + 60),
                              Arrays.copyOfRange(parmData, index + 60, index + 68)
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
