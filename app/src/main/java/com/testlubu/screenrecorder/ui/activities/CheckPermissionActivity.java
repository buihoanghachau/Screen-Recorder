package com.testlubu.screenrecorder.ui.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.testlubu.screenrecorder.common.Const;
import com.testlubu.screenrecorder.services.FloatingControlCameraService;

public class CheckPermissionActivity extends AppCompatActivity {
    /* access modifiers changed from: protected */
    @Override // androidx.core.app.ComponentActivity, androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        if (getIntent() != null && getIntent().getExtras().containsKey("boolean")) {
            requestCameraPermission(getIntent().getExtras().getBoolean("boolean"));
            requestSystemWindowsPermission(Const.CAMERA_SYSTEM_WINDOWS_CODE);
        }
    }

    public void requestCameraPermission(boolean z) {
        if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.CAMERA"}, Const.CAMERA_REQUEST_CODE);
            return;
        }
        Intent intent = new Intent(this, FloatingControlCameraService.class);
        if (!z) {
            stopService(intent);
        } else {
            startService(intent);
        }
        finish();
    }

    @TargetApi(23)
    public void requestSystemWindowsPermission(int i) {
        if (!Settings.canDrawOverlays(this)) {
            startActivityForResult(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + getPackageName())), i);
        }
    }

    @Override // androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback, androidx.fragment.app.FragmentActivity
    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i != 1116) {
            return;
        }
        if (iArr.length <= 0 || iArr[0] == 0) {
            startService(new Intent(this, FloatingControlCameraService.class));
            finish();
            return;
        }
        finish();
    }
}
