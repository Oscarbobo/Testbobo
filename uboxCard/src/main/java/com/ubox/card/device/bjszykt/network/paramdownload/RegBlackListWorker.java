package com.ubox.card.device.bjszykt.network.paramdownload;

import com.ubox.card.device.bjszykt.pubwork.LocalContext;
import com.ubox.card.util.logger.Logger;

public class RegBlackListWorker extends DownloadWorker{

    public RegBlackListWorker() {
        super(LocalContext.REG_BLACK_LIST, 32, "RegArea Blacklist", new byte[] {0x00, (byte)0x80, 0x00, 0x00});
    }

    @Override
    protected void analyisData(byte[] parmData) {
        Logger.warn(">>>>FAIL:Not Analyze RegArea blacklist");
    }
}
