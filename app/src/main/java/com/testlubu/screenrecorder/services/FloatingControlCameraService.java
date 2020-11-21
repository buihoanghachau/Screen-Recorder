package com.testlubu.screenrecorder.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import androidx.annotation.Nullable;
import com.testlubu.screenrecorder.common.Const;
import com.testlubu.screenrecorder.services.FloatingCameraViewService;

public class FloatingControlCameraService extends Service {
    private IBinder binder = new ServiceBinder();
    private ServiceConnection floatingCameraConnection = new ServiceConnection() {
        /* class com.testlubu.screenrecorder.services.FloatingControlCameraService.AnonymousClass1 */

        public void onServiceDisconnected(ComponentName componentName) {
        }

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ((FloatingCameraViewService.ServiceBinder) iBinder).getService();
        }
    };
    private SharedPreferences prefs;

    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint({"WrongConstant"})
    public int onStartCommand(Intent intent, int i, int i2) {
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Intent intent2 = new Intent(this, FloatingCameraViewService.class);
        startService(intent2);
        bindService(intent2, this.floatingCameraConnection, 1);
        return super.onStartCommand(intent, i, i2);
    }

    public void onDestroy() {
        unbindService(this.floatingCameraConnection);
        this.prefs.edit().putBoolean(Const.PREFS_TOOLS_BRUSH, false).apply();
        super.onDestroy();
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        Log.d(Const.TAG, "Binding successful!");
        return this.binder;
    }

    public class ServiceBinder extends Binder {
        public ServiceBinder() {
        }

        /* access modifiers changed from: package-private */
        public FloatingControlCameraService getService() {
            return FloatingControlCameraService.this;
        }
    }
}
