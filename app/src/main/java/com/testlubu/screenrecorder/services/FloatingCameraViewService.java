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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import com.master.cameralibrary.CameraView;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.common.Const;

public class FloatingCameraViewService extends Service implements View.OnClickListener {
    private static FloatingCameraViewService context;
    private IBinder binder = new ServiceBinder();
    private CameraView cameraView;
    private Handler handler = new Handler();
    private ImageButton hideCameraBtn;
    private boolean isCameraViewHidden;
    private View mCurrentView;
    private LinearLayout mFloatingView;
    private WindowManager mWindowManager;
    private OverlayResize overlayResize = OverlayResize.MINWINDOW;
    private WindowManager.LayoutParams params;
    private SharedPreferences prefs;
    private ImageButton resizeOverlay;
    private Runnable runnable = new Runnable() {
        /* class com.testlubu.screenrecorder.services.FloatingCameraViewService.AnonymousClass1 */

        public void run() {
            FloatingCameraViewService.this.resizeOverlay.setVisibility(8);
            FloatingCameraViewService.this.hideCameraBtn.setVisibility(8);
            FloatingCameraViewService.this.switchCameraBtn.setVisibility(8);
        }
    };
    private ImageButton switchCameraBtn;
    private Values values;

    /* access modifiers changed from: private */
    public enum OverlayResize {
        MAXWINDOW,
        MINWINDOW
    }

    public FloatingCameraViewService() {
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
        this.mFloatingView = (LinearLayout) ((LayoutInflater) getSystemService("layout_inflater")).inflate(R.layout.layout_floating_camera_view, (ViewGroup) null);
        this.cameraView = (CameraView) this.mFloatingView.findViewById(R.id.cameraView);
        this.hideCameraBtn = (ImageButton) this.mFloatingView.findViewById(R.id.hide_camera);
        this.switchCameraBtn = (ImageButton) this.mFloatingView.findViewById(R.id.switch_camera);
        this.resizeOverlay = (ImageButton) this.mFloatingView.findViewById(R.id.overlayResize);
        this.values = new Values();
        this.hideCameraBtn.setOnClickListener(this);
        this.switchCameraBtn.setOnClickListener(this);
        this.resizeOverlay.setOnClickListener(this);
        this.mCurrentView = this.mFloatingView;
        int xPos = getXPos();
        int yPos = getYPos();
        this.params = new WindowManager.LayoutParams(this.values.smallCameraX, this.values.smallCameraY, Build.VERSION.SDK_INT < 26 ? 2005 : 2038, 8, -3);
        WindowManager.LayoutParams layoutParams = this.params;
        layoutParams.gravity = 8388659;
        layoutParams.x = xPos;
        layoutParams.y = yPos;
        this.mWindowManager = (WindowManager) getSystemService("window");
        this.mWindowManager.addView(this.mCurrentView, this.params);
        this.cameraView.start();
        setupDragListener();
        return 1;
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        changeCameraOrientation();
    }

    private void changeCameraOrientation() {
        this.values.buildValues();
        int i = this.overlayResize == OverlayResize.MAXWINDOW ? this.values.bigCameraX : this.values.smallCameraX;
        int i2 = this.overlayResize == OverlayResize.MAXWINDOW ? this.values.bigCameraY : this.values.smallCameraY;
        if (!this.isCameraViewHidden) {
            WindowManager.LayoutParams layoutParams = this.params;
            layoutParams.height = i2;
            layoutParams.width = i;
            this.mWindowManager.updateViewLayout(this.mCurrentView, layoutParams);
        }
    }

