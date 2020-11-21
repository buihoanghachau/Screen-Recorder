package com.testlubu.screenrecorder.services;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.internal.view.SupportMenu;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.RecorderApplication;
import com.testlubu.screenrecorder.common.Const;
import com.testlubu.screenrecorder.common.PrefUtils;
import com.testlubu.screenrecorder.common.Utils;
import com.testlubu.screenrecorder.listener.ObserverInterface;
import com.testlubu.screenrecorder.listener.ObserverUtils;
import com.testlubu.screenrecorder.model.listener.ErrorRecordService;
import com.testlubu.screenrecorder.model.listener.EvbClickBlur;
import com.testlubu.screenrecorder.model.listener.EvbRecordTime;
import com.testlubu.screenrecorder.model.listener.EvbStageRecord;
import com.testlubu.screenrecorder.model.listener.EvbStartRecord;
import com.testlubu.screenrecorder.model.listener.EvbStopService;
import com.testlubu.screenrecorder.model.listener.HideService;
import com.testlubu.screenrecorder.model.listener.ShowService;
import com.testlubu.screenrecorder.ui.activities.HomeActivity;
import com.testlubu.screenrecorder.ui.activities.RecorderActivity;
import com.testlubu.screenrecorder.ui.activities.RequestRecorderActivity;
import com.testlubu.screenrecorder.ui.activities.ScreenShotActivity;
import com.testlubu.screenrecorder.ui.activities.SplashScreenActivity;
import java.util.ArrayList;

