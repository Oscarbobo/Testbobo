package com.ubox.card;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.ubox.card.core.CardMain;
import com.ubox.card.core.CardOperator;
import com.ubox.card.util.logger.Logger;
import com.ubox.card.vs.http.HttpsLogin;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

public class CardService extends Service {
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		System.exit(0);
	}

	public static CardService service = null;

	@Override
	public void onCreate() {
		super.onCreate();
		Thread.setDefaultUncaughtExceptionHandler(mUncaughtExceptionHandler);
		
		HttpsLogin.init(getApplicationContext());
		CardMain.startUp();
		service = this;
	}

	public static void sendMsg(String sendMsg) {
		Intent intent = new Intent("com.ubox.card.broadcast");
		intent.putExtra("msg", sendMsg);
		if (service != null) {
			service.sendBroadcast(intent);
		}
	}

	public static void recvMsg(final String receiveMsg) {
        CardOperator.handle(receiveMsg);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private UncaughtExceptionHandler mUncaughtExceptionHandler = new UncaughtExceptionHandler() {

		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			
			StringBuffer sb = new StringBuffer();
			Writer writer = new StringWriter();
			PrintWriter pw = new PrintWriter(writer);
			ex.printStackTrace(pw);
			Throwable cause = ex.getCause();
			while (cause != null) {
				cause.printStackTrace(pw);
				cause = cause.getCause();
			}
			pw.close();
			String result = writer.toString();
			sb.append(result);
			Logger.error("mUncaughtExceptionHandler ---- ex === " + sb);
			ex.printStackTrace();
			System.exit(-1);
		}
	};

}
