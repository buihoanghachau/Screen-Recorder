package com.testlubu.screenrecorder.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefUtils {
    public static final String VALUE_AUDIO = "1";
    public static final String VALUE_BITRATE = "7130317";
    public static final boolean VALUE_CAMERA = false;
    public static final boolean VALUE_ENABLE_TARGET_APP = false;
    public static final String VALUE_FRAMES = "30";
    public static final String VALUE_LANGUAGE = "en";
    public static final String VALUE_NAME_FORMAT = "yyyyMMdd_hhmmss";
    public static final String VALUE_NAME_PREFIX = "recording";
    public static final String VALUE_ORIENTATION = "auto";
    public static final String VALUE_RESOLUTION = "720";
    public static final boolean VALUE_SAVING_GIF = true;
    public static final boolean VALUE_SHAKE = false;
    public static final String VALUE_TIMER = "3";
    public static final boolean VALUE_TOUCHES = false;
    public static final boolean VALUE_USE_FLOAT = true;
    public static final boolean VALUE_VIBRATE = true;

    public static void saveStringValue(Context context, String str, String str2) {
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.putString(str, str2);
        edit.commit();
    }

    public static void savePosValue(Context context, String str, int i) {
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.putInt(str, i);
        edit.commit();
    }

    public static void saveBooleanValue(Context context, String str, boolean z) {
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.putBoolean(str, z);
        edit.commit();
    }

    public static String readStringValue(Context context, String str, String str2) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(str, str2);
    }

    public static int readPosValue(Context context, String str, int i) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(str, i);
    }

    public static boolean readBooleanValue(Context context, String str, boolean z) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(str, z);
    }

    public static boolean firstOpen(Context context) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean z = defaultSharedPreferences.getBoolean("firstopen", true);
        if (z) {
            SharedPreferences.Editor edit = defaultSharedPreferences.edit();
            edit.putBoolean("firstopen", false);
            edit.commit();
        }
        return z;
    }
}
