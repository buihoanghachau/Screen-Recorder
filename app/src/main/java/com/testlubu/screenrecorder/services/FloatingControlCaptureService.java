package com.testlubu.screenrecorder.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.common.Const;
import com.testlubu.screenrecorder.common.PrefUtils;
import com.testlubu.screenrecorder.ui.activities.ScreenShotActivity;

public class FloatingControlCaptureService extends Service implements View.OnClickListener {
    private final int TIME_DELAY = 2000;
    private IBinder binder = new ServiceBinder();
    private LinearLayout floatingControls;
    private GestureDetector gestureDetector;
    private Handler handler = new Handler();
    private int height;
    private ImageView img;
    private boolean isOverRemoveView;
    private View mRemoveView;
    private int[] overlayViewLocation = {0, 0};
    private WindowManager.LayoutParams params;
    private SharedPreferences prefs;
    public BroadcastReceiver receiverCapture = new BroadcastReceiver() {
        /* class com.testlubu.screenrecorder.services.FloatingControlCaptureService.AnonymousClass4 */

        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                int i = intent.getExtras().getInt("capture");
                if (i == 0) {
                    FloatingControlCaptureService.this.floatingControls.setVisibility(4);
                } else if (i == 1) {
                    FloatingControlCaptureService.this.floatingControls.setVisibility(0);
                }
            }
        }
    };
    private int[] removeViewLocation = {0, 0};
    private Runnable runnable = new Runnable() {
        /* class com.testlubu.screenrecorder.services.FloatingControlCaptureService.AnonymousClass1 */

        public void run() {
            FloatingControlCaptureService.this.setAlphaAssistiveIcon();
        }
    };
    private Vibrator vibrate;
    private int width;
    private WindowManager windowManager;

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPointInArea(int i, int i2, int i3, int i4, int i5) {
        return i >= i3 - i5 && i <= i3 + i5 && i2 >= i4 - i5 && i2 <= i4 + i5;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setAlphaAssistiveIcon() {
        ViewGroup.LayoutParams layoutParams = this.img.getLayoutParams();
        int i = this.width;
        layoutParams.height = i / 10;
        layoutParams.width = i / 10;
        this.img.setImageResource(R.drawable.ic_camera_service);
        this.floatingControls.setAlpha(0.5f);
        this.img.setLayoutParams(layoutParams);
        if (this.params.x < this.width - this.params.x) {
            this.params.x = 0;
        } else {
            this.params.x = this.width;
        }
        this.windowManager.updateViewLayout(this.floatingControls, this.params);
    }

    public void onCreate() {
        super.onCreate();
        this.vibrate = (Vibrator) getSystemService("vibrator");
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.windowManager = (WindowManager) getApplicationContext().getSystemService("window");
        this.floatingControls = (LinearLayout) ((LayoutInflater) getApplicationContext().getSystemService("layout_inflater")).inflate(R.layout.layout_floatbutton_control_capture, (ViewGroup) null);
        this.img = (ImageView) this.floatingControls.findViewById(R.id.imgIcon);
        this.mRemoveView = onGetRemoveView();
        setupRemoveView(this.mRemoveView);
        this.params = new WindowManager.LayoutParams(-2, -2, 2038, 8, -3);
        if (Build.VERSION.SDK_INT < 26) {
            this.params.type = 2005;
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        this.height = displayMetrics.heightPixels;
        this.width = displayMetrics.widthPixels;
        WindowManager.LayoutParams layoutParams = this.params;
        layoutParams.gravity = 8388659;
        layoutParams.x = this.width;
        layoutParams.y = this.height / 2;
        this.gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            /* class com.testlubu.screenrecorder.services.FloatingControlCaptureService.AnonymousClass2 */

            public boolean onSingleTapUp(MotionEvent motionEvent) {
                return true;
            }
        });
        this.floatingControls.setOnTouchListener(new View.OnTouchListener() {
            /* class com.testlubu.screenrecorder.services.FloatingControlCaptureService.AnonymousClass3 */
            private boolean flag = false;
            private float initialTouchX;
            private float initialTouchY;
            private int initialX;
            private int initialY;
            private boolean oneRun = false;
            private WindowManager.LayoutParams paramsF = FloatingControlCaptureService.this.params;

            public boolean onTouch(View view, MotionEvent motionEvent) {
                FloatingControlCaptureService.this.handler.removeCallbacks(FloatingControlCaptureService.this.runnable);
                if (FloatingControlCaptureService.this.gestureDetector.onTouchEvent(motionEvent)) {
                    FloatingControlCaptureService.this.mRemoveView.setVisibility(8);
                    FloatingControlCaptureService.this.handler.removeCallbacks(FloatingControlCaptureService.this.runnable);
                    FloatingControlCaptureService.this.handler.postDelayed(FloatingControlCaptureService.this.runnable, 2000);
                    FloatingControlCaptureService.this.openCapture();
                } else {
                    int action = motionEvent.getAction();
                    if (action == 0) {
                        ViewGroup.LayoutParams layoutParams = FloatingControlCaptureService.this.img.getLayoutParams();
                        layoutParams.height = FloatingControlCaptureService.this.width / 8;
                        layoutParams.width = FloatingControlCaptureService.this.width / 8;
                        FloatingControlCaptureService.this.img.setLayoutParams(layoutParams);
                        FloatingControlCaptureService.this.floatingControls.setAlpha(1.0f);
                        this.initialX = this.paramsF.x;
                        this.initialY = this.paramsF.y;
                        this.initialTouchX = motionEvent.getRawX();
                        this.initialTouchY = motionEvent.getRawY();
                        this.flag = true;
                    } else if (action == 1) {
                        this.flag = false;
                        if (FloatingControlCaptureService.this.params.x < FloatingControlCaptureService.this.width - FloatingControlCaptureService.this.params.x) {
                            FloatingControlCaptureService.this.params.x = 0;
                        } else {
                            FloatingControlCaptureService.this.params.x = FloatingControlCaptureService.this.width - FloatingControlCaptureService.this.floatingControls.getWidth();
                        }
                        if (FloatingControlCaptureService.this.isOverRemoveView) {
                            FloatingControlCaptureService.this.prefs.edit().putBoolean(Const.PREFS_TOOLS_CAPTURE, false).apply();
                            FloatingControlCaptureService.this.stopSelf();
                        } else {
                            FloatingControlCaptureService.this.windowManager.updateViewLayout(FloatingControlCaptureService.this.floatingControls, FloatingControlCaptureService.this.params);
                            FloatingControlCaptureService.this.handler.postDelayed(FloatingControlCaptureService.this.runnable, 2000);
                        }
                        FloatingControlCaptureService.this.mRemoveView.setVisibility(8);
                    } else if (action == 2) {
                        WindowManager.LayoutParams layoutParams2 = this.paramsF;
                        layoutParams2.x = this.initialX + ((int) (motionEvent.getRawX() - this.initialTouchX));
                        layoutParams2.y = this.initialY + ((int) (motionEvent.getRawY() - this.initialTouchY));
                        if (this.flag) {
                            FloatingControlCaptureService.this.mRemoveView.setVisibility(0);
                        }
                        FloatingControlCaptureService.this.windowManager.updateViewLayout(FloatingControlCaptureService.this.floatingControls, this.paramsF);
                        FloatingControlCaptureService.this.floatingControls.getLocationOnScreen(FloatingControlCaptureService.this.overlayViewLocation);
                        FloatingControlCaptureService.this.mRemoveView.getLocationOnScreen(FloatingControlCaptureService.this.removeViewLocation);
                        FloatingControlCaptureService floatingControlCaptureService = FloatingControlCaptureService.this;
                        floatingControlCaptureService.isOverRemoveView = floatingControlCaptureService.isPointInArea(floatingControlCaptureService.overlayViewLocation[0], FloatingControlCaptureService.this.overlayViewLocation[1], FloatingControlCaptureService.this.removeViewLocation[0], FloatingControlCaptureService.this.removeViewLocation[1], FloatingControlCaptureService.this.mRemoveView.getWidth());
                        if (FloatingControlCaptureService.this.isOverRemoveView) {
                            if (this.oneRun) {
                                if (Build.VERSION.SDK_INT < 26) {
                                    FloatingControlCaptureService.this.vibrate.vibrate(200);
                                } else {
                                    FloatingControlCaptureService.this.vibrate.vibrate(VibrationEffect.createOneShot(200, 255));
                                }
                            }
                            this.oneRun = false;
                        } else {
                            this.oneRun = true;
                        }
                    } else if (action == 3) {
                        FloatingControlCaptureService.this.mRemoveView.setVisibility(8);
                    }
                }
                return false;
            }
        });
        addBubbleView();
        this.handler.postDelayed(this.runnable, 2000);
        registerReceiver(this.receiverCapture, new IntentFilter(Const.ACTION_SCREEN_SHOT));
    }

    private void setupRemoveView(View view) {
        view.setVisibility(8);
        this.windowManager.addView(view, newWindowManagerLayoutParamsForRemoveView());
    }

    private static WindowManager.LayoutParams newWindowManagerLayoutParamsForRemoveView() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-2, -2, Build.VERSION.SDK_INT < 26 ? 2002 : 2038, 262664, -3);
        layoutParams.gravity = 81;
        layoutParams.y = 56;
        return layoutParams;
    }

    /* access modifiers changed from: protected */
    @SuppressLint({"InflateParams"})
    public View onGetRemoveView() {
        return LayoutInflater.from(this).inflate(R.layout.overlay_remove_view, (ViewGroup) null);
    }

    @SuppressLint({"WrongConstant"})
    public int onStartCommand(Intent intent, int i, int i2) {
        ViewGroup.LayoutParams layoutParams = this.img.getLayoutParams();
        int i3 = this.width;
        layoutParams.height = i3 / 8;
        layoutParams.width = i3 / 8;
        this.img.setLayoutParams(layoutParams);
        this.floatingControls.setAlpha(1.0f);
        return super.onStartCommand(intent, i, i2);
    }

    public void addBubbleView() {
        LinearLayout linearLayout;
        WindowManager windowManager2 = this.windowManager;
        if (windowManager2 != null && (linearLayout = this.floatingControls) != null) {
            windowManager2.addView(linearLayout, this.params);
        }
    }

    public void removeBubbleView() {
        LinearLayout linearLayout;
        WindowManager windowManager2 = this.windowManager;
        if (windowManager2 != null && (linearLayout = this.floatingControls) != null) {
            windowManager2.removeView(linearLayout);
        }
    }

    public void onClick(View view) {
        view.getId();
        if (PrefUtils.readBooleanValue(this, getString(R.string.preference_vibrate_key), true)) {
            ((Vibrator) getSystemService("vibrator")).vibrate(100);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void openCapture() {
        Intent intent = new Intent(this, ScreenShotActivity.class);
        intent.setFlags(268435456);
        startActivity(intent);
    }

    public void onDestroy() {
        View view;
        removeBubbleView();
        unregisterReceiver(this.receiverCapture);
        WindowManager windowManager2 = this.windowManager;
        if (!(windowManager2 == null || (view = this.mRemoveView) == null)) {
            windowManager2.removeView(view);
        }
        super.onDestroy();
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        Log.d(Const.TAG, "Binding successful!");
        return this.binder;
    }

    public class ServiceBinder extends Binder {
        public ServiceBinder() {
        }

        /* access modifiers changed from: package-private */
        public FloatingControlCaptureService getService() {
            return FloatingControlCaptureService.this;
        }
    }
}
