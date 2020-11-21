package com.testlubu.screenrecorder.ui.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import androidx.appcompat.widget.Toolbar;
import com.testlubu.screenrecorder.BaseActivity;
import com.testlubu.screenrecorder.R;

public class ShowTouchTutsActivity extends BaseActivity {
    private WebView webView;

    /* access modifiers changed from: protected */
    @Override // androidx.core.app.ComponentActivity, androidx.appcompat.app.AppCompatActivity, com.testlubu.screenrecorder.BaseActivity, androidx.fragment.app.FragmentActivity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_show_touch_tuts);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        setTitle(getString(R.string.title_show_touch_activity));
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.webView = (WebView) findViewById(R.id.webView);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.loadUrl("file:///android_asset/tuts_permission_show_touch.html");
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == 16908332) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
