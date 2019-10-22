package com.ubox.card;

import com.ubox.card.device.mzt.MZTSerial;
import com.ubox.card.util.logger.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class CardReceiver extends BroadcastReceiver {
	private String serviceName = "com.ubox.card.CardService";
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = "";
		try {
			action = intent.getAction();
			
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			return;
		}

		Logger.debug(">>>> VCARD action = " + action);
		if (action.equals("com.ubox.launcher.BOOT")) {
			Intent service = new Intent(serviceName);
			context.startService(service);
			Logger.debug(">>>> VCARD START");
		} else if (action.equals("com.ubox.launcher.STOP")) {
			Intent service = new Intent(serviceName);
			context.stopService(service);
			Logger.debug(">>>> VCARD STOP");
		} else if ("com.ubox.card.recv".equals(intent.getAction())) {
			Bundle msgB = intent.getBundleExtra("cardPost");
			if (null!=msgB) {
				String msg = msgB.getString("cardMsg");
				Logger.debug(">>>> VARD RECEIVE MGD: " + msg);
				CardService.recvMsg(msg);
			}else{
				Logger.error("error msg== msgB is null" );
			}
		}
	}
}
