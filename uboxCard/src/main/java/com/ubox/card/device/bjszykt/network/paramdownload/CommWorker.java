package com.ubox.card.device.bjszykt.network.paramdownload;

import com.ubox.card.device.bjszykt.pubwork.LocalContext;
import com.ubox.card.device.bjszykt.pubwork.PubUtils;
import com.ubox.card.util.logger.Logger;

public class CommWorker extends DownloadWorker {

    public CommWorker() {
        super(LocalContext.COMMUNICATION_PARAM, 200, "communications", new byte[]{(byte) 0x08, 0x00, 0x00, 0x00});
    }

    @Override
    protected void analyisData(byte[] parmData) {
        try {
            byte[] dailNumber_Byte_1    = new byte[8];
            byte[] dailNumber_Byte_2    = new byte[8];
            byte[] dailNumber_Byte_3    = new byte[8];
            byte[] serverIP_Byte_1      = new byte[40];
            byte[] serverPORT_Byte_1    = new byte[3];
            byte[] serverIP_Byte_2      = new byte[40];
            byte[] serverPORT_Byte_2    = new byte[3];
            byte[] serverIP_Byte_3      = new byte[40];
            byte[] serverPORT_Byte_3    = new byte[3];
            byte[] serverIP_Byte_4      = new byte[40];
            byte[] serverPORT_Byte_4    = new byte[3];

            System.arraycopy(parmData,    0,      dailNumber_Byte_1,   0,     8);
            System.arraycopy(parmData,    8,      dailNumber_Byte_2,   0,     8);
            System.arraycopy(parmData,    16,     dailNumber_Byte_3,   0,     8);
            System.arraycopy(parmData,    24,     serverIP_Byte_1,     0,     40);
            System.arraycopy(parmData,    64,     serverPORT_Byte_1,   0,     3);
            System.arraycopy(parmData,    67,     serverIP_Byte_2,     0,     40);
            System.arraycopy(parmData,    107,    serverPORT_Byte_2,   0,     3);
            System.arraycopy(parmData,    110,    serverIP_Byte_3,     0,     40);
            System.arraycopy(parmData,    150,    serverPORT_Byte_3,   0,     3);
            System.arraycopy(parmData,    153,    serverIP_Byte_4,     0,     40);
            System.arraycopy(parmData,    193,    serverPORT_Byte_4,   0,     3);

            Logger.info(
                    "\n>>>> Communications <<<<" +
                    "\n>>>> Dail Number 1:   " + PubUtils.BA2HS(dailNumber_Byte_1) +
                    "\n>>>> Dail Number 2:   " + PubUtils.BA2HS(dailNumber_Byte_2) +
                    "\n>>>> Dail Number 3:   " + PubUtils.BA2HS(dailNumber_Byte_3) +
                    "\n>>>> Server 1   IP:   " + new String(serverIP_Byte_1) +
                    "\n>>>> Server 1 PORT:   " + PubUtils.BA2HS(serverPORT_Byte_1) +
                    "\n>>>> Server 2   IP:   " + new String(serverIP_Byte_2) +
                    "\n>>>> Server 2 PORT:   " + PubUtils.BA2HS(serverPORT_Byte_2) +
                    "\n>>>> Server 3   IP:   " + new String(serverIP_Byte_3) +
                    "\n>>>> Server 3 PORT:   " + PubUtils.BA2HS(serverPORT_Byte_3) +
                    "\n>>>> Server 4   IP:   " + new String(serverIP_Byte_4) +
                    "\n>>>> Server 4 PORT:   " + PubUtils.BA2HS(serverPORT_Byte_4)
            );
        } catch(Exception e) {
            Logger.warn(">>>> FAIL:Analyze communications");
        }
    }
}
