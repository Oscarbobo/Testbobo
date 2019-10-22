package com.ubox.card.device.bjszykt.network.paramdownload;

import com.ubox.card.device.bjszykt.pubwork.LocalContext;
import com.ubox.card.util.logger.Logger;

public class BlackListWorker extends DownloadWorker {

    public BlackListWorker() {
        super(LocalContext.BLACK_LIST, 16, "blacklist", new byte[]{(byte) 0x80, 0x00, 0x00, 0x00});
    }

    @Override
    protected void analyisData(byte[] parmData) {
        Logger.warn(">>>> WARN:Not analyze blacklist");
    }
}
