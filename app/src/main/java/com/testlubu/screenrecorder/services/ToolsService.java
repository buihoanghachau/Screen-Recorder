package com.testlubu.screenrecorder.services;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.common.Const;
import com.testlubu.screenrecorder.ui.activities.CheckPermissionActivity;

public class ToolsService extends Service implements View.OnTouchListener, View.OnClickListener {
    private static final int NOTIFICATION_ID = 161;
    private ConstraintLayout mLayout;
    private NotificationManager mNotificationManager;
    private WindowManager.LayoutParams mParams;
    private SharedPreferences prefs;
    private WindowManager windowManager;

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        return true;
    }

    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint({"WrongConstant"})
    private void initView() {
        this.windowManager = (WindowManager) getApplicationContext().getSystemService("window");
        this.mNotificationManager = (NotificationManager) getSystemService("notification");
        this.mLayout = (ConstraintLayout) ((LayoutInflater) getApplicationContext().getSystemService("layout_inflater")).inflate(R.layout.layout_tools, (ViewGroup) null);
        Switch r1 = (Switch) this.mLayout.findViewById(R.id.sw_capture);
        Switch r2 = (Switch) this.mLayout.findViewById(R.id.sw_camera);
        Switch r3 = (Switch) this.mLayout.findViewById(R.id.sw_brush);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ((ImageView) this.mLayout.findViewById(R.id.imv_close)).setOnClickListener(new View.OnClickListener() {
            /* class com.testlubu.screenrecorder.services.ToolsService.AnonymousClass1 */

            public void onClick(View view) {
                ToolsService.this.stopSelf();
            }
        });
        r1.setChecked(this.prefs.getBoolean(Const.PREFS_TOOLS_CAPTURE, false));
        r2.setChecked(this.prefs.getBoolean(Const.PREFS_TOOLS_CAMERA, false));
        r3.setChecked(this.prefs.getBoolean(Const.PREFS_TOOLS_BRUSH, false));
        r1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /* class com.testlubu.screenrecorder.services.ToolsService.AnonymousClass2 */

            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                ToolsService.this.prefs.edit().putBoolean(Const.PREFS_TOOLS_CAPTURE, z).apply();
                Intent intent = new Intent(ToolsService.this, FloatingControlCaptureService.class);
                if (!z) {
                    ToolsService.this.stopService(intent);
                } else {
                    ToolsService.this.startService(intent);
                }
            }
        });
        r2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /* class com.testlubu.screenrecorder.services.ToolsService.AnonymousClass3 */

            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                ToolsService.this.prefs.edit().putBoolean(Const.PREFS_TOOLS_CAMERA, z).apply();
                Intent intent = new Intent(ToolsService.this, CheckPermissionActivity.class);
                intent.addFlags(268435456);
                intent.putExtra("boolean", z);
                ToolsService.this.startActivity(intent);
                ToolsService.this.stopSelf();
            }
        });
        r3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /* class com.testlubu.screenrecorder.services.ToolsService.AnonymousClass4 */

            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                ToolsService.this.prefs.edit().putBoolean(Const.PREFS_TOOLS_BRUSH, z).apply();
                Intent intent = new Intent(ToolsService.this, FloatingControlBrushService.class);
                if (!z) {
                    ToolsService.this.stopService(intent);
                } else {
                    ToolsService.this.startService(intent);
                }
            }
        });
        this.mParams = new WindowManager.LayoutParams(-1, -1, 2038, 8, -3);
        if (Build.VERSION.SDK_INT < 26) {
            this.mParams.type = 2005;
        }
        this.windowManager.addView(this.mLayout, this.mParams);
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        initView();
        return 2;
    }

    public void onClick(View view) {
        if (view.getId() == R.id.imv_close) {
            stopSelf();
        }
    }

    public void onDestroy() {
        ConstraintLayout constraintLayout;
        WindowManager windowManager2 = this.windowManager;
        if (!(windowManager2 == null || (constraintLayout = this.mLayout) == null)) {
            windowManager2.removeView(constraintLayout);
        }
        super.onDestroy();
    }
}
