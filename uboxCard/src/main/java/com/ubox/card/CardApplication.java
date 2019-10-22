package com.ubox.card;

import android.app.Application;
import android.os.Handler;
import android.os.Message;


/**
 * Created by qinrui on 2019/4/30.
 */
public class CardApplication extends Application {

    private  static Handler sHandler;


    @Override
    public void onCreate() {
        super.onCreate();
        sHandler =new Handler();

    }

    public  static  Handler getHandler() {
        return sHandler;
    }


    public static void dispatchMessage()
    {
        if (sHandler!=null) {
            Message message = sHandler.obtainMessage();
            sHandler.handleMessage(message);
        }
    }
}
