package com.testlubu.screenrecorder.ui.activities;

import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.FileProvider;
//import com.ads.control.AdmobHelp;
import com.testlubu.screenrecorder.BaseActivity;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.common.Const;
import com.testlubu.screenrecorder.common.Utils;
import java.io.File;

public class DialogResultActivity extends BaseActivity implements View.OnClickListener {
    private String FILEPATH;
    private Uri fileUri;
    private ImageView imgThumbnail;
    private boolean isVideo = true;

    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.btn_cancel /*{ENCODED_INT: 2131361867}*/:
                    finish();
                    break;
                case R.id.delete /*{ENCODED_INT: 2131361912}*/:
                    try {
                        new File(this.FILEPATH).delete();
                        closeNotify();
                        break;
                    } catch (Exception unused) {
                        break;
                    }
                case R.id.edit /*{ENCODED_INT: 2131361927}*/:
                    closeNotify();
                    Intent intent = new Intent(this, EditVideoActivity.class);
                    intent.setFlags(268435456);
                    intent.putExtra(Const.VIDEO_EDIT_URI_KEY, this.FILEPATH);
                    startActivity(intent);
                    break;
                case R.id.play /*{ENCODED_INT: 2131362086}*/:
                case R.id.thumbnail /*{ENCODED_INT: 2131362197}*/:
                    closeNotify();
                    Intent intent2 = new Intent();
                    intent2.setAction("android.intent.action.VIEW").addFlags(268435457).setDataAndType(this.fileUri, getContentResolver().getType(this.fileUri));
                    startActivity(intent2);
                    break;
                case R.id.share /*{ENCODED_INT: 2131362142}*/:
                    Intent type = new Intent().setAction("android.intent.action.SEND").putExtra("android.intent.extra.STREAM", this.fileUri).setType(this.isVideo ? "video/mp4" : "image/*");
                    type.addFlags(268435456);
                    startActivity(type);
                    break;
            }
            finish();
        } catch (Exception unused2) {
        }
    }

    /* access modifiers changed from: protected */
    @Override // androidx.core.app.ComponentActivity, androidx.appcompat.app.AppCompatActivity, com.testlubu.screenrecorder.BaseActivity, androidx.fragment.app.FragmentActivity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.dialog_popup);
        intData();
        intView();
        intEvent();
//        AdmobHelp.getInstance().loadNative(this);
    }

    public void intView() {
        Bitmap bitmap;
        try {
            if (this.FILEPATH.endsWith(".mp4")) {
                this.isVideo = true;
                sendBroadcast(new Intent(Const.UPDATE_UI));
            } else {
                sendBroadcast(new Intent(Const.UPDATE_UI_IMAGE));
                this.isVideo = false;
                findViewById(R.id.play).setVisibility(8);
                findViewById(R.id.edit).setVisibility(8);
                ((ImageView) findViewById(R.id.thumbnail)).setScaleType(ImageView.ScaleType.CENTER_CROP);
                ((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.screenshot_finished));
            }
            this.fileUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", new File(this.FILEPATH));
            if (this.isVideo) {
                bitmap = Utils.getBitmapVideo(this, new File(this.FILEPATH));
            } else {
                bitmap = BitmapFactory.decodeFile(this.FILEPATH);
            }
            this.imgThumbnail = (ImageView) findViewById(R.id.thumbnail);
            this.imgThumbnail.setImageBitmap(bitmap);
        } catch (Exception unused) {
        }
    }

    public void intData() {
        this.FILEPATH = getIntent().getStringExtra("path");
    }

    public void intEvent() {
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.play).setOnClickListener(this);
        findViewById(R.id.share).setOnClickListener(this);
        findViewById(R.id.edit).setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
        findViewById(R.id.thumbnail).setOnClickListener(this);
    }

    private void closeNotify() {
        try {
            ((NotificationManager) getSystemService("notification")).cancel(Const.SCREEN_RECORDER_SHARE_NOTIFICATION_ID);
        } catch (Exception unused) {
        }
    }
}
