package com.testlubu.screenrecorder.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.common.Const;

public class FloatingCameraViewServiceTest extends Service implements View.OnClickListener {
    private static FloatingCameraViewServiceTest context;
    private IBinder binder = new ServiceBinder();
    ImageView btnOptions;
    private Handler handler = new Handler();
    private boolean isCameraViewHidden;
    private View mCurrentView;
    private ConstraintLayout mFloatingView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams params;
    private SharedPreferences prefs;
    ConstraintLayout viewOptions;

    public FloatingCameraViewServiceTest() {
        context = this;
    }

    public IBinder onBind(Intent intent) {
        Log.d(Const.TAG, "Binding successful!");
        return this.binder;
    }

    public boolean onUnbind(Intent intent) {
        Log.d(Const.TAG, "Unbinding and stopping service");
        stopSelf();
        return super.onUnbind(intent);
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        try {
            this.mFloatingView = (ConstraintLayout) ((LayoutInflater) getSystemService("layout_inflater")).inflate(R.layout.layout_floatbutton, (ViewGroup) null);
            this.btnOptions = (ImageView) this.mFloatingView.findViewById(R.id.imgIcon);
            this.btnOptions.setOnClickListener(new View.OnClickListener() {
                /* class com.testlubu.screenrecorder.services.FloatingCameraViewServiceTest.AnonymousClass1 */

                public void onClick(View view) {
                    if (FloatingCameraViewServiceTest.this.viewOptions.isShown()) {
                        FloatingCameraViewServiceTest.this.viewOptions.setVisibility(4);
                    } else {
                        FloatingCameraViewServiceTest.this.viewOptions.setVisibility(0);
                    }
                }
            });
            this.mCurrentView = this.mFloatingView;
            int xPos = getXPos();
            int yPos = getYPos();
            this.params = new WindowManager.LayoutParams(-2, -2, Build.VERSION.SDK_INT < 26 ? 2005 : 2038, 8, -3);
            this.params.gravity = 8388659;
            this.params.x = xPos;
            this.params.y = yPos;
            this.mWindowManager = (WindowManager) getSystemService("window");
            this.mWindowManager.addView(this.mCurrentView, this.params);
            setupDragListener();
            return 1;
        } catch (Exception e) {
            e.getMessage();
            return 1;
        }
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    private void setupDragListener() {
        try {
            this.mCurrentView.setOnTouchListener(new View.OnTouchListener() {
                private float initialTouchX;
                private float initialTouchY;
                private int initialX;
                private int initialY;
                boolean isMoving = false;
                private WindowManager.LayoutParams paramsF = FloatingCameraViewServiceTest.this.params;

                public boolean onTouch(final View view, final MotionEvent motionEvent) {
                    try {
                        final int action = motionEvent.getAction();
                        if (action != 0) {
                            if (action != 1) {
                                if (action == 2) {
                                    final int a = (int)(motionEvent.getRawX() - this.initialTouchX);
                                    final int a2 = (int)(motionEvent.getRawY() - this.initialTouchY);
                                    this.paramsF.x = this.initialX + a;
                                    this.paramsF.y = this.initialY + a2;
                                    if (Math.abs(a) > 10 || Math.abs(a2) > 10) {
                                        this.isMoving = true;
                                    }
                                    FloatingCameraViewServiceTest.this.mWindowManager.updateViewLayout(FloatingCameraViewServiceTest.this.mCurrentView, this.paramsF);
                                    FloatingCameraViewServiceTest.this.persistCoordinates(this.initialX + a, this.initialY + a2);
                                    return true;
                                }
                            }
                            return false;
                        }
                        this.isMoving = false;
                        this.initialX = this.paramsF.x;
                        this.initialY = this.paramsF.y;
                        this.initialTouchX = motionEvent.getRawX();
                        this.initialTouchY = motionEvent.getRawY();
                        return true;
                    }
                    catch (Exception ex) {
                        return false;
                    }
                }
            });
        }
        catch (Exception ex) {}
    }
    private int getXPos() {
        return Integer.parseInt(getDefaultPrefs().getString(Const.PREFS_CAMERA_OVERLAY_POS, "0X100").split("X")[0]);
    }

    private int getYPos() {
        return Integer.parseInt(getDefaultPrefs().getString(Const.PREFS_CAMERA_OVERLAY_POS, "0X100").split("X")[1]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void persistCoordinates(int i, int i2) {
        SharedPreferences.Editor edit = getDefaultPrefs().edit();
        edit.putString(Const.PREFS_CAMERA_OVERLAY_POS, String.valueOf(i) + "X" + String.valueOf(i2)).apply();
    }

    private SharedPreferences getDefaultPrefs() {
        if (this.prefs == null) {
            this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        }
        return this.prefs;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void onClick(View view) {
        try {
            view.getId();
        } catch (Exception unused) {
        }
    }

    public class ServiceBinder extends Binder {
        public ServiceBinder() {
        }

        /* access modifiers changed from: package-private */
        public FloatingCameraViewServiceTest getService() {
            return FloatingCameraViewServiceTest.this;
        }
    }
}
