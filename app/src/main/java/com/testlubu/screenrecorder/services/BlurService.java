package com.testlubu.screenrecorder.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.listener.ObserverUtils;
import com.testlubu.screenrecorder.model.listener.EvbClickBlur;

public class BlurService extends Service implements View.OnTouchListener, View.OnClickListener {
    private ConstraintLayout mLayout;
    private WindowManager.LayoutParams mParams;
    private WindowManager windowManager;

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onClick(View view) {
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        return true;
    }

    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint({"WrongConstant"})
    private void initView() {
        this.windowManager = (WindowManager) getApplicationContext().getSystemService("window");
        this.mLayout = (ConstraintLayout) ((LayoutInflater) getApplicationContext().getSystemService("layout_inflater")).inflate(R.layout.layout_main_blur, (ViewGroup) null);
        this.mParams = new WindowManager.LayoutParams(-1, -1, 2038, 8, -3);
        if (Build.VERSION.SDK_INT < 26) {
            this.mParams.type = 2005;
        }
        this.windowManager.addView(this.mLayout, this.mParams);
        this.mLayout.setOnClickListener(new View.OnClickListener() {
            /* class com.testlubu.screenrecorder.services.BlurService.AnonymousClass1 */

            public void onClick(View view) {
                ObserverUtils.getInstance().notifyObservers(new EvbClickBlur());
                BlurService.this.stopSelf();
            }
        });
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        initView();
        return 2;
    }

    public void onDestroy() {
        ConstraintLayout constraintLayout;
        WindowManager windowManager2 = this.windowManager;
        if (!(windowManager2 == null || (constraintLayout = this.mLayout) == null)) {
            windowManager2.removeView(constraintLayout);
        }
        super.onDestroy();
    }
}
