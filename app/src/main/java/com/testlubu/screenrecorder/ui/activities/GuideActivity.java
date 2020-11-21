package com.testlubu.screenrecorder.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import com.testlubu.screenrecorder.BaseActivity;
import com.testlubu.screenrecorder.R;

public class GuideActivity extends BaseActivity {
    @Override // androidx.fragment.app.FragmentActivity
    public void onBackPressed() {
    }

    /* access modifiers changed from: protected */
    @Override // androidx.core.app.ComponentActivity, androidx.appcompat.app.AppCompatActivity, com.testlubu.screenrecorder.BaseActivity, androidx.fragment.app.FragmentActivity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_guide);
        findViewById(R.id.na_guide_ok).setOnClickListener(new View.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.activities.GuideActivity.AnonymousClass1 */

            public void onClick(View view) {
                GuideActivity.this.finish();
            }
        });
    }

    private void openAndroidPermissionsMenu() {
        try {
            Intent intent = new Intent("android.settings.action.MANAGE_WRITE_SETTINGS");
            intent.setFlags(268435456);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception unused) {
        }
    }
}
