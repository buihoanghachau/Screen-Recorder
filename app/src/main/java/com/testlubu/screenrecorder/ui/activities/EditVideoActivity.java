package com.testlubu.screenrecorder.ui.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.testlubu.screenrecorder.BaseActivity;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.common.Const;
import com.testlubu.screenrecorder.videoTrimmer.K4LVideoTrimmer;
import com.testlubu.screenrecorder.videoTrimmer.interfaces.OnTrimVideoListener;
import java.io.File;

public class EditVideoActivity extends BaseActivity implements OnTrimVideoListener {
    private ProgressDialog saveprogress;
    private Uri videoUri;

    @Override // com.testlubu.screenrecorder.videoTrimmer.interfaces.OnTrimVideoListener
    public void onError(String str) {
    }

    @Override // com.testlubu.screenrecorder.videoTrimmer.interfaces.OnTrimVideoListener
    public void onTrimStarted() {
    }

    /* access modifiers changed from: protected */
    @Override // androidx.core.app.ComponentActivity, androidx.appcompat.app.AppCompatActivity, com.testlubu.screenrecorder.BaseActivity, androidx.fragment.app.FragmentActivity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_edit_video);
        this.saveprogress = new ProgressDialog(this);
        try {
            if (!getIntent().hasExtra(Const.VIDEO_EDIT_URI_KEY)) {
                Toast.makeText(this, getResources().getString(R.string.video_not_found), 0).show();
                finish();
                return;
            }
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            StringBuilder sb = new StringBuilder();
            String string = getString(R.string.savelocation_key);
            sb.append(defaultSharedPreferences.getString(string, Environment.getExternalStorageDirectory() + File.separator + Const.APPDIR));
            sb.append(File.separator);
            new File(sb.toString());
            this.videoUri = Uri.parse(getIntent().getStringExtra(Const.VIDEO_EDIT_URI_KEY));
            if (!new File(this.videoUri.getPath()).exists()) {
                Toast.makeText(this, getResources().getString(R.string.video_not_found), 0).show();
                finish();
                return;
            }
            K4LVideoTrimmer k4LVideoTrimmer = (K4LVideoTrimmer) findViewById(R.id.videoTimeLine);
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(this, this.videoUri);
            int parseLong = (((int) Long.parseLong(mediaMetadataRetriever.extractMetadata(9))) / 1000) + 1000;
            Log.d(Const.TAG, parseLong + "");
            File file = new File(this.videoUri.getPath());
            k4LVideoTrimmer.setOnTrimVideoListener(this);
            k4LVideoTrimmer.setVideoURI(this.videoUri);
            k4LVideoTrimmer.setMaxDuration(parseLong);
            Log.d(Const.TAG, "Edited file save name: " + file.getAbsolutePath());
            String string2 = getString(R.string.savelocation_key);
            k4LVideoTrimmer.setDestinationPath(defaultSharedPreferences.getString(string2, Environment.getExternalStorageDirectory() + File.separator + Const.APPDIR));
        } catch (Exception unused) {
        }
    }

    @Override // com.testlubu.screenrecorder.videoTrimmer.interfaces.OnTrimVideoListener
    public void getResult(Uri uri) {
        try {
            Log.d(Const.TAG, "Test link ne: " + uri.getPath());
            indexFile(uri.getPath());
            runOnUiThread(new Runnable() {
                /* class com.testlubu.screenrecorder.ui.activities.EditVideoActivity.AnonymousClass1 */

                public void run() {
                    EditVideoActivity.this.saveprogress.setMessage("Please wait while the video is being saved");
                    EditVideoActivity.this.saveprogress.setTitle("Please wait");
                    EditVideoActivity.this.saveprogress.setIndeterminate(true);
                    EditVideoActivity.this.saveprogress.show();
                }
            });
        } catch (Exception unused) {
        }
    }

    private void showActionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_title_delete_old_file));
        builder.setPositiveButton(getString(17039379), new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.activities.EditVideoActivity.AnonymousClass2 */

            public void onClick(DialogInterface dialogInterface, int i) {
                File file = new File(EditVideoActivity.this.videoUri.getPath());
                if (file.exists()) {
                    file.delete();
                }
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(getString(17039369), new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.activities.EditVideoActivity.AnonymousClass3 */

            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.setCanceledOnTouchOutside(false);
        create.setOnDismissListener(new DialogInterface.OnDismissListener() {
            /* class com.testlubu.screenrecorder.ui.activities.EditVideoActivity.AnonymousClass4 */

            public void onDismiss(DialogInterface dialogInterface) {
                EditVideoActivity.this.finish();
            }
        });
        create.show();
    }

    @Override // com.testlubu.screenrecorder.videoTrimmer.interfaces.OnTrimVideoListener
    public void cancelAction() {
        finish();
    }

    private void indexFile(String str) {
        File file = new File(str);
        if (Build.VERSION.SDK_INT >= 19) {
            Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
            intent.setData(Uri.fromFile(file));
            sendBroadcast(intent);
        } else {
            sendBroadcast(new Intent("android.intent.action.MEDIA_MOUNTED", Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
        this.saveprogress.cancel();
        setResult(Const.VIDEO_EDIT_RESULT_CODE);
        finish();
    }
}
