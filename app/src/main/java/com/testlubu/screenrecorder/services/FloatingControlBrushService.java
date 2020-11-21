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

public class FloatingControlBrushService extends Service implements View.OnClickListener {
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
        /* class com.testlubu.screenrecorder.services.FloatingControlBrushService.AnonymousClass4 */

        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                int i = intent.getExtras().getInt("capture");
                if (i == 0) {
                    FloatingControlBrushService.this.floatingControls.setVisibility(4);
                } else if (i == 1) {
                    FloatingControlBrushService.this.floatingControls.setVisibility(0);
                }
            }
        }
    };
    private int[] removeViewLocation = {0, 0};
    private Runnable runnable = new Runnable() {
        /* class com.testlubu.screenrecorder.services.FloatingControlBrushService.AnonymousClass1 */

        public void run() {
            FloatingControlBrushService.this.setAlphaAssistiveIcon();
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
        this.img.setImageResource(R.drawable.ic_brush_service);
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
        this.floatingControls = (LinearLayout) ((LayoutInflater) getApplicationContext().getSystemService("layout_inflater")).inflate(R.layout.layout_floatbutton_control_brush, (ViewGroup) null);
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
        layoutParams.y = this.height / 4;
        this.gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            /* class com.testlubu.screenrecorder.services.FloatingControlBrushService.AnonymousClass2 */

            public boolean onSingleTapUp(MotionEvent motionEvent) {
                return true;
            }
        });
        this.floatingControls.setOnTouchListener(new View.OnTouchListener() {
            /* class com.testlubu.screenrecorder.services.FloatingControlBrushService.AnonymousClass3 */
            private boolean flag = false;
            private float initialTouchX;
            private float initialTouchY;
            private int initialX;
            private int initialY;
            private boolean oneRun = false;
            private WindowManager.LayoutParams paramsF = FloatingControlBrushService.this.params;

            public boolean onTouch(View view, MotionEvent motionEvent) {
                FloatingControlBrushService.this.handler.removeCallbacks(FloatingControlBrushService.this.runnable);
                if (FloatingControlBrushService.this.gestureDetector.onTouchEvent(motionEvent)) {
                    FloatingControlBrushService.this.mRemoveView.setVisibility(8);
                    FloatingControlBrushService.this.handler.removeCallbacks(FloatingControlBrushService.this.runnable);
                    FloatingControlBrushService.this.handler.postDelayed(FloatingControlBrushService.this.runnable, 2000);
                    FloatingControlBrushService.this.openBrsuh();
                } else {
                    int action = motionEvent.getAction();
                    if (action == 0) {
                        ViewGroup.LayoutParams layoutParams = FloatingControlBrushService.this.img.getLayoutParams();
                        layoutParams.height = FloatingControlBrushService.this.width / 8;
                        layoutParams.width = FloatingControlBrushService.this.width / 8;
                        FloatingControlBrushService.this.img.setLayoutParams(layoutParams);
                        FloatingControlBrushService.this.floatingControls.setAlpha(1.0f);
                        this.initialX = this.paramsF.x;
                        this.initialY = this.paramsF.y;
                        this.initialTouchX = motionEvent.getRawX();
                        this.initialTouchY = motionEvent.getRawY();
                        this.flag = true;
                    } else if (action == 1) {
                        this.flag = false;
                        if (FloatingControlBrushService.this.params.x < FloatingControlBrushService.this.width - FloatingControlBrushService.this.params.x) {
                            FloatingControlBrushService.this.params.x = 0;
                        } else {
                            FloatingControlBrushService.this.params.x = FloatingControlBrushService.this.width - FloatingControlBrushService.this.floatingControls.getWidth();
                        }
                        if (FloatingControlBrushService.this.isOverRemoveView) {
                            FloatingControlBrushService.this.prefs.edit().putBoolean(Const.PREFS_TOOLS_BRUSH, false).apply();
                            FloatingControlBrushService.this.stopSelf();
                        } else {
                            FloatingControlBrushService.this.windowManager.updateViewLayout(FloatingControlBrushService.this.floatingControls, FloatingControlBrushService.this.params);
                            FloatingControlBrushService.this.handler.postDelayed(FloatingControlBrushService.this.runnable, 2000);
                        }
                        FloatingControlBrushService.this.mRemoveView.setVisibility(8);
                    } else if (action == 2) {
                        WindowManager.LayoutParams layoutParams2 = this.paramsF;
                        layoutParams2.x = this.initialX + ((int) (motionEvent.getRawX() - this.initialTouchX));
                        layoutParams2.y = this.initialY + ((int) (motionEvent.getRawY() - this.initialTouchY));
                        if (this.flag) {
                            FloatingControlBrushService.this.mRemoveView.setVisibility(0);
                        }
                        FloatingControlBrushService.this.windowManager.updateViewLayout(FloatingControlBrushService.this.floatingControls, this.paramsF);
                        FloatingControlBrushService.this.floatingControls.getLocationOnScreen(FloatingControlBrushService.this.overlayViewLocation);
                        FloatingControlBrushService.this.mRemoveView.getLocationOnScreen(FloatingControlBrushService.this.removeViewLocation);
                        FloatingControlBrushService floatingControlBrushService = FloatingControlBrushService.this;
                        floatingControlBrushService.isOverRemoveView = floatingControlBrushService.isPointInArea(floatingControlBrushService.overlayViewLocation[0], FloatingControlBrushService.this.overlayViewLocation[1], FloatingControlBrushService.this.removeViewLocation[0], FloatingControlBrushService.this.removeViewLocation[1], FloatingControlBrushService.this.mRemoveView.getWidth());
                        if (FloatingControlBrushService.this.isOverRemoveView) {
                            if (this.oneRun) {
                                if (Build.VERSION.SDK_INT < 26) {
                                    FloatingControlBrushService.this.vibrate.vibrate(200);
                                } else {
                                    FloatingControlBrushService.this.vibrate.vibrate(VibrationEffect.createOneShot(200, 255));
                                }
                            }
                            this.oneRun = false;
                        } else {
                            this.oneRun = true;
                        }
                    } else if (action == 3) {
                        FloatingControlBrushService.this.mRemoveView.setVisibility(8);
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
    private void openBrsuh() {
        startService(new Intent(this, BrushService.class));
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
        public FloatingControlBrushService getService() {
            return FloatingControlBrushService.this;
        }
    }
}
