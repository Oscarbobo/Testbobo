package com.ubox.card.device.bjszykt.network.paramdownload;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.ubox.card.device.bjszykt.pubwork.LocalContext;
import com.ubox.card.device.bjszykt.pubwork.PubUtils;
import com.ubox.card.util.logger.Logger;

public class GreyListWorker extends DownloadWorker {

	public GreyListWorker() {
        super(LocalContext.GREY_LIST, 10, "GreyList", new byte[]{(byte) 0x00, 0x01, 0x00, 0x00});
    }

	@Override
	protected void analyisData(byte[] parmData) {
		/** 灰名单 */
        class ConsumCardType {
            public final int    geryMark;
            public final byte[] geryValue;

            ConsumCardType(int geryMark, byte[] geryValue) {
                this.geryMark   = geryMark;
                this.geryValue   = geryValue;
            }

            String logStr() {
                StringBuilder sb = new StringBuilder(30);
                try {
                    sb.append("\n>>>> physicalType   : 0x").append(PubUtils.BA2HS(new byte[]{(byte) geryMark}));
                    sb.append("\n>>>> cardTypeName   : ").append(new String(geryValue, "GBK"));
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
                              Arrays.copyOfRange(parmData, index + 1, index + 4)
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
