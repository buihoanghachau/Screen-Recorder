package com.ads.control.funtion;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.testlubu.screenrecorder.common.PrefUtils;
import java.util.Locale;

public class UtilsApp {
    public static void ShowToastShort(Context context, String str) {
        Toast.makeText(context, str, 0).show();
    }

    public static void ShowToastLong(Context context, String str) {
        Toast.makeText(context, str, 1).show();
    }

    public static void OpenMoreApp(Context context, String str) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setData(Uri.parse("market://search?q=pub:" + str));
        context.startActivity(intent);
    }

    public static void OpenBrower(Context context, String str) {
        try {
            context.startActivity(new Intent("android.intent.action.VIEW").setData(Uri.parse(str)));
        } catch (Exception unused) {
        }
    }

    public static boolean isPackageInstalled(String str, PackageManager packageManager) {
        try {
            return packageManager.getApplicationInfo(str, 0).enabled;
        } catch (PackageManager.NameNotFoundException unused) {
            return false;
        }
    }

    public static void RateApp(Context context) {
        context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName())));
    }

    public static void SendFeedBack(Context context, String str, String str2) {
        String[] strArr = {str};
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setData(Uri.parse("mailto:"));
        intent.setType("message/rfc822");
        intent.putExtra("android.intent.extra.EMAIL", strArr);
        intent.putExtra("android.intent.extra.SUBJECT", str2);
        intent.putExtra("android.intent.extra.TEXT", "Enter your FeedBack");
        try {
            context.startActivity(Intent.createChooser(intent, "Send FeedBack..."));
        } catch (ActivityNotFoundException unused) {
            ShowToastShort(context, "There is no email client installed.");
        }
    }

    public static boolean isConnectionAvailable(Context context) {
        NetworkInfo activeNetworkInfo;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        if (connectivityManager == null || (activeNetworkInfo = connectivityManager.getActiveNetworkInfo()) == null || !activeNetworkInfo.isConnected() || !activeNetworkInfo.isConnectedOrConnecting() || !activeNetworkInfo.isAvailable()) {
            return false;
        }
        return true;
    }

    public static void shareApp(Context context) {
        String packageName = context.getPackageName();
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");
        intent.putExtra("android.intent.extra.TEXT", "Check out the App at: https://play.google.com/store/apps/details?id=" + packageName);
        intent.setType("text/plain");
        context.startActivity(intent);
    }

    public static boolean isMyServiceRunning(Class<?> cls, Context context) {
        for (ActivityManager.RunningServiceInfo runningServiceInfo : ((ActivityManager) context.getSystemService("activity")).getRunningServices(Integer.MAX_VALUE)) {
            if (cls.getName().equals(runningServiceInfo.service.getClassName())) {
                Log.i("isMyServiceRunning?", "true");
                return true;
            }
        }
        Log.i("isMyServiceRunning?", "false");
        return false;
    }

    public static void setLanguageAds(Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("Frist_ads", true)) {
            PreferenceManager.getDefaultSharedPreferences(context).getString("lgAds", PrefUtils.VALUE_LANGUAGE);
            SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
            edit.putBoolean("Frist_ads", false);
            if (Locale.getDefault().getLanguage().equalsIgnoreCase("vi")) {
                edit.putString("lgAds", "vi");
            }
            edit.commit();
        }
    }

    public static String getTextLanguage(String str, String str2) {
        String[] split = str.split(str2 + ":");
        if (split.length == 1) {
            split = str.split("en:");
        }
        if (split.length == 1) {
            return split[0];
        }
        return split[1].split(",,")[0];
    }
}
