package com.ubox.card.deploy;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.content.Context;

import com.ubox.card.R;
import com.ubox.card.util.logger.Logger;

public class Reader {
	
	private final Context context;
	
	public Reader(Context context) { this.context = context; }

	public String readCards() {
		try {
			String                assets_cards = context.getString(R.string.assets_cards);
			InputStream           cards_stream = context.getAssets().open(assets_cards);
			ByteArrayOutputStream outBuff      = new ByteArrayOutputStream();
			
			byte[] buffer = new byte[256];
			int    len    = 0;
			while((len = cards_stream.read(buffer)) != -1) {
				outBuff.write(buffer, 0, len);
			}
			
			return new String(outBuff.toByteArray());
		} catch (Exception e) {
			Logger.error("Read cards error", e);
			return null;
		}
	}
}
