package com.ubox.card.device.bjszykt.network.paramdownload;

import com.ubox.card.device.bjszykt.pubwork.LocalContext;
import com.ubox.card.device.bjszykt.pubwork.PubUtils;
import com.ubox.card.util.logger.Logger;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class TerServiceWorker extends DownloadWorker {

    public TerServiceWorker() {
        super(LocalContext.TER_SERVICE, 20, "Terminal Service", new byte[]{0x00, 0x40, 0x00, 0x00});
    }

    @Override
    protected void analyisData(byte[] parmData) {
        /** 终端业务功能参数格式类 */
        class TerServer {
            public final byte[] menu;
            public final byte[] code;

            TerServer(byte[] menu, byte[] code) {
                this.menu = menu;
                this.code = code;
            }

            String logStr() {
                StringBuilder sb = new StringBuilder(10);
                try {
                    sb.append("\n>>>> menu   :").append(new String(menu, "GBK"));
                    sb.append("\n>>>> code   :").append(PubUtils.b2iLt(code, 2));
                } catch(UnsupportedEncodingException e) {
                    Logger.warn(">>>>FAIL:character conversion");
                }

                return sb.toString();
            }
        }

        /* 逻辑操作 */
        try {
            StringBuilder sb = new StringBuilder(30);
            sb.append("\n>>>> Terminal Service <<<<");

            int dataLen = parmData.length;
            int index   = 0;
            int step    = 10;
            while((index + step) <= dataLen) {
                sb.append("\n -----------------")
                        .append(
                                new TerServer(
                                        Arrays.copyOfRange(parmData, index, index + 8),
                                        Arrays.copyOfRange(parmData, index + 8, index + 10)
                                ).logStr()
                        );

                index +=step;
            }
            if(index < dataLen)  sb.append("\n------ ERROR DATA ------\n").append(PubUtils.BA2HS(Arrays.copyOfRange(parmData, index, dataLen - 1)));

            Logger.info(sb.toString());
        } catch(Exception e) {
            Logger.warn(">>>> FAIL: Analyze terminal service");
        }
    }
}
