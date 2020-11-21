package com.testlubu.screenrecorder.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.testlubu.screenrecorder.BaseActivity;
import com.testlubu.screenrecorder.common.Const;
import com.testlubu.screenrecorder.services.FloatingControlService;
import com.testlubu.screenrecorder.services.RecorderService;

public class RecorderActivity extends BaseActivity {
    /* access modifiers changed from: protected */
    @Override // androidx.core.app.ComponentActivity, androidx.appcompat.app.AppCompatActivity, com.testlubu.screenrecorder.BaseActivity, androidx.fragment.app.FragmentActivity
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        try {
            if (FloatingControlService.isCountdown) {
                finish();
                return;
            }
            String action = getIntent().getAction();
            char c = 65535;
            switch (action.hashCode()) {
                case -1996135482:
                    if (action.equals(Const.SCREEN_RECORDING_START_FROM_NOTIFY)) {
                        c = 1;
                        break;
                    }
                    break;
                case -1053033865:
                    if (action.equals(Const.SCREEN_RECORDING_STOP)) {
                        c = 2;
                        break;
                    }
                    break;
                case 143300674:
                    if (action.equals(Const.SCREEN_RECORDING_DESTROY)) {
                        c = 0;
                        break;
                    }
                    break;
                case 1599260844:
                    if (action.equals(Const.SCREEN_RECORDING_RESUME)) {
                        c = 4;
                        break;
                    }
                    break;
                case 1780700019:
                    if (action.equals(Const.SCREEN_RECORDING_PAUSE)) {
                        c = 3;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                Intent intent = new Intent(this, FloatingControlService.class);
                intent.setAction(Const.SCREEN_RECORDING_DESTROY);
                startService(intent);
            } else if (c == 1) {
                Intent intent2 = new Intent(this, FloatingControlService.class);
                intent2.setAction(Const.SCREEN_RECORDING_START_FROM_NOTIFY);
                startService(intent2);
            } else if (c == 2) {
                Intent intent3 = new Intent(this, RecorderService.class);
                intent3.setAction(Const.SCREEN_RECORDING_STOP);
                startService(intent3);
            } else if (c == 3) {
                Intent intent4 = new Intent(this, RecorderService.class);
                intent4.setAction(Const.SCREEN_RECORDING_PAUSE);
                startService(intent4);
            } else if (c == 4) {
                Log.e(NotificationCompat.CATEGORY_STATUS, "resume click");
                Intent intent5 = new Intent(this, RecorderService.class);
                intent5.setAction(Const.SCREEN_RECORDING_RESUME);
                startService(intent5);
            }
            finish();
        } catch (Exception unused) {
        }
    }
}
