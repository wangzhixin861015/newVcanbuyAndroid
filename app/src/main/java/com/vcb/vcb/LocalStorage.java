package com.vcb.vcb;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by huangjianhua on 2018/8/20.
 */

public class LocalStorage {

    private static SharedPreferences sp;
    private static final String PREFERENCE_FILE_NAME = "storage";
    private static SharedPreferences.Editor editor;


    synchronized public static void init(Context context) {
        if (sp == null) {
            try {
                sp = context.getSharedPreferences(PREFERENCE_FILE_NAME, MODE_PRIVATE);
                editor = sp.edit();
            } catch (Exception e) {
            }
        }

    }

    public static void set(String key, String value) {
        if (editor != null) {
            editor.putString(key, value);
            editor.commit();
        }

    }

    public static String get(String key) {
        return sp.getString(key, "");
    }


    public static void clear(String key) {
        if (editor != null) {
            editor.remove(key);
            editor.commit();
        }
    }

}
