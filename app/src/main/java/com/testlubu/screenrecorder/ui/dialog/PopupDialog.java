package com.testlubu.screenrecorder.ui.dialog;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.common.Const;
import com.testlubu.screenrecorder.common.Utils;
import com.testlubu.screenrecorder.ui.activities.EditVideoActivity;
import java.io.File;

public class PopupDialog extends Dialog implements View.OnClickListener {
    static Context mContext;
    private String FILEPATH;
    private Uri fileUri;
    private ImageView imgThumbnail;
    private boolean isVideo = true;

    public PopupDialog(@NonNull Context context) {
        super(context);
    }

    public static PopupDialog newInstance(Context context, String str) {
        mContext = context;
        PopupDialog popupDialog = new PopupDialog(context);
        popupDialog.setFILEPATH(str);
        return popupDialog;
    }

    public void setFILEPATH(String str) {
        this.FILEPATH = str;
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        if (Build.VERSION.SDK_INT < 26) {
            getWindow().setType(2002);
        } else {
            getWindow().setType(2038);
        }
        setContentView(R.layout.dialog_popup);
        setCanceledOnTouchOutside(true);
        initViews();
        initEvents();
        ((LayoutInflater) mContext.getSystemService("layout_inflater")).inflate(R.layout.dialog_popup, (ViewGroup) null);
    }

    private void initEvents() {
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.play).setOnClickListener(this);
        findViewById(R.id.share).setOnClickListener(this);
        findViewById(R.id.edit).setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
        findViewById(R.id.thumbnail).setOnClickListener(this);
    }

    public void onBackPressed() {
        super.onBackPressed();
        cancel();
    }

    private void initViews() {
        Bitmap bitmap;
        try {
            if (this.FILEPATH.endsWith(".mp4")) {
                this.isVideo = true;
                mContext.sendBroadcast(new Intent(Const.UPDATE_UI));
            } else {
                mContext.sendBroadcast(new Intent(Const.UPDATE_UI_IMAGE));
                this.isVideo = false;
                findViewById(R.id.play).setVisibility(8);
                findViewById(R.id.edit).setVisibility(8);
                ((ImageView) findViewById(R.id.thumbnail)).setScaleType(ImageView.ScaleType.CENTER_CROP);
                ((TextView) findViewById(R.id.txt_title)).setText(getContext().getString(R.string.screenshot_finished));
            }
            Context context = getContext();
            this.fileUri = FileProvider.getUriForFile(context, getContext().getPackageName() + ".provider", new File(this.FILEPATH));
            if (this.isVideo) {
                bitmap = Utils.getBitmapVideo(getContext(), new File(this.FILEPATH));
            } else {
                bitmap = BitmapFactory.decodeFile(this.FILEPATH);
            }
            this.imgThumbnail = (ImageView) findViewById(R.id.thumbnail);
            this.imgThumbnail.setImageBitmap(bitmap);
        } catch (Exception unused) {
        }
    }

    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.btn_cancel /*{ENCODED_INT: 2131361867}*/:
                    cancel();
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
                    Intent intent = new Intent(getContext(), EditVideoActivity.class);
                    intent.setFlags(268435456);
                    intent.putExtra(Const.VIDEO_EDIT_URI_KEY, this.FILEPATH);
                    getContext().startActivity(intent);
                    break;
                case R.id.play /*{ENCODED_INT: 2131362086}*/:
                case R.id.thumbnail /*{ENCODED_INT: 2131362197}*/:
                    closeNotify();
                    Intent intent2 = new Intent();
                    intent2.setAction("android.intent.action.VIEW").addFlags(268435457).setDataAndType(this.fileUri, getContext().getContentResolver().getType(this.fileUri));
                    getContext().startActivity(intent2);
                    break;
                case R.id.share /*{ENCODED_INT: 2131362142}*/:
                    getContext().startActivity(new Intent().setAction("android.intent.action.SEND").putExtra("android.intent.extra.STREAM", this.fileUri).setType(this.isVideo ? "video/mp4" : "image"));
                    break;
            }
            cancel();
        } catch (Exception unused2) {
        }
    }

    private void closeNotify() {
        try {
            ((NotificationManager) getContext().getSystemService("notification")).cancel(Const.SCREEN_RECORDER_SHARE_NOTIFICATION_ID);
        } catch (Exception unused) {
        }
    }
}
