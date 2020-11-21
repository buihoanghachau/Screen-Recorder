package com.testlubu.screenrecorder.ui.activities;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.testlubu.screenrecorder.BaseActivity;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.common.Const;
import com.testlubu.screenrecorder.listener.ObserverUtils;
import com.testlubu.screenrecorder.model.listener.EvbStopService;
import com.testlubu.screenrecorder.services.FloatingControlService;
import com.testlubu.screenrecorder.services.RecorderService;

public class RequestRecorderActivity extends BaseActivity {
    private MediaProjectionManager mProjectionManager;

    /* access modifiers changed from: protected */
    @Override // androidx.core.app.ComponentActivity, androidx.appcompat.app.AppCompatActivity, com.testlubu.screenrecorder.BaseActivity, androidx.fragment.app.FragmentActivity
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        this.mProjectionManager = (MediaProjectionManager) getSystemService("media_projection");
        startActivityForResult(this.mProjectionManager.createScreenCaptureIntent(), Const.SCREEN_RECORD_REQUEST_CODE);
    }

    /* access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i2 == 0 && i == 1113) {
            ObserverUtils.getInstance().notifyObservers(new EvbStopService());
            Toast.makeText(this, getString(R.string.screen_recording_permission_denied), 0).show();
            finish();
            return;
        }
        FloatingControlService.isRecording = true;
        Intent intent2 = new Intent(this, RecorderService.class);
        intent2.setAction(Const.SCREEN_RECORDING_START);
        intent2.putExtra(Const.RECORDER_INTENT_DATA, intent);
        intent2.putExtra(Const.RECORDER_INTENT_RESULT, i2);
        startService(intent2);
        finish();
    }
}