    private void setupDragListener() {
        this.mCurrentView.setOnTouchListener(new View.OnTouchListener() {
            /* class com.testlubu.screenrecorder.services.FloatingCameraViewService.AnonymousClass2 */
            private float initialTouchX;
            private float initialTouchY;
            private int initialX;
            private int initialY;
            boolean isMoving = false;
            private WindowManager.LayoutParams paramsF = FloatingCameraViewService.this.params;

            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if (action != 0) {
                    if (action == 1) {
                        FloatingCameraViewService.this.handler.postDelayed(FloatingCameraViewService.this.runnable, 3000);
                    } else if (action == 2) {
                        int rawX = (int) (motionEvent.getRawX() - this.initialTouchX);
                        int rawY = (int) (motionEvent.getRawY() - this.initialTouchY);
                        WindowManager.LayoutParams layoutParams = this.paramsF;
                        layoutParams.x = this.initialX + rawX;
                        layoutParams.y = this.initialY + rawY;
                        if (Math.abs(rawX) > 10 || Math.abs(rawY) > 10) {
                            this.isMoving = true;
                        }
                        FloatingCameraViewService.this.mWindowManager.updateViewLayout(FloatingCameraViewService.this.mCurrentView, this.paramsF);
                        FloatingCameraViewService.this.persistCoordinates(this.initialX + rawX, this.initialY + rawY);
                        return true;
                    }
                    return false;
                }
                if (FloatingCameraViewService.this.resizeOverlay.isShown()) {
                    FloatingCameraViewService.this.resizeOverlay.setVisibility(8);
                    FloatingCameraViewService.this.hideCameraBtn.setVisibility(8);
                    FloatingCameraViewService.this.switchCameraBtn.setVisibility(8);
                } else {
                    FloatingCameraViewService.this.resizeOverlay.setVisibility(0);
                    FloatingCameraViewService.this.hideCameraBtn.setVisibility(0);
                    FloatingCameraViewService.this.switchCameraBtn.setVisibility(0);
                    FloatingCameraViewService.this.handler.removeCallbacks(FloatingCameraViewService.this.runnable);
                }
                this.isMoving = false;
                this.initialX = this.paramsF.x;
                this.initialY = this.paramsF.y;
                this.initialTouchX = motionEvent.getRawX();
                this.initialTouchY = motionEvent.getRawY();
                return true;
            }
        });
        this.resizeOverlay.setOnTouchListener(new View.OnTouchListener() {
            /* class com.testlubu.screenrecorder.services.FloatingCameraViewService.AnonymousClass3 */
            private float initialTouchX;
            private float initialTouchY;
            private int initialX;
            private int initialY;

            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if (action == 0) {
                    this.initialX = FloatingCameraViewService.this.params.width;
                    this.initialY = FloatingCameraViewService.this.params.height;
                    this.initialTouchX = motionEvent.getRawX();
                    this.initialTouchY = motionEvent.getRawY();
                    return true;
                } else if (action == 1) {
                    FloatingCameraViewService.this.handler.postDelayed(FloatingCameraViewService.this.runnable, 3000);
                    return false;
                } else if (action != 2) {
                    return false;
                } else {
                    if (FloatingCameraViewService.this.resizeOverlay.isShown()) {
                        FloatingCameraViewService.this.handler.removeCallbacks(FloatingCameraViewService.this.runnable);
                    }
                    FloatingCameraViewService.this.params.width = this.initialX + ((int) (motionEvent.getRawX() - this.initialTouchX));
                    FloatingCameraViewService.this.params.height = this.initialY + ((int) (motionEvent.getRawY() - this.initialTouchY));
                    FloatingCameraViewService.this.mWindowManager.updateViewLayout(FloatingCameraViewService.this.mCurrentView, FloatingCameraViewService.this.params);
                    return true;
                }
            }
        });
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
        this.prefs.edit().putBoolean(Const.PREFS_TOOLS_CAMERA, false).apply();
        if (this.mFloatingView != null) {
            this.handler.removeCallbacks(this.runnable);
            this.mWindowManager.removeView(this.mCurrentView);
            this.cameraView.stop();
        }
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.hide_camera) {
            Log.d(Const.TAG, "hide camera");
            if (this.mCurrentView.equals(this.mFloatingView)) {
                this.mWindowManager.removeViewImmediate(this.mCurrentView);
                this.cameraView.stop();
                this.mFloatingView = null;
            }
            this.prefs.edit().putBoolean(Const.PREFS_TOOLS_CAMERA, false).apply();
            setupDragListener();
        } else if (id == R.id.switch_camera) {
            if (this.cameraView.getFacing() == 0) {
                this.cameraView.setFacing(1);
                this.cameraView.setAutoFocus(true);
                return;
            }
            this.cameraView.setFacing(0);
            this.cameraView.setAutoFocus(true);
        }
    }

    private void showCameraView() {
        this.mWindowManager.removeViewImmediate(this.mCurrentView);
        this.mCurrentView = this.mFloatingView;
        this.mWindowManager.addView(this.mCurrentView, this.params);
        this.isCameraViewHidden = false;
        setupDragListener();
    }

    /* access modifiers changed from: private */
    public class Values {
        int bigCameraX;
        int bigCameraY;
        int cameraHideX = dpToPx(60);
        int cameraHideY = dpToPx(60);
        int smallCameraX;
        int smallCameraY;

        public Values() {
            buildValues();
        }

        private int dpToPx(int i) {
            return Math.round(((float) i) * (FloatingCameraViewService.this.getResources().getDisplayMetrics().xdpi / 160.0f));
        }

        /* access modifiers changed from: package-private */
        public void buildValues() {
            if (FloatingCameraViewService.context.getResources().getConfiguration().orientation == 2) {
                this.smallCameraX = dpToPx(160);
                this.smallCameraY = dpToPx(120);
                this.bigCameraX = dpToPx(ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION);
                this.bigCameraY = dpToPx(150);
                return;
            }
            this.smallCameraX = dpToPx(120);
            this.smallCameraY = dpToPx(160);
            this.bigCameraX = dpToPx(150);
            this.bigCameraY = dpToPx(ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION);
        }
    }

    public class ServiceBinder extends Binder {
        public ServiceBinder() {
        }

        /* access modifiers changed from: package-private */
        public FloatingCameraViewService getService() {
            return FloatingCameraViewService.this;
        }
    }
}
