package com.ubox.card.device.bjszykt.network.paramdownload;

import com.ubox.card.device.bjszykt.pubwork.LocalContext;
import com.ubox.card.util.logger.Logger;

public class UpdataBlacklistWorker extends DownloadWorker {

    public UpdataBlacklistWorker() {
        super(LocalContext.UPDATA_BLACK_LIST, 18, "UpdataBlacklist", new byte[] {0x00, 0x10, 0x00, 0x00});
    }

    @Override
    protected void analyisData(byte[] parmData) {
        Logger.warn(">>>>WARN:Not analysis UpdataBlacklist");
    }
}
