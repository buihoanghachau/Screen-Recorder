package com.testlubu.screenrecorder.ui.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.widget.Toast;
//import com.ads.control.AdmobHelp;
import com.testlubu.screenrecorder.BaseActivity;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.common.PrefUtils;
import java.util.Locale;

public class SplashScreenActivity extends BaseActivity {
    public static boolean isFirstOpen = true;
    Handler mHandler;
    Runnable r;

    /* access modifiers changed from: protected */
    @Override // androidx.core.app.ComponentActivity, androidx.appcompat.app.AppCompatActivity, com.testlubu.screenrecorder.BaseActivity, androidx.fragment.app.FragmentActivity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        setContentView(R.layout.activity_splash_screen);
//        AdmobHelp.getInstance().init(this);
        setLocale();
        isFirstOpen = true;
        this.mHandler = new Handler();
        this.r = new Runnable() {
            /* class com.testlubu.screenrecorder.ui.activities.SplashScreenActivity.AnonymousClass1 */

            public void run() {
                SplashScreenActivity.this.checkPermission();
            }
        };
        this.mHandler.postDelayed(this.r, 7000);
    }

    /* access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i != 12) {
            return;
        }
        if (isSystemAlertPermissionGranted(this)) {
            onPermissionGranted();
            return;
        }
        Toast.makeText(this, (int) R.string.str_permission_remind, 1).show();
        finishAffinity();
    }

    @SuppressLint({"NewApi"})
    public static boolean isSystemAlertPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        return Settings.canDrawOverlays(context);
    }

    @SuppressLint({"NewApi"})
    public void checkPermission() {
        if (isSystemAlertPermissionGranted(this)) {
            onPermissionGranted();
            return;
        }
        startActivityForResult(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + getPackageName())), 12);
        startActivity(new Intent(this, GuideActivity.class));
    }

    private void onPermissionGranted() {
//        try {
//            AdmobHelp.getInstance().showInterstitialAd(new AdmobHelp.AdCloseListener() {
//                /* class com.testlubu.screenrecorder.ui.activities.SplashScreenActivity.AnonymousClass2 */
//
//                @Override // com.ads.control.AdmobHelp.AdCloseListener
//                public void onAdClosed() {
                    Intent intent = new Intent(SplashScreenActivity.this, HomeActivity.class);
                    intent.setFlags(335544320);
                    SplashScreenActivity.this.startActivity(intent);
                    SplashScreenActivity.this.finish();
//                }
//            });
//        } catch (Exception unused) {
//        }
    }

    public void setLocale() {
        String readStringValue = PrefUtils.readStringValue(this, getString(R.string.language_key), PrefUtils.VALUE_LANGUAGE);
        if (PrefUtils.firstOpen(this)) {
            readStringValue = Locale.getDefault().getLanguage();
            if (readStringValue.equalsIgnoreCase("vi")) {
                PrefUtils.saveStringValue(this, getString(R.string.language_key), readStringValue);
            }
        }
        Locale locale = new Locale(readStringValue);
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        resources.updateConfiguration(configuration, displayMetrics);
    }

    /* access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, com.testlubu.screenrecorder.BaseActivity, androidx.fragment.app.FragmentActivity
    public void onDestroy() {
        Runnable runnable;
        Handler handler = this.mHandler;
        if (!(handler == null || (runnable = this.r) == null)) {
            handler.removeCallbacks(runnable);
        }
        super.onDestroy();
    }
}
