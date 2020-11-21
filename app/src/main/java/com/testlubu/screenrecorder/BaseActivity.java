package com.testlubu.screenrecorder;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    /* access modifiers changed from: protected */
    @Override // androidx.core.app.ComponentActivity, androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        RecorderApplication.getInstance().doForCreate(this);
    }

    /* access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity
    public void onDestroy() {
        super.onDestroy();
        RecorderApplication.getInstance().doForFinish(this);
    }

    public final void clear() {
        super.finish();
    }
}
