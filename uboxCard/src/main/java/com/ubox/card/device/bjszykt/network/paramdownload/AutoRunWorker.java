package com.ubox.card.device.bjszykt.network.paramdownload;

import com.ubox.card.device.bjszykt.pubwork.LocalContext;
import com.ubox.card.util.logger.Logger;

public class AutoRunWorker extends DownloadWorker {

    public AutoRunWorker() {
        super(LocalContext.AUTO_DATA, 16, "Auto Run", new byte[] {0x02, 0x00, 0x00, 0x00});
    }

    @Override
    protected void analyisData(byte[] parmData) {
        Logger.warn(">>>> WARN: Not analyze Auto Run");
    }
}