public class FloatingControlService extends Service implements View.OnClickListener, ObserverInterface {
    public static final String ACTION_NOTIFICATION_BUTTON_CLICK = "recorder acction click notification";
    public static final String EXTRA_BUTTON_CLICKED = "recorder extra click button";
    private static FloatingControlService instance = null;
    public static boolean isCountdown = false;
    public static boolean isExpand = false;
    public static boolean isPause = false;
    public static boolean isRecording = false;
    private static boolean isRightSide = true;
    private final int NOTIFICATION_ID = 212;
    private final int NOTIFICATION_ID_NEW = 213;
    private final int TIME_DELAY = 2000;
    private IBinder binder = new ServiceBinder();
    private View controlsMain;
    private View controlsMainLeft;
    private View controlsRecorder;
    private View controlsRecorderLeft;
    private LinearLayout floatingControls;
    private GestureDetector gestureDetector;
    private Handler handler = new Handler();
    private int height;
    private ImageView img;
    private ImageView imgBrush;
    private ImageView imgBrushLeft;
    public boolean isBottom = false;
    public boolean isCollapRecord = false;
    public boolean isMove = false;
    private boolean isOverRemoveView;
    public boolean isTop = false;
    private FrameLayout layoutTime;
    private LinearLayout layoutTimer;
    private View mRemoveView;
    private int[] overlayViewLocation = {0, 0};
    private ImageView panelIB;
    private ImageView panelLeftIB;
    private WindowManager.LayoutParams params;
    private WindowManager.LayoutParams paramsClose;
    private WindowManager.LayoutParams paramsTimer;
    private ImageView pauseIB;
    private ImageView pauseLeftIB;
    private SharedPreferences prefs;
    public BroadcastReceiver receiver = new BroadcastReceiver() {
        /* class com.testlubu.screenrecorder.services.FloatingControlService.AnonymousClass8 */

        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(FloatingControlService.EXTRA_BUTTON_CLICKED, -1)) {
                case R.id.capture /*{ENCODED_INT: 2131361878}*/:
                    Intent intent2 = new Intent(context, ScreenShotActivity.class);
                    intent2.setFlags(268435456);
                    Utils.startActivityAllStage(context, intent2);
                    break;
                case R.id.close /*{ENCODED_INT: 2131361895}*/:
                    FloatingControlService.this.onDestroy();
                    FloatingControlService.this.stopSelf();
                    FloatingControlService.this.stopForeground(true);
                    break;
                case R.id.notification_layout_main_container /*{ENCODED_INT: 2131362063}*/:
                    if (Utils.isAppOnForeground(context)) {
                        if (!(RecorderApplication.getInstance().getTopActivity() instanceof HomeActivity)) {
                            Intent intent3 = new Intent(context, HomeActivity.class);
                            intent3.addFlags(335544320);
                            Utils.startActivityAllStage(context, intent3);
                            break;
                        }
                    } else {
                        Intent intent4 = new Intent(context, HomeActivity.class);
                        intent4.addFlags(335544320);
                        Utils.startActivityAllStage(context, intent4);
                        break;
                    }
                    break;
                case R.id.pause_new /*{ENCODED_INT: 2131362081}*/:
                    Intent intent5 = new Intent(context, RecorderActivity.class);
                    intent5.setFlags(268435456);
                    intent5.setAction(Const.SCREEN_RECORDING_PAUSE);
                    Utils.startActivityAllStage(context, intent5);
                    break;
                case R.id.record /*{ENCODED_INT: 2131362095}*/:
                    Intent intent6 = new Intent(context, RecorderActivity.class);
                    intent6.setAction(Const.SCREEN_RECORDING_START_FROM_NOTIFY);
                    intent6.setFlags(268435456);
                    Utils.startActivityAllStage(context, intent6);
                    break;
                case R.id.resume_new /*{ENCODED_INT: 2131362104}*/:
                    Intent intent7 = new Intent(context, RecorderActivity.class);
                    intent7.setFlags(268435456);
                    intent7.setAction(Const.SCREEN_RECORDING_RESUME);
                    Utils.startActivityAllStage(context, intent7);
                    break;
                case R.id.stop_new /*{ENCODED_INT: 2131362167}*/:
                    Intent intent8 = new Intent(context, RecorderActivity.class);
                    intent8.setFlags(268435456);
                    intent8.setAction(Const.SCREEN_RECORDING_STOP);
                    Utils.startActivityAllStage(context, intent8);
                    break;
                case R.id.tools /*{ENCODED_INT: 2131362207}*/:
                    FloatingControlService.this.openTools();
                    break;
                case R.id.tools_new /*{ENCODED_INT: 2131362208}*/:
                    FloatingControlService.this.openTools();
                    break;
            }
            context.sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
        }
    };
    public BroadcastReceiver receiverCapture = new BroadcastReceiver() {
        /* class com.testlubu.screenrecorder.services.FloatingControlService.AnonymousClass9 */

        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                int i = intent.getExtras().getInt("capture");
                if (i == 0) {
                    FloatingControlService.this.floatingControls.setVisibility(4);
                } else if (i == 1) {
                    FloatingControlService.this.floatingControls.setVisibility(0);
                }
            }
        }
    };
    private ImageView recorderIB;
    private ImageView recorderLeftIB;
    private int[] removeViewLocation = {0, 0};
    private ImageView resumeIB;
    private ImageView resumeLeftIB;
    private ImageView rewardIB;
    private Runnable runAnim = new Runnable() {
        /* class com.testlubu.screenrecorder.services.FloatingControlService.AnonymousClass11 */

        public void run() {
            if (FloatingControlService.this.tvTime.getAlpha() == 0.5f) {
                FloatingControlService.this.tvTime.setAlpha(1.0f);
            } else {
                FloatingControlService.this.tvTime.setAlpha(0.5f);
            }
            FloatingControlService.this.handler.postDelayed(this, 800);
        }
    };
    private Runnable runnable = new Runnable() {
        /* class com.testlubu.screenrecorder.services.FloatingControlService.AnonymousClass1 */

        public void run() {
            FloatingControlService.this.collapseFloatingControls();
            FloatingControlService.this.setAlphaAssistiveIcon();
        }
    };
    private Runnable runnableRecord = new Runnable() {
        /* class com.testlubu.screenrecorder.services.FloatingControlService.AnonymousClass2 */

        public void run() {
            if (!FloatingControlService.this.isMove) {
                FloatingControlService floatingControlService = FloatingControlService.this;
                floatingControlService.isCollapRecord = true;
                ViewGroup.LayoutParams layoutParams = floatingControlService.layoutTime.getLayoutParams();
                layoutParams.height = Utils.convertDpToPixel(48.0f, FloatingControlService.this);
                layoutParams.width = Utils.convertDpToPixel(24.0f, FloatingControlService.this);
                FloatingControlService.this.img.setVisibility(8);
                FloatingControlService.this.tvTime.setVisibility(8);
                if (FloatingControlService.isRightSide) {
                    FloatingControlService.this.layoutTime.setBackgroundResource(R.drawable.ic_record_dot_right);
                } else {
                    FloatingControlService.this.layoutTime.setBackgroundResource(R.drawable.ic_record_dot);
                }
                FloatingControlService.this.floatingControls.setAlpha(0.5f);
                FloatingControlService.this.layoutTime.setLayoutParams(layoutParams);
            }
        }
    };
    private ImageView screenshotIB;
    private ImageView screenshotLeftIB;
    private ImageView stopIB;
    private ImageView stopLeftIB;
    private ImageView toolRecordLeft;
    private ImageView toolsRecord;
    private TextView tvTime;
    private TextView txtTimer;
    private Vibrator vibrate;
    private int width;
    private WindowManager windowManager;
    private int yTranstion = 0;

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPointInArea(int i, int i2, int i3, int i4, int i5) {
        return i >= i3 - i5 && i <= i3 + i5 && i2 >= i4 - i5 && i2 <= i4 + i5;
    }

    public static FloatingControlService getInstance() {
        return instance;
    }

    public static void setInstance(FloatingControlService floatingControlService) {
        instance = floatingControlService;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setAlphaAssistiveIcon() {
        if (this.floatingControls == null) {
            return;
        }
        if (this.controlsRecorder.getVisibility() != 0 || this.controlsMain.getVisibility() != 0 || this.controlsRecorderLeft.getVisibility() != 0 || this.controlsMainLeft.getVisibility() != 0) {
            ViewGroup.LayoutParams layoutParams = this.layoutTime.getLayoutParams();
            int i = this.width;
            layoutParams.height = i / 10;
            layoutParams.width = i / 10;
            this.img.setImageResource(R.drawable.ic_record_float);
            this.floatingControls.setAlpha(0.5f);
            this.layoutTime.setLayoutParams(layoutParams);
            if (this.params.x < this.width - this.params.x) {
                this.params.x = 0;
            } else {
                this.params.x = this.width;
            }
            this.windowManager.updateViewLayout(this.floatingControls, this.params);
            if (isRecording) {
                this.handler.removeCallbacks(this.runnableRecord);
                this.handler.postDelayed(this.runnableRecord, 2000);
            }
        }
    }

    public void onCreate() {
        super.onCreate();
        this.vibrate = (Vibrator) getSystemService("vibrator");
        ObserverUtils.getInstance().registerObserver((ObserverInterface) this);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (this.prefs.getBoolean(Const.PREFS_TOOLS_BRUSH, false)) {
            openBrushControlService();
        }
        if (this.prefs.getBoolean(Const.PREFS_TOOLS_CAPTURE, false)) {
            openCaptureControlService();
        }
        if (this.prefs.getBoolean(Const.PREFS_TOOLS_CAMERA, false)) {
            openCameraControlService();
        }
        this.windowManager = (WindowManager) getApplicationContext().getSystemService("window");
        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService("layout_inflater");
        this.floatingControls = (LinearLayout) layoutInflater.inflate(R.layout.layout_floatbutton, (ViewGroup) null);
        this.mRemoveView = onGetRemoveView();
        setupRemoveView(this.mRemoveView);
        this.layoutTimer = (LinearLayout) layoutInflater.inflate(R.layout.layout_timer, (ViewGroup) null);
        this.txtTimer = (TextView) this.layoutTimer.findViewById(R.id.txt_timer);
        this.controlsRecorder = this.floatingControls.findViewById(R.id.controls_recorder);
        this.controlsRecorderLeft = this.floatingControls.findViewById(R.id.controls_recorder_left);
        this.controlsMain = this.floatingControls.findViewById(R.id.controls_main);
        this.controlsMainLeft = this.floatingControls.findViewById(R.id.controls_main_left);
        this.layoutTime = (FrameLayout) this.floatingControls.findViewById(R.id.layout_time);
        this.imgBrush = (ImageView) this.floatingControls.findViewById(R.id.imgTools);
        this.imgBrushLeft = (ImageView) this.floatingControls.findViewById(R.id.imgTools_left);
        this.img = (ImageView) this.floatingControls.findViewById(R.id.imgIcon);
        this.tvTime = (TextView) this.floatingControls.findViewById(R.id.tv_time);
        this.floatingControls.post(new Runnable() {
            /* class com.testlubu.screenrecorder.services.FloatingControlService.AnonymousClass3 */

            public void run() {
                FloatingControlService floatingControlService = FloatingControlService.this;
                floatingControlService.yTranstion = (floatingControlService.floatingControls.getHeight() / 2) - (FloatingControlService.this.layoutTime.getHeight() / 2);
            }
        });
        this.controlsMain.setVisibility(8);
        this.controlsMainLeft.setVisibility(8);
        this.controlsRecorder.setVisibility(8);
        this.controlsRecorderLeft.setVisibility(8);
        this.stopIB = (ImageView) this.controlsRecorder.findViewById(R.id.stop);
        this.pauseIB = (ImageView) this.controlsRecorder.findViewById(R.id.pause);
        this.resumeIB = (ImageView) this.controlsRecorder.findViewById(R.id.resume);
        this.toolsRecord = (ImageView) this.controlsRecorder.findViewById(R.id.tools_record);
        this.stopLeftIB = (ImageView) this.controlsRecorderLeft.findViewById(R.id.stop_left);
        this.pauseLeftIB = (ImageView) this.controlsRecorderLeft.findViewById(R.id.pause_left);
        this.resumeLeftIB = (ImageView) this.controlsRecorderLeft.findViewById(R.id.resume_left);
        this.toolRecordLeft = (ImageView) this.controlsRecorderLeft.findViewById(R.id.tools_record_left);
        this.recorderIB = (ImageView) this.controlsMain.findViewById(R.id.recorder);
        this.screenshotIB = (ImageView) this.controlsMain.findViewById(R.id.screenshot);
        this.panelIB = (ImageView) this.controlsMain.findViewById(R.id.panel);
        this.recorderLeftIB = (ImageView) this.controlsMainLeft.findViewById(R.id.recorder_left);
        this.screenshotLeftIB = (ImageView) this.controlsMainLeft.findViewById(R.id.screenshot_left);
        this.panelLeftIB = (ImageView) this.controlsMainLeft.findViewById(R.id.panel_left);
        this.stopIB.setOnClickListener(this);
        this.imgBrush.setOnClickListener(this);
        this.imgBrushLeft.setOnClickListener(this);
        this.stopLeftIB.setOnClickListener(this);
        this.recorderIB.setOnClickListener(this);
        this.recorderLeftIB.setOnClickListener(this);
        this.screenshotIB.setOnClickListener(this);
        this.screenshotLeftIB.setOnClickListener(this);
        this.panelIB.setOnClickListener(this);
        this.panelLeftIB.setOnClickListener(this);
        this.toolsRecord.setOnClickListener(this);
        this.toolRecordLeft.setOnClickListener(this);
        this.pauseIB.setOnClickListener(this);
        this.pauseLeftIB.setOnClickListener(this);
        this.resumeIB.setOnClickListener(this);
        this.resumeLeftIB.setOnClickListener(this);
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
        layoutParams.x = 0;
        layoutParams.y = this.height / 4;
        this.paramsTimer = new WindowManager.LayoutParams(-2, -2, 2038, 8, -3);
        if (Build.VERSION.SDK_INT < 26) {
            this.paramsTimer.type = 2005;
        }
        this.paramsTimer.gravity = 17;
        this.gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            /* class com.testlubu.screenrecorder.services.FloatingControlService.AnonymousClass4 */

            public boolean onDoubleTap(MotionEvent motionEvent) {
                return true;
            }

            public boolean onSingleTapUp(MotionEvent motionEvent) {
                return true;
            }
        });
        this.floatingControls.setOnTouchListener(new View.OnTouchListener() {
            /* class com.testlubu.screenrecorder.services.FloatingControlService.AnonymousClass5 */
            private boolean flag = false;
            private float initialTouchX;
            private float initialTouchY;
            private int initialX;
            private int initialY;
            private boolean oneRun = false;
            private WindowManager.LayoutParams paramsF = FloatingControlService.this.params;

            public boolean onTouch(View view, MotionEvent motionEvent) {
                FloatingControlService.this.handler.removeCallbacks(FloatingControlService.this.runnable);
                if (FloatingControlService.this.gestureDetector.onTouchEvent(motionEvent)) {
                    FloatingControlService.this.mRemoveView.setVisibility(8);
                    if (FloatingControlService.this.controlsRecorder.getVisibility() == 0 || FloatingControlService.this.controlsMain.getVisibility() == 0 || FloatingControlService.this.controlsRecorderLeft.getVisibility() == 0 || FloatingControlService.this.controlsMainLeft.getVisibility() == 0) {
                        FloatingControlService.this.collapseFloatingControls();
                    } else {
                        FloatingControlService.this.expandFloatingControls();
                    }
                    FloatingControlService.this.handler.removeCallbacks(FloatingControlService.this.runnable);
                    FloatingControlService.this.handler.postDelayed(FloatingControlService.this.runnable, 2000);
                } else if (!FloatingControlService.isExpand) {
                    int action = motionEvent.getAction();
                    if (action == 0) {
                        if (FloatingControlService.isRecording) {
                            FloatingControlService.this.layoutTime.setBackgroundResource(R.drawable.bg_time);
                            ViewGroup.LayoutParams layoutParams = FloatingControlService.this.layoutTime.getLayoutParams();
                            layoutParams.height = FloatingControlService.this.width / 8;
                            layoutParams.width = FloatingControlService.this.width / 8;
                            FloatingControlService.this.tvTime.setVisibility(0);
                            FloatingControlService.this.floatingControls.setAlpha(1.0f);
                            FloatingControlService.this.layoutTime.setLayoutParams(layoutParams);
                        } else {
                            ViewGroup.LayoutParams layoutParams2 = FloatingControlService.this.layoutTime.getLayoutParams();
                            layoutParams2.height = FloatingControlService.this.width / 8;
                            layoutParams2.width = FloatingControlService.this.width / 8;
                            FloatingControlService.this.layoutTime.setLayoutParams(layoutParams2);
                            FloatingControlService.this.floatingControls.setAlpha(1.0f);
                        }
                        this.initialX = this.paramsF.x;
                        this.initialY = this.paramsF.y;
                        this.initialTouchX = motionEvent.getRawX();
                        this.initialTouchY = motionEvent.getRawY();
                        this.flag = true;
                    } else if (action == 1) {
                        FloatingControlService floatingControlService = FloatingControlService.this;
                        floatingControlService.isMove = false;
                        if (floatingControlService.params.x < FloatingControlService.this.width - FloatingControlService.this.params.x) {
                            FloatingControlService.this.params.x = 0;
                            boolean unused = FloatingControlService.isRightSide = true;
                        } else {
                            FloatingControlService.this.params.x = FloatingControlService.this.width - FloatingControlService.this.floatingControls.getWidth();
                            boolean unused2 = FloatingControlService.isRightSide = false;
                        }
                        FloatingControlService floatingControlService2 = FloatingControlService.this;
                        floatingControlService2.isTop = floatingControlService2.params.y <= FloatingControlService.this.yTranstion;
                        FloatingControlService floatingControlService3 = FloatingControlService.this;
                        floatingControlService3.isBottom = floatingControlService3.params.y >= (FloatingControlService.this.height - FloatingControlService.this.yTranstion) - FloatingControlService.this.floatingControls.getHeight();
                        if (FloatingControlService.this.params.y <= 0) {
                            FloatingControlService.this.params.y = 0;
                        } else if (FloatingControlService.this.params.y >= FloatingControlService.this.height - FloatingControlService.this.floatingControls.getHeight()) {
                            FloatingControlService.this.params.y = FloatingControlService.this.height;
                        }
                        this.flag = false;
                        if (FloatingControlService.this.isOverRemoveView) {
                            FloatingControlService.this.onDestroy();
                            FloatingControlService.this.stopSelf();
                            FloatingControlService.this.stopForeground(true);
                        } else {
                            FloatingControlService.this.windowManager.updateViewLayout(FloatingControlService.this.floatingControls, FloatingControlService.this.params);
                            FloatingControlService.this.handler.removeCallbacks(FloatingControlService.this.runnable);
                            FloatingControlService.this.handler.postDelayed(FloatingControlService.this.runnable, 2000);
                        }
                        FloatingControlService.this.mRemoveView.setVisibility(8);
                    } else if (action == 2) {
                        FloatingControlService.this.isMove = true;
                        WindowManager.LayoutParams layoutParams3 = this.paramsF;
                        layoutParams3.x = this.initialX + ((int) (motionEvent.getRawX() - this.initialTouchX));
                        layoutParams3.y = this.initialY + ((int) (motionEvent.getRawY() - this.initialTouchY));
                        if (this.flag && !FloatingControlService.isRecording) {
                            FloatingControlService.this.mRemoveView.setVisibility(0);
                        }
                        FloatingControlService.this.windowManager.updateViewLayout(FloatingControlService.this.floatingControls, this.paramsF);
                        FloatingControlService.this.floatingControls.getLocationOnScreen(FloatingControlService.this.overlayViewLocation);
                        FloatingControlService.this.mRemoveView.getLocationOnScreen(FloatingControlService.this.removeViewLocation);
                        FloatingControlService floatingControlService4 = FloatingControlService.this;
                        floatingControlService4.isOverRemoveView = floatingControlService4.isPointInArea(floatingControlService4.overlayViewLocation[0], FloatingControlService.this.overlayViewLocation[1], FloatingControlService.this.removeViewLocation[0], FloatingControlService.this.removeViewLocation[1], FloatingControlService.this.mRemoveView.getWidth());
                        if (FloatingControlService.this.isOverRemoveView) {
                            if (this.oneRun) {
                                FloatingControlService.this.floatingControls.setY(FloatingControlService.this.mRemoveView.getY());
                                if (Build.VERSION.SDK_INT < 26) {
                                    FloatingControlService.this.vibrate.vibrate(200);
                                } else {
                                    FloatingControlService.this.vibrate.vibrate(VibrationEffect.createOneShot(200, 255));
                                }
                            }
                            this.oneRun = false;
                        } else {
                            this.oneRun = true;
                        }
                    } else if (action == 3) {
                        FloatingControlService.this.mRemoveView.setVisibility(8);
                    }
                }
                return false;
            }
        });
        addBubbleView();
        this.handler.removeCallbacks(this.runnable);
        this.handler.postDelayed(this.runnable, 2000);
        registerReceiver(this.receiverCapture, new IntentFilter(Const.ACTION_SCREEN_SHOT));
    }

    private void openCaptureControlService() {
        startService(new Intent(this, FloatingControlCaptureService.class));
    }

    private void openCameraControlService() {
        startService(new Intent(this, FloatingControlCameraService.class));
    }

    private void openBrushControlService() {
        startService(new Intent(this, FloatingControlBrushService.class));
    }

    private void setupRemoveView(View view) {
        view.setVisibility(8);
        this.windowManager.addView(view, newWindowManagerLayoutParamsForRemoveView());
    }

    private WindowManager.LayoutParams newWindowManagerLayoutParamsForRemoveView() {
        this.paramsClose = new WindowManager.LayoutParams(-2, -2, Build.VERSION.SDK_INT < 26 ? 2002 : 2038, 262664, -3);
        WindowManager.LayoutParams layoutParams = this.paramsClose;
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
        if (getInstance() == null) {
            setInstance(this);
        }
        showNotification2();
        if (!(intent == null || intent.getAction() == null)) {
            String action = intent.getAction();
            char c = 65535;
            switch (action.hashCode()) {
                case -1996135482:
                    if (action.equals(Const.SCREEN_RECORDING_START_FROM_NOTIFY)) {
                        c = 0;
                        break;
                    }
                    break;
                case -1053033865:
                    if (action.equals(Const.SCREEN_RECORDING_STOP)) {
                        c = 3;
                        break;
                    }
                    break;
                case -453103993:
                    if (action.equals(Const.SCREEN_RECORDING_START)) {
                        c = 2;
                        break;
                    }
                    break;
                case 143300674:
                    if (action.equals(Const.SCREEN_RECORDING_DESTROY)) {
                        c = 1;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                handlerTimer();
            } else if (c == 1) {
                onDestroy();
                stopSelf();
                stopForeground(true);
                stopBrush();
                stopCamera();
                stopCapture();
            } else if (c == 2 || c == 3) {
                collapseFloatingControls();
            }
        }
        if (SplashScreenActivity.isFirstOpen && !isRecording && !isPause) {
            if (isRightSide) {
                this.controlsMain.setVisibility(0);
            } else {
                this.controlsMainLeft.setVisibility(0);
            }
            expandFloatingControls();
            ViewGroup.LayoutParams layoutParams = this.layoutTime.getLayoutParams();
            int i3 = this.width;
            layoutParams.height = i3 / 8;
            layoutParams.width = i3 / 8;
            this.layoutTime.setLayoutParams(layoutParams);
            this.floatingControls.setAlpha(1.0f);
            new Handler().postDelayed(new Runnable() {
                /* class com.testlubu.screenrecorder.services.FloatingControlService.AnonymousClass6 */

                public void run() {
                    if (!FloatingControlService.isRecording && !FloatingControlService.isPause) {
                        if (FloatingControlService.isRightSide) {
                            FloatingControlService.this.controlsMain.setVisibility(8);
                        } else {
                            FloatingControlService.this.controlsMainLeft.setVisibility(8);
                        }
                        FloatingControlService.this.collapseFloatingControls();
                        FloatingControlService.this.setAlphaAssistiveIcon();
                    }
                }
            }, 2000);
            SplashScreenActivity.isFirstOpen = false;
        }
        return super.onStartCommand(intent, i, i2);
    }

    private void stopCapture() {
        stopService(new Intent(this, FloatingControlCaptureService.class));
    }

    private void stopCamera() {
        stopService(new Intent(this, FloatingControlCameraService.class));
    }

    private void stopBrush() {
        stopService(new Intent(this, FloatingControlBrushService.class));
    }

    public void addBubbleView() {
        LinearLayout linearLayout;
        WindowManager windowManager2 = this.windowManager;
        if (windowManager2 != null && (linearLayout = this.floatingControls) != null) {
            windowManager2.addView(linearLayout, this.params);
        }
    }

    public void addTimerView() {
        LinearLayout linearLayout;
        WindowManager windowManager2 = this.windowManager;
        if (windowManager2 != null && (linearLayout = this.layoutTimer) != null) {
            windowManager2.addView(linearLayout, this.paramsTimer);
        }
    }

    public void removeTimerView() {
        LinearLayout linearLayout;
        WindowManager windowManager2 = this.windowManager;
        if (windowManager2 != null && (linearLayout = this.layoutTimer) != null) {
            windowManager2.removeView(linearLayout);
        }
    }

    public void removeBubbleView() {
        try {
            if (this.windowManager != null && this.floatingControls != null) {
                this.windowManager.removeView(this.floatingControls);
            }
        } catch (Exception unused) {
        }
    }

    public void handlerTimer() {
        final Animation loadAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_tv);
        collapseFloatingControls();
        int intValue = Integer.valueOf(PrefUtils.readStringValue(this, getString(R.string.timer_key), "3")).intValue();
        if (intValue == 0) {
            requestRecorder();
            return;
        }
        isCountdown = true;
        addTimerView();
        TextView textView = this.txtTimer;
        StringBuilder sb = new StringBuilder();
        int i = intValue + 1;
        sb.append(i);
        sb.append("");
        textView.setText(sb.toString());
        new CountDownTimer((long) (i * 1000), 1000) {
            /* class com.testlubu.screenrecorder.services.FloatingControlService.AnonymousClass7 */

            public void onFinish() {
                FloatingControlService.this.txtTimer.setText("");
                FloatingControlService.this.removeTimerView();
                FloatingControlService.this.requestRecorder();
                FloatingControlService.isCountdown = false;
            }

            public void onTick(long j) {
                TextView textView = FloatingControlService.this.txtTimer;
                textView.setText((j / 1000) + "");
                FloatingControlService.this.txtTimer.startAnimation(loadAnimation);
            }
        }.start();
    }

    public void removeNotification() {
        ((NotificationManager) getSystemService("notification")).cancel(212);
    }

    public void removeNotificationNew() {
        ((NotificationManager) getSystemService("notification")).cancel(213);
    }

    private void showNotification2() {
        RemoteViews remoteViews;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setPriority(-2);
        builder.setLargeIcon(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.logo), 52, 52, false));
        builder.setOngoing(true);
        builder.setContentTitle(getString(R.string.app_name) + " is running").setTicker("Notification keeps app always run properly");
        if (isRecording) {
            remoteViews = new RemoteViews(getPackageName(), (int) R.layout.custom_notification_new_record);
            if (isPause) {
                remoteViews.setViewVisibility(R.id.pause_new, 8);
                remoteViews.setViewVisibility(R.id.resume_new, 0);
            } else {
                remoteViews.setViewVisibility(R.id.pause_new, 0);
                remoteViews.setViewVisibility(R.id.resume_new, 8);
            }
        } else {
            remoteViews = new RemoteViews(getPackageName(), (int) R.layout.custom_notification_new);
        }
        remoteViews.setOnClickPendingIntent(R.id.notification_layout_main_container, onButtonNotificationClick(R.id.notification_layout_main_container));
        remoteViews.setOnClickPendingIntent(R.id.record, onButtonNotificationClick(R.id.record));
        remoteViews.setOnClickPendingIntent(R.id.capture, onButtonNotificationClick(R.id.capture));
        remoteViews.setOnClickPendingIntent(R.id.tools, onButtonNotificationClick(R.id.tools));
        remoteViews.setOnClickPendingIntent(R.id.close, onButtonNotificationClick(R.id.close));
        remoteViews.setOnClickPendingIntent(R.id.pause_new, onButtonNotificationClick(R.id.pause_new));
        remoteViews.setOnClickPendingIntent(R.id.resume_new, onButtonNotificationClick(R.id.resume_new));
        remoteViews.setOnClickPendingIntent(R.id.stop_new, onButtonNotificationClick(R.id.stop_new));
        remoteViews.setOnClickPendingIntent(R.id.tools_new, onButtonNotificationClick(R.id.tools_new));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_NOTIFICATION_BUTTON_CLICK);
        try {
            unregisterReceiver(this.receiver);
        } catch (Exception unused) {
        }
        registerReceiver(this.receiver, intentFilter);
        if (Utils.isAndroid26()) {
            createChanelIDNew();
            builder.setChannelId("my_channel_screenrecorder_new");
        }
        builder.setCustomContentView(remoteViews);
        startForeground(212, builder.build());
    }

    public PendingIntent onButtonNotificationClick(@IdRes int i) {
        Intent intent = new Intent(ACTION_NOTIFICATION_BUTTON_CLICK);
        intent.putExtra(EXTRA_BUTTON_CLICKED, i);
        return PendingIntent.getBroadcast(this, i, intent, 0);
    }

    @SuppressLint({"WrongConstant"})
    @TargetApi(26)
    private void createChanelIDNew() {
        String string = getString(R.string.app_name);
        String string2 = getString(R.string.app_name);
        NotificationChannel notificationChannel = new NotificationChannel("my_channel_screenrecorder_new", string, 2);
        notificationChannel.setDescription(string2);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(SupportMenu.CATEGORY_MASK);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        ((NotificationManager) getSystemService("notification")).createNotificationChannel(notificationChannel);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void expandFloatingControls() {
        startBlur();
        isExpand = true;
        this.handler.removeCallbacks(this.runnableRecord);
        this.isCollapRecord = false;
        if (isRecording) {
            this.layoutTime.setBackgroundResource(R.drawable.bg_time);
            ViewGroup.LayoutParams layoutParams = this.layoutTime.getLayoutParams();
            int i = this.width;
            layoutParams.height = i / 8;
            layoutParams.width = i / 8;
            this.tvTime.setVisibility(0);
            this.floatingControls.setAlpha(1.0f);
            this.layoutTime.setLayoutParams(layoutParams);
        }
        this.img.setImageResource(R.drawable.ic_close_brush);
        final ArrayList arrayList = new ArrayList();
        this.layoutTime.setVisibility(4);
        this.layoutTime.post(new Runnable() {
            /* class com.testlubu.screenrecorder.services.FloatingControlService.AnonymousClass10 */

            public void run() {
                if (FloatingControlService.isRightSide) {
                    if (FloatingControlService.isRecording) {
                        FloatingControlService.this.controlsRecorder.setVisibility(4);
                        FloatingControlService.this.controlsMain.setVisibility(8);
                        arrayList.add(FloatingControlService.this.controlsRecorder);
                    } else {
                        FloatingControlService.this.controlsMain.setVisibility(4);
                        FloatingControlService.this.controlsRecorder.setVisibility(8);
                        arrayList.add(FloatingControlService.this.controlsMain);
                    }
                    if (FloatingControlService.isPause) {
                        FloatingControlService.this.pauseIB.setVisibility(8);
                        FloatingControlService.this.resumeIB.setVisibility(0);
                    } else {
                        FloatingControlService.this.pauseIB.setVisibility(0);
                        FloatingControlService.this.resumeIB.setVisibility(8);
                    }
                } else {
                    if (FloatingControlService.isRecording) {
                        FloatingControlService.this.controlsRecorderLeft.setVisibility(4);
                        FloatingControlService.this.controlsMainLeft.setVisibility(8);
                        arrayList.add(FloatingControlService.this.controlsRecorderLeft);
                    } else {
                        FloatingControlService.this.controlsMainLeft.setVisibility(4);
                        FloatingControlService.this.controlsRecorderLeft.setVisibility(8);
                        arrayList.add(FloatingControlService.this.controlsMainLeft);
                    }
                    if (FloatingControlService.isPause) {
                        FloatingControlService.this.pauseLeftIB.setVisibility(8);
                        FloatingControlService.this.resumeLeftIB.setVisibility(0);
                    } else {
                        FloatingControlService.this.pauseLeftIB.setVisibility(0);
                        FloatingControlService.this.resumeLeftIB.setVisibility(8);
                    }
                }
                FloatingControlService.this.floatingControls.post(new Runnable() {
                    /* class com.testlubu.screenrecorder.services.FloatingControlService.AnonymousClass10.AnonymousClass1 */

                    public void run() {
                        FloatingControlService.this.params.y -= (FloatingControlService.this.floatingControls.getHeight() / 2) - (FloatingControlService.this.layoutTime.getHeight() / 2);
                        FloatingControlService.this.windowManager.removeView(FloatingControlService.this.floatingControls);
                        FloatingControlService.this.windowManager.addView(FloatingControlService.this.floatingControls, FloatingControlService.this.params);
                        if (!arrayList.isEmpty()) {
                            ((View) arrayList.get(0)).setVisibility(0);
                        }
                        FloatingControlService.this.layoutTime.setVisibility(0);
                    }
                });
            }
        });
    }

    private void startBlur() {
        startService(new Intent(this, BlurService.class));
    }

    private void stopBlur() {
        stopService(new Intent(this, BlurService.class));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void collapseFloatingControls() {
        stopBlur();
        this.img.setImageResource(R.drawable.ic_record_float);
        isExpand = false;
        this.tvTime.setText("00:00");
        this.layoutTime.setVisibility(4);
        if (this.isTop) {
            this.params.y = (this.floatingControls.getHeight() / 2) - (this.layoutTime.getHeight() / 2);
            this.windowManager.removeView(this.floatingControls);
            this.windowManager.addView(this.floatingControls, this.params);
        } else if (this.isBottom) {
            this.params.y = this.height - ((this.floatingControls.getHeight() / 2) + (this.layoutTime.getHeight() / 2));
            this.windowManager.removeView(this.floatingControls);
            this.windowManager.addView(this.floatingControls, this.params);
        } else {
            this.params.y += (this.floatingControls.getHeight() / 2) - (this.layoutTime.getHeight() / 2);
            this.windowManager.removeView(this.floatingControls);
            this.windowManager.addView(this.floatingControls, this.params);
        }
        this.controlsMain.setVisibility(8);
        this.controlsRecorder.setVisibility(8);
        this.controlsMainLeft.setVisibility(8);
        this.controlsRecorderLeft.setVisibility(8);
        this.layoutTime.setVisibility(0);
    }

    public void onClick(View view) {
        if (!isCountdown) {
            switch (view.getId()) {
                case R.id.imgTools /*{ENCODED_INT: 2131361972}*/:
                case R.id.imgTools_left /*{ENCODED_INT: 2131361973}*/:
                case R.id.tools_record /*{ENCODED_INT: 2131362209}*/:
                case R.id.tools_record_left /*{ENCODED_INT: 2131362210}*/:
                    openTools();
                    break;
                case R.id.panel /*{ENCODED_INT: 2131362073}*/:
                case R.id.panel_left /*{ENCODED_INT: 2131362074}*/:
                    openSetting();
                    break;
                case R.id.pause /*{ENCODED_INT: 2131362079}*/:
                case R.id.pause_left /*{ENCODED_INT: 2131362080}*/:
                    pauseScreenRecording();
                    break;
                case R.id.recorder /*{ENCODED_INT: 2131362096}*/:
                case R.id.recorder_left /*{ENCODED_INT: 2131362097}*/:
                    handlerTimer();
                    break;
                case R.id.resume /*{ENCODED_INT: 2131362102}*/:
                case R.id.resume_left /*{ENCODED_INT: 2131362103}*/:
                    resumeScreenRecording();
                    break;
                case R.id.screenshot /*{ENCODED_INT: 2131362118}*/:
                case R.id.screenshot_left /*{ENCODED_INT: 2131362119}*/:
                    openPanel();
                    break;
                case R.id.stop /*{ENCODED_INT: 2131362165}*/:
                case R.id.stop_left /*{ENCODED_INT: 2131362166}*/:
                    stopScreenSharing();
                    break;
            }
            if (PrefUtils.readBooleanValue(this, getString(R.string.preference_vibrate_key), true)) {
                ((Vibrator) getSystemService("vibrator")).vibrate(100);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void openTools() {
        collapseFloatingControls();
        startService(new Intent(this, ToolsService.class));
    }

    private void openSetting() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(268435456);
        intent.putExtra("action", "setting");
        Utils.startActivityAllStage(this, intent);
        collapseFloatingControls();
    }

    private void openPanel() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(268435456);
        Utils.startActivityAllStage(this, intent);
        collapseFloatingControls();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestRecorder() {
        this.tvTime.setVisibility(0);
        this.img.setVisibility(8);
        Intent intent = new Intent(this, RequestRecorderActivity.class);
        intent.setFlags(268435456);
        Utils.startActivityAllStage(this, intent);
    }

    private void screenshot() {
        collapseFloatingControls();
        Intent intent = new Intent(this, ScreenShotActivity.class);
        intent.setFlags(268435456);
        startActivity(intent);
    }

    private void resumeScreenRecording() {
        this.handler.removeCallbacks(this.runAnim);
        isRecording = true;
        isPause = false;
        collapseFloatingControls();
        Intent intent = new Intent(this, RecorderService.class);
        intent.setAction(Const.SCREEN_RECORDING_RESUME);
        startService(intent);
    }

    private void pauseScreenRecording() {
        this.handler.postDelayed(this.runAnim, 800);
        isRecording = true;
        isPause = true;
        collapseFloatingControls();
        Intent intent = new Intent(this, RecorderService.class);
        intent.setAction(Const.SCREEN_RECORDING_PAUSE);
        startService(intent);
    }

    private void stopScreenSharing() {
        isRecording = false;
        isPause = false;
        this.layoutTime.setBackgroundResource(R.drawable.bg_time);
        ViewGroup.LayoutParams layoutParams = this.layoutTime.getLayoutParams();
        int i = this.width;
        layoutParams.height = i / 8;
        layoutParams.width = i / 8;
        this.floatingControls.setAlpha(1.0f);
        this.layoutTime.setLayoutParams(layoutParams);
        collapseFloatingControls();
        Intent intent = new Intent(this, RecorderService.class);
        intent.setAction(Const.SCREEN_RECORDING_STOP);
        startService(intent);
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.testlubu.screenrecorder.services.FloatingControlService$12  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass12 {
        static final /* synthetic */ int[] $SwitchMap$com$testlubu$screenrecorder$common$Const$RecordingState = new int[Const.RecordingState.values().length];

        /* JADX WARNING: Can't wrap try/catch for region: R(8:0|1|2|3|4|5|6|8) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0014 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001f */
        static {
            $SwitchMap$com$testlubu$screenrecorder$common$Const$RecordingState[Const.RecordingState.PAUSED.ordinal()] = 1;
            $SwitchMap$com$testlubu$screenrecorder$common$Const$RecordingState[Const.RecordingState.RECORDING.ordinal()] = 2;
            try {
                $SwitchMap$com$testlubu$screenrecorder$common$Const$RecordingState[Const.RecordingState.STOPPED.ordinal()] = 3;
            } catch (NoSuchFieldError unused) {
            }
        }
    }

    public void setRecordingState(Const.RecordingState recordingState) {
        int i = AnonymousClass12.$SwitchMap$com$testlubu$screenrecorder$common$Const$RecordingState[recordingState.ordinal()];
        if (i == 1) {
            isPause = true;
        } else if (i == 2) {
            isPause = false;
        } else if (i == 3) {
            isRecording = false;
            isPause = false;
            collapseFloatingControls();
        }
    }

    public void onDestroy() {
        setInstance(null);
        try {
            ObserverUtils.getInstance().notifyObservers(new EvbStopService());
            removeBubbleView();
            removeNotification();
            removeNotificationNew();
            ObserverUtils.getInstance().removeObserver((ObserverInterface) this);
            this.handler.removeCallbacks(this.runAnim);
            unregisterReceiver(this.receiverCapture);
            unregisterReceiver(this.receiver);
            if (!(this.windowManager == null || this.mRemoveView == null)) {
                this.windowManager.removeView(this.mRemoveView);
            }
        } catch (Exception unused) {
        }
        super.onDestroy();
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        Log.d(Const.TAG, "Binding successful!");
        return this.binder;
    }

    @Override // com.testlubu.screenrecorder.listener.ObserverInterface
    public void notifyAction(Object obj) {
        if (obj instanceof EvbStageRecord) {
            if (((EvbStageRecord) obj).isStart) {
                handlerTimer();
            } else {
                stopScreenSharing();
            }
        } else if (obj instanceof EvbRecordTime) {
            this.tvTime.setText(((EvbRecordTime) obj).time);
        } else if (obj instanceof EvbStopService) {
            this.img.setVisibility(0);
            this.tvTime.setVisibility(8);
        }
        if (obj instanceof EvbStartRecord) {
            this.handler.postDelayed(this.runnable, 2000);
        }
        if (obj instanceof EvbClickBlur) {
            collapseFloatingControls();
        }
        if (obj instanceof ShowService) {
            this.floatingControls.setVisibility(0);
        }
        if (obj instanceof HideService) {
            this.floatingControls.setVisibility(8);
        }
        if (obj instanceof ErrorRecordService) {
            stopScreenSharing();
        }
    }

    public class ServiceBinder extends Binder {
        public ServiceBinder() {
        }

        /* access modifiers changed from: package-private */
        public FloatingControlService getService() {
            return FloatingControlService.this;
        }
    }
}
