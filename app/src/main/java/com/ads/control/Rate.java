package com.ads.control;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;
import com.ads.control.funtion.UtilsApp;
import com.testlubu.screenrecorder.R;

public class Rate {
    public static void Show(Context context, int i) {
        if (!UtilsApp.isConnectionAvailable(context)) {
            ((Activity) context).finish();
        } else if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("Show_rate", false)) {
            RateApp rateApp = new RateApp(context, context.getString(R.string.email_feedback), context.getString(R.string.Title_email), i);
            rateApp.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
            rateApp.show();
        } else {
            ((Activity) context).finish();
        }
    }
}
