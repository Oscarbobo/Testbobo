package com.ubox.card.device.bjszykt.network.paramdownload;

import com.ubox.card.device.bjszykt.pubwork.LocalContext;
import com.ubox.card.util.logger.Logger;

public class TotalNumCardWorker extends DownloadWorker {

    public TotalNumCardWorker() {
        super(LocalContext.TOTAL_NUM_CARD, 32, "Total Number Card", new byte[]{0x00, 0x20, 0x00, 0x00});
    }

    @Override
    protected void analyisData(byte[] parmData) {
        Logger.warn(">>>>WARN:Not Analyze Total Number Card");
    }
}
