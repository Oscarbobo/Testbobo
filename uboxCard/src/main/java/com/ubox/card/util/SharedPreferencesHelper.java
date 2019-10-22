package com.ubox.card.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Base64;


/**
 */
public class SharedPreferencesHelper {
    private static final String dbName = "UboxCard";

    public static SharedPreferences getSharePre(Context context) {
        if (context != null) {
            return context.getSharedPreferences(dbName,Context.MODE_PRIVATE);
        }
        return null;
    }

    public static void putString(Context context, String key, String value) {
        SharedPreferences preferences = context.getSharedPreferences(dbName,Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString(key, value).apply();
    }

    public static String getString(Context context, String key, String defValue) {
        SharedPreferences preferences = context.getSharedPreferences(dbName,Context.MODE_PRIVATE);
        if (preferences != null) {
            return preferences.getString(key, defValue);
        }
        return "";
    }

    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences preferences = context.getSharedPreferences(dbName,Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(key, value).apply();
    }

    public static boolean getBoolean(Context context, String key,
                                     boolean defValue) {
        SharedPreferences preferences = context.getSharedPreferences(dbName,Context.MODE_PRIVATE);
        return preferences.getBoolean(key, defValue);
    }

    public static void putInt(Context context, String key, int value) {
        SharedPreferences preferences = context.getSharedPreferences(dbName,Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(key, value).apply();
    }

    public static int getInt(Context context, String key, int defValue) {
        SharedPreferences preferences = context.getSharedPreferences(dbName,Context.MODE_PRIVATE);
        return preferences.getInt(key, defValue);
    }

    public static void putLong(Context context, String key, long value) {
        SharedPreferences preferences = context.getSharedPreferences(dbName,Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putLong(key, value).apply();
    }

    public static long getLong(Context context, String key, long defValue) {
        SharedPreferences preferences = context.getSharedPreferences(dbName,Context.MODE_PRIVATE);
        return preferences.getLong(key, defValue);
    }

    public static void putFloat(Context context, String key, float value) {
        SharedPreferences preferences = context.getSharedPreferences(dbName,Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putFloat(key, value).apply();
    }

    public static float getFloat(Context context, String key, float defValue) {
        SharedPreferences preferences = context.getSharedPreferences(dbName,Context.MODE_PRIVATE);
        return preferences.getFloat(key, defValue);
    }

    public static void removeKeys(Context context, String... keys) {
        SharedPreferences preferences = context.getSharedPreferences(dbName,Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        for (int i = 0; i < keys.length; i++) {
            editor.remove(keys[i]);
        }
        editor.apply();
    }

    public static void removeKey(Context context, String key) {
        SharedPreferences preferences = context.getSharedPreferences(dbName,Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.remove(key);
        editor.apply();
    }

    public static void putObject(Context context, String key, Object value) {
        SharedPreferences preferences = context.getSharedPreferences("encode",Context.MODE_PRIVATE);
        // 创建字节输出流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            // 创建对象输出流，并封装字节流
            oos = new ObjectOutputStream(baos);
            // 将对象写入字节流
            oos.writeObject(value);
            oos.flush();
            // 将字节流编码成base64的字符串
            String productBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            Editor editor = preferences.edit();
            editor.putString(key, productBase64);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                oos.close();
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Object getObject(Context context, String key) {
        SharedPreferences preferences = context.getSharedPreferences("encode",Context.MODE_PRIVATE);
        String productBase64 = preferences.getString(key,"");
        if (!TextUtils.isEmpty(productBase64)) {
            // 读取字节
            byte[] base64 = Base64.decode(productBase64.getBytes(), Base64.DEFAULT);
            // 封装到字节流
            ByteArrayInputStream bais = new ByteArrayInputStream(base64);
            ObjectInputStream bis = null;
            try {
                // 再次封装
                bis = new ObjectInputStream(bais);
                try {
                    // 读取对象
                    return bis.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    bis.close();
                    bais.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
