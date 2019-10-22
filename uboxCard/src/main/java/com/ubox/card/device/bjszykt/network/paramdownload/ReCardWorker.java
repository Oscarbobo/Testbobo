package com.ubox.card.device.bjszykt.network.paramdownload;

import com.ubox.card.device.bjszykt.pubwork.LocalContext;
import com.ubox.card.util.logger.Logger;

public class ReCardWorker extends DownloadWorker {

    public ReCardWorker() {
        super(LocalContext.RETURN_CARD, 20, "Return Card", new byte[] {0x10, 0x00, 0x00, 0x00});
    }

    @Override
    protected void analyisData(byte[] parmData) {
        Logger.warn(">>>>WARN:Not Analyze Return Card");
    }
}
