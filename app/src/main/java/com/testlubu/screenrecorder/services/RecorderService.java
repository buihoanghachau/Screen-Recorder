package com.testlubu.screenrecorder.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.internal.view.SupportMenu;
import com.ads.control.funtion.UtilsApp;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.common.Const;
import com.testlubu.screenrecorder.common.PrefUtils;
import com.testlubu.screenrecorder.common.Utils;
import com.testlubu.screenrecorder.gesture.ShakeEventManager;
import com.testlubu.screenrecorder.listener.ObserverUtils;
import com.testlubu.screenrecorder.model.listener.EvbRecordTime;
import com.testlubu.screenrecorder.model.listener.EvbStageRecord;
import com.testlubu.screenrecorder.model.listener.EvbStartRecord;
import com.testlubu.screenrecorder.model.listener.EvbStopService;
import com.testlubu.screenrecorder.services.FloatingCameraViewService;
import com.testlubu.screenrecorder.services.FloatingControlService;
import com.testlubu.screenrecorder.ui.activities.EditVideoActivity;
import com.testlubu.screenrecorder.ui.activities.HomeActivity;
import com.testlubu.screenrecorder.videoTrimmer.utils.Toolbox;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

public class RecorderService extends Service implements ShakeEventManager.ShakeListener {
    private static int BITRATE;
    private static int DENSITY_DPI;
    private static int FPS;
    private static int HEIGHT;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static String SAVEPATH;
    private static int WIDTH;
    private static ArrayList<String> arrPart;
    private static String audioRecSource;
    public static boolean isRecording;
    private static int part = 0;
    private Intent data;
    private long elapsedTime = 0;
    private ServiceConnection floatingCameraConnection = new ServiceConnection() {
        /* class com.testlubu.screenrecorder.services.RecorderService.AnonymousClass3 */

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ((FloatingCameraViewService.ServiceBinder) iBinder).getService();
        }

        public void onServiceDisconnected(ComponentName componentName) {
            RecorderService.this.floatingControlService = null;
        }
    };
    private FloatingControlService floatingControlService;
    private boolean isBound = false;
    private boolean isShakeGestureActive;
    private boolean isStart = false;
    Handler mHandler = new Handler(Looper.getMainLooper()) {
        /* class com.testlubu.screenrecorder.services.RecorderService.AnonymousClass1 */

        public void handleMessage(Message message) {
            Toast.makeText(RecorderService.this, (int) R.string.screen_recording_stopped_toast, Toast.LENGTH_SHORT).show();
            RecorderService.this.showShareNotification();
            Utils.showDialogResult(RecorderService.this.getApplicationContext(), RecorderService.SAVEPATH);
        }
    };
    private MediaProjection mMediaProjection;
    private MediaProjectionCallback mMediaProjectionCallback;
    private MediaRecorder mMediaRecorder;
    private NotificationManager mNotificationManager;
    private ShakeEventManager mShakeDetector;
    private VirtualDisplay mVirtualDisplay;
    private SharedPreferences prefs;
    private int result;
    private int screenOrientation;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        /* class com.testlubu.screenrecorder.services.RecorderService.AnonymousClass2 */

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            RecorderService.this.floatingControlService = ((FloatingControlService.ServiceBinder) iBinder).getService();
            RecorderService.this.isBound = true;
        }

        public void onServiceDisconnected(ComponentName componentName) {
            RecorderService.this.floatingControlService = null;
            RecorderService.this.isBound = false;
        }
    };
    private boolean showCameraOverlay;
    private long startTime;
    private int time = 0;
    private WindowManager window;

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    static /* synthetic */ int access$708(RecorderService recorderService) {
        int i = recorderService.time;
        recorderService.time = i + 1;
        return i;
    }

    static {
        ORIENTATIONS.append(0, 0);
        ORIENTATIONS.append(1, 90);
        ORIENTATIONS.append(2, 180);
        ORIENTATIONS.append(3, 270);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public int onStartCommand(Intent intent, int i, int i2) {
        char c;
        if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannels();
        }
        Intent intent2 = new Intent(this, FloatingControlService.class);
        if (intent != null) {
            intent2.setAction(intent.getAction());
            startService(intent2);
            bindService(intent2, this.serviceConnection, BIND_AUTO_CREATE);
            this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String action = intent.getAction();
            switch (action.hashCode()) {
                case -1053033865:
                    if (action.equals(Const.SCREEN_RECORDING_STOP)) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -592011553:
                    if (action.equals(Const.SCREEN_RECORDING_DESTORY_SHAKE_GESTURE)) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -453103993:
                    if (action.equals(Const.SCREEN_RECORDING_START)) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 1599260844:
                    if (action.equals(Const.SCREEN_RECORDING_RESUME)) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 1780700019:
                    if (action.equals(Const.SCREEN_RECORDING_PAUSE)) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c != 0) {
                if (c == 1) {
                    pauseScreenRecording();
                } else if (c == 2) {
                    resumeScreenRecording();
                } else if (c != 3) {
                    if (c == 4) {
                        this.mShakeDetector.stop();
                        stopSelf();
                    }
                } else if (isRecording) {
                    stopRecording();
                }
            } else if (!isRecording) {
                this.screenOrientation = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRotation();
                this.data = (Intent) intent.getParcelableExtra(Const.RECORDER_INTENT_DATA);
                this.result = intent.getIntExtra(Const.RECORDER_INTENT_RESULT, -1);
                getValues();
                if (this.prefs.getBoolean(getString(R.string.preference_enable_target_app_key), false)) {
                    startAppBeforeRecording(this.prefs.getString(getString(R.string.preference_app_chooser_key), "none"));
                }
                if (this.isShakeGestureActive) {
                    this.mShakeDetector = new ShakeEventManager(this);
                    this.mShakeDetector.init(this);
                    Bitmap decodeResource = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
                    Intent intent3 = new Intent(this, RecorderService.class);
                    intent3.setAction(Const.SCREEN_RECORDING_DESTORY_SHAKE_GESTURE);
                    startNotificationForeGround(new NotificationCompat.Builder(this, Const.RECORDING_NOTIFICATION_CHANNEL_ID).setContentTitle("Waiting for device shake").setContentText("Shake your device to start recording or press this notification to cancel").setOngoing(true).setSmallIcon(R.drawable.ic_notification).setLargeIcon(Bitmap.createScaledBitmap(decodeResource, 128, 128, false)).setContentIntent(PendingIntent.getService(this, 0, intent3, 0)).build(), Const.SCREEN_RECORDER_SHARE_NOTIFICATION_ID);
                    Toast.makeText(this, (int) R.string.screenrecording_waiting_for_gesture_toast, 1).show();
                } else {
                    startRecording();
                }
            } else {
                Toast.makeText(this, (int) R.string.screenrecording_already_active_toast, 0).show();
            }
        }
        return START_STICKY;
    }

    private void stopRecording() {
        ObserverUtils.getInstance().notifyObservers(new EvbStopService());
        boolean z = this.isBound;
        if (z) {
            if (z) {
                this.floatingControlService.setRecordingState(Const.RecordingState.STOPPED);
            }
            unbindService(this.serviceConnection);
            if (this.showCameraOverlay) {
                unbindService(this.floatingCameraConnection);
            }
            Log.d(Const.TAG, "Unbinding connection service");
        }
        stopScreenSharing();
        isRecording = false;
        this.isStart = false;
    }

    private void startAppBeforeRecording(String str) {
        if (!str.equals("none")) {
            startActivity(getPackageManager().getLaunchIntentForPackage(str));
        }
    }

    @TargetApi(24)
    private void pauseScreenRecording() {
        if (isRecording) {
            if (Build.VERSION.SDK_INT < 24) {
                destroyMediaProjection();
            } else {
                this.mMediaRecorder.pause();
                isRecording = false;
            }
            this.elapsedTime += System.currentTimeMillis() - this.startTime;
            new Intent(this, RecorderService.class).setAction(Const.SCREEN_RECORDING_RESUME);
            updateNotification(createRecordingNotification().setUsesChronometer(false).build(), Const.SCREEN_RECORDER_NOTIFICATION_ID);
            Toast.makeText(this, (int) R.string.screen_recording_paused_toast, 0).show();
            if (this.isBound) {
                this.floatingControlService.setRecordingState(Const.RecordingState.PAUSED);
            }
        }
    }

    @TargetApi(24)
    private void resumeScreenRecording() {
        if (!isRecording) {
            if (Build.VERSION.SDK_INT < 24) {
                startRecording();
            } else {
                this.mMediaRecorder.resume();
            }
            isRecording = true;
            this.startTime = System.currentTimeMillis();
            new Intent(this, RecorderService.class).setAction(Const.SCREEN_RECORDING_PAUSE);
            Toast.makeText(this, (int) R.string.screen_recording_resumed_toast, 0).show();
            if (this.isBound) {
                this.floatingControlService.setRecordingState(Const.RecordingState.RECORDING);
            }
        }
    }

    private void startRecording() {
        FloatingControlService.isRecording = true;
        this.mMediaRecorder = new MediaRecorder();
        initRecorder();
        this.mMediaProjectionCallback = new MediaProjectionCallback();
        this.mMediaProjection = ((MediaProjectionManager) getSystemService("media_projection")).getMediaProjection(this.result, this.data);
        this.mMediaProjection.registerCallback(this.mMediaProjectionCallback, null);
        this.mVirtualDisplay = createVirtualDisplay();
        try {
            this.mMediaRecorder.start();
            if (this.showCameraOverlay) {
                if (!UtilsApp.isMyServiceRunning(FloatingCameraViewService.class, this)) {
                    Intent intent = new Intent(this, FloatingCameraViewService.class);
                    startService(intent);
                    bindService(intent, this.floatingCameraConnection, 1);
                }
            }
            if (this.isBound) {
                this.floatingControlService.setRecordingState(Const.RecordingState.RECORDING);
            }
            isRecording = true;
            if (part == 0) {
                Toast.makeText(this, (int) R.string.screen_recording_started_toast, 0).show();
                this.isStart = true;
                new TimeCount().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
            }
        } catch (Exception unused) {
            Log.e(Const.TAG, "Mediarecorder reached Illegal state exception. Did you start the recording twice?");
            Toast.makeText(this, (int) R.string.recording_failed_toast, 0).show();
            isRecording = false;
            this.mMediaProjection.stop();
            stopSelf();
        }
        if (Build.VERSION.SDK_INT >= 24) {
            this.startTime = System.currentTimeMillis();
            Intent intent2 = new Intent(this, RecorderService.class);
            intent2.setAction(Const.SCREEN_RECORDING_PAUSE);
            new NotificationCompat.Action(17301539, getString(R.string.screen_recording_notification_action_pause), PendingIntent.getService(this, 0, intent2, 0));
            startNotificationForeGround(createRecordingNotification().build(), Const.SCREEN_RECORDER_NOTIFICATION_ID);
            return;
        }
        startNotificationForeGround(createRecordingNotification().build(), Const.SCREEN_RECORDER_NOTIFICATION_ID);
    }

    /* access modifiers changed from: private */
    public class TimeCount extends AsyncTask<Void, Integer, Void> {
        private TimeCount() {
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... voidArr) {
            ObserverUtils.getInstance().notifyObservers(new EvbStartRecord());
            while (RecorderService.this.isStart) {
                if (RecorderService.isRecording) {
                    RecorderService.access$708(RecorderService.this);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                publishProgress(Integer.valueOf(RecorderService.this.time));
            }
            return null;
        }

        /* access modifiers changed from: protected */
        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            ObserverUtils instance = ObserverUtils.getInstance();
            instance.notifyObservers(new EvbRecordTime(Toolbox.converTime(numArr[0] + "")));
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Void r3) {
            super.onPostExecute(r3);
            ObserverUtils.getInstance().notifyObservers(new EvbStageRecord(false));
            RecorderService.this.time = 0;
        }
    }

    private VirtualDisplay createVirtualDisplay() {
        return this.mMediaProjection.createVirtualDisplay("MainActivity", WIDTH, HEIGHT, DENSITY_DPI, 16, this.mMediaRecorder.getSurface(), null, null);
    }

    private void initRecorder() {
        String str = audioRecSource;
        boolean z = true;
        if (((str.hashCode() == 49 && str.equals(PrefUtils.VALUE_AUDIO)) ? (char) 0 : 65535) != 0) {
            z = false;
        } else {
            this.mMediaRecorder.setAudioSource(1);
        }
        this.mMediaRecorder.setVideoSource(2);
        this.mMediaRecorder.setOutputFormat(2);
        if (Build.VERSION.SDK_INT < 24) {
            String str2 = SAVEPATH;
            String replaceAll = str2.replaceAll("[.]mp4", "." + part + "_.mp4");
            arrPart.add(replaceAll);
            this.mMediaRecorder.setOutputFile(replaceAll);
        } else {
            this.mMediaRecorder.setOutputFile(SAVEPATH);
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.window = (WindowManager) getSystemService("window");
        this.window.getDefaultDisplay().getRealMetrics(displayMetrics);
        DENSITY_DPI = displayMetrics.densityDpi;
        int parseInt = Integer.parseInt(this.prefs.getString(getString(R.string.res_key), Integer.toString(displayMetrics.widthPixels)));
        float aspectRatio = getAspectRatio(displayMetrics);
        Log.d(Const.TAG, "resolution service: [Width: " + parseInt + ", Height: " + (((float) parseInt) * aspectRatio) + ", aspect ratio: " + aspectRatio + "]");
        WIDTH = parseInt;
        HEIGHT = (int)(((float) parseInt) * aspectRatio);
        this.mMediaRecorder.setVideoSize(WIDTH, HEIGHT);
        this.mMediaRecorder.setVideoEncoder(2);
        if (z) {
            this.mMediaRecorder.setAudioEncoder(3);
        }
        this.mMediaRecorder.setVideoEncodingBitRate(BITRATE);
        this.mMediaRecorder.setVideoFrameRate(FPS);
        try {
            this.mMediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(Const.TAG, "Loi ne initRecorder " + e.getMessage());
        }
    }

    private int getBestVideoEncoder() {
        if (getMediaCodecFor("video/hevc")) {
            if (Build.VERSION.SDK_INT >= 24) {
                return 5;
            }
        } else if (getMediaCodecFor("video/avc")) {
            return 2;
        }
        return 0;
    }

    private boolean getMediaCodecFor(String str) {
        String findEncoderForFormat = new MediaCodecList(0).findEncoderForFormat(MediaFormat.createVideoFormat(str, WIDTH, HEIGHT));
        if (findEncoderForFormat == null) {
            Log.d("Null Encoder: ", str);
            return false;
        }
        Log.d("Encoder", findEncoderForFormat);
        return !findEncoderForFormat.startsWith("OMX.google");
    }

    private long getFreeSpaceInBytes(String str) {
        long availableBytes = new StatFs(str).getAvailableBytes();
        Log.d(Const.TAG, "Free space in GB: " + (availableBytes / 1000000000));
        return availableBytes;
    }

    @TargetApi(26)
    private void createNotificationChannels() {
        ArrayList arrayList = new ArrayList();
        NotificationChannel notificationChannel = new NotificationChannel(Const.RECORDING_NOTIFICATION_CHANNEL_ID, Const.RECORDING_NOTIFICATION_CHANNEL_NAME, 3);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(SupportMenu.CATEGORY_MASK);
        notificationChannel.setShowBadge(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLockscreenVisibility(1);
        arrayList.add(notificationChannel);
        NotificationChannel notificationChannel2 = new NotificationChannel(Const.SHARE_NOTIFICATION_CHANNEL_ID, Const.SHARE_NOTIFICATION_CHANNEL_NAME, 3);
        notificationChannel2.enableLights(true);
        notificationChannel2.setLightColor(SupportMenu.CATEGORY_MASK);
        notificationChannel2.setShowBadge(true);
        notificationChannel2.enableVibration(true);
        notificationChannel2.setLockscreenVisibility(1);
        arrayList.add(notificationChannel2);
        getManager().createNotificationChannels(arrayList);
    }

    private NotificationCompat.Builder createRecordingNotification() {
        Bitmap decodeResource = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        new Intent(this, RecorderService.class).setAction(Const.SCREEN_RECORDING_STOP);
        return new NotificationCompat.Builder(this, Const.RECORDING_NOTIFICATION_CHANNEL_ID).setContentTitle(getResources().getString(R.string.screen_recording_notification_title)).setTicker(getResources().getString(R.string.screen_recording_notification_title)).setSmallIcon(R.drawable.ic_notification).setLargeIcon(Bitmap.createScaledBitmap(decodeResource, 128, 128, false)).setUsesChronometer(true).setOngoing(true).setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, HomeActivity.class), 0)).setPriority(3);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showShareNotification() {
        Bitmap decodeResource = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        new Intent(this, EditVideoActivity.class).putExtra(Const.VIDEO_EDIT_URI_KEY, SAVEPATH);
        updateNotification(new NotificationCompat.Builder(this, Const.SHARE_NOTIFICATION_CHANNEL_ID).setContentTitle(getString(R.string.share_intent_notification_title)).setContentText(getString(R.string.share_intent_notification_content)).setSmallIcon(R.drawable.ic_notification).setLargeIcon(Bitmap.createScaledBitmap(decodeResource, 128, 128, false)).setAutoCancel(true).setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, HomeActivity.class).setAction(Const.SCREEN_RECORDER_VIDEOS_LIST_FRAGMENT_INTENT), 134217728)).build(), Const.SCREEN_RECORDER_SHARE_NOTIFICATION_ID);
    }

    private void startNotificationForeGround(Notification notification, int i) {
        startForeground(i, notification);
    }

    private void updateNotification(Notification notification, int i) {
        getManager().notify(i, notification);
    }

    private NotificationManager getManager() {
        if (this.mNotificationManager == null) {
            this.mNotificationManager = (NotificationManager) getSystemService("notification");
        }
        return this.mNotificationManager;
    }

    public void onDestroy() {
        Log.d(Const.TAG, "Recorder service destroyed");
        unbindService(this.serviceConnection);
        this.isStart = false;
        super.onDestroy();
    }

    public void getValues() {
        setWidthHeight(getResolution());
        ArrayList<String> arrayList = arrPart;
        if (arrayList == null) {
            arrPart = new ArrayList<>();
        } else {
            arrayList.clear();
        }
        FPS = Integer.parseInt(this.prefs.getString(getString(R.string.fps_key), PrefUtils.VALUE_FRAMES));
        BITRATE = Integer.parseInt(this.prefs.getString(getString(R.string.bitrate_key), PrefUtils.VALUE_BITRATE));
        audioRecSource = this.prefs.getString(getString(R.string.audiorec_key), PrefUtils.VALUE_AUDIO);
        SharedPreferences sharedPreferences = this.prefs;
        String string = getString(R.string.savelocation_key);
        String string2 = sharedPreferences.getString(string, Environment.getExternalStorageDirectory() + File.separator + Const.APPDIR);
        File file = new File(string2);
        if (Environment.getExternalStorageState().equals("mounted") && !file.isDirectory()) {
            file.mkdirs();
        }
        this.showCameraOverlay = this.prefs.getBoolean(getString(R.string.preference_camera_overlay_key), false);
        String fileSaveName = getFileSaveName();
        SAVEPATH = string2 + File.separator + fileSaveName + ".mp4";
        this.isShakeGestureActive = this.prefs.getBoolean(getString(R.string.preference_shake_gesture_key), false);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x004b  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0072  */
    private void setWidthHeight(String str) {
        char c;
        String[] split = str.split("x");
        String string = this.prefs.getString(getString(R.string.orientation_key), PrefUtils.VALUE_ORIENTATION);
        int hashCode = string.hashCode();
        if (hashCode != 3005871) {
            if (hashCode != 729267099) {
                if (hashCode == 1430647483 && string.equals("landscape")) {
                    c = 2;
                    if (c != 0) {
                        int i = this.screenOrientation;
                        if (i == 0 || i == 2) {
                            WIDTH = Integer.parseInt(split[0]);
                            HEIGHT = Integer.parseInt(split[1]);
                        } else {
                            HEIGHT = Integer.parseInt(split[0]);
                            WIDTH = Integer.parseInt(split[1]);
                        }
                    } else if (c == 1) {
                        WIDTH = Integer.parseInt(split[0]);
                        HEIGHT = Integer.parseInt(split[1]);
                    } else if (c == 2) {
                        HEIGHT = Integer.parseInt(split[0]);
                        WIDTH = Integer.parseInt(split[1]);
                    }
                    Log.d(Const.TAG, "Width: " + WIDTH + ",Height:" + HEIGHT);
                }
            } else if (string.equals("portrait")) {
                c = 1;
                if (c != 0) {
                }
                Log.d(Const.TAG, "Width: " + WIDTH + ",Height:" + HEIGHT);
            }
        } else if (string.equals(PrefUtils.VALUE_ORIENTATION)) {
            c = 0;
            if (c != 0) {
            }
            Log.d(Const.TAG, "Width: " + WIDTH + ",Height:" + HEIGHT);
        }
        c = 65535;
        if (c != 0) {
        }
        Log.d(Const.TAG, "Width: " + WIDTH + ",Height:" + HEIGHT);
    }

    private String getResolution() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.window = (WindowManager) getSystemService("window");
        this.window.getDefaultDisplay().getRealMetrics(displayMetrics);
        DENSITY_DPI = displayMetrics.densityDpi;
        int parseInt = Integer.parseInt(this.prefs.getString(getString(R.string.res_key), Integer.toString(displayMetrics.widthPixels)));
        float aspectRatio = getAspectRatio(displayMetrics);
        String str = parseInt + "x" + calculateClosestHeight(parseInt, aspectRatio);
        Log.d(Const.TAG, "resolution service: [Width: " + parseInt + ", Height: " + (((float) parseInt) * aspectRatio) + ", aspect ratio: " + aspectRatio + "]");
        return str;
    }

    private int calculateClosestHeight(int i, float f) {
        int i2 = (int) (((float) i) * f);
        Log.d(Const.TAG, "Calculated width=" + i2);
        Log.d(Const.TAG, "Aspect ratio: " + f);
        int i3 = i2 / 16;
        if (i3 == 0) {
            return i2;
        }
        Log.d(Const.TAG, i2 + " not divisible by 16");
        int i4 = i3 * 16;
        Log.d(Const.TAG, "Maximum possible height is " + i4);
        return i4;
    }

    private float getAspectRatio(DisplayMetrics displayMetrics) {
        float f = (float) displayMetrics.widthPixels;
        float f2 = (float) displayMetrics.heightPixels;
        return f > f2 ? f / f2 : f2 / f;
    }

    private String getFileSaveName() {
        String string = this.prefs.getString(getString(R.string.filename_key), PrefUtils.VALUE_NAME_FORMAT);
        String string2 = this.prefs.getString(getString(R.string.fileprefix_key), PrefUtils.VALUE_NAME_PREFIX);
        Date time2 = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(string);
        return string2 + "_" + simpleDateFormat.format(time2);
    }

    private void destroyMediaProjection() {
        this.mMediaRecorder.stop();
        if (Build.VERSION.SDK_INT >= 24) {
            indexFile();
        } else {
            part++;
        }
        this.mMediaRecorder.reset();
        this.mVirtualDisplay.release();
        this.mMediaRecorder.release();
        MediaProjection mediaProjection = this.mMediaProjection;
        if (mediaProjection != null) {
            mediaProjection.unregisterCallback(this.mMediaProjectionCallback);
            this.mMediaProjection.stop();
            this.mMediaProjection = null;
        }
        isRecording = false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void indexFile() {
        ArrayList arrayList = new ArrayList();
        arrayList.add(SAVEPATH);
        MediaScannerConnection.scanFile(this, (String[]) arrayList.toArray(new String[arrayList.size()]), null, new MediaScannerConnection.OnScanCompletedListener() {
            /* class com.testlubu.screenrecorder.services.RecorderService.AnonymousClass4 */

            public void onScanCompleted(String str, Uri uri) {
                RecorderService.this.mHandler.obtainMessage().sendToTarget();
                RecorderService.this.stopSelf();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopScreenSharing() {
        if (this.mVirtualDisplay == null) {
            Log.d(Const.TAG, "Virtual display is null. Screen sharing already stopped");
            return;
        }
        if (Build.VERSION.SDK_INT >= 24) {
            destroyMediaProjection();
        } else {
            if (isRecording) {
                destroyMediaProjection();
            }
            mergeMediaFiles(arrPart, SAVEPATH);
        }
        Iterator<String> it = arrPart.iterator();
        while (it.hasNext()) {
            Log.e(Const.TAG, it.next());
        }
    }

    public void mergeMediaFiles(ArrayList<String> arrayList, String str) {
        new MyTask(arrayList, str).execute(new Void[0]);
    }

    @Override // com.testlubu.screenrecorder.gesture.ShakeEventManager.ShakeListener
    public void onShake() {
        if (!isRecording) {
            Vibrator vibrator = (Vibrator) getSystemService("vibrator");
            getManager().cancel(Const.SCREEN_RECORDER_WAITING_FOR_SHAKE_NOTIFICATION_ID);
            if (Build.VERSION.SDK_INT < 26) {
                vibrator.vibrate(500);
            } else {
                VibrationEffect.createOneShot(500, 255);
            }
            startRecording();
            return;
        }
        Intent intent = new Intent(this, RecorderService.class);
        intent.setAction(Const.SCREEN_RECORDING_STOP);
        startService(intent);
        this.mShakeDetector.stop();
    }

    /* access modifiers changed from: private */
    public class MediaProjectionCallback extends MediaProjection.Callback {
        private MediaProjectionCallback() {
        }

        public void onStop() {
            Log.v(Const.TAG, "Recording Stopped");
            RecorderService.this.stopScreenSharing();
        }
    }

    public class MyTask extends AsyncTask<Void, Void, Void> {
        ArrayList<String> sourceFiles;
        String targetFile;

        public MyTask(ArrayList<String> arrayList, String str) {
            this.sourceFiles = arrayList;
            this.targetFile = str;
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... voidArr) {
            try {
                Log.e(Const.TAG, "start mergeVideos");
                Log.e(Const.TAG, "target: " + this.targetFile);
                ArrayList<Movie> arrayList = new ArrayList();
                Iterator<String> it = this.sourceFiles.iterator();
                while (it.hasNext()) {
                    String next = it.next();
                    arrayList.add(MovieCreator.build(next));
                    Log.e(Const.TAG, next);
                }
                LinkedList linkedList = new LinkedList();
                LinkedList linkedList2 = new LinkedList();
                for (Movie movie : arrayList) {
                    for (Track track : movie.getTracks()) {
                        if (track.getHandler().equals("vide")) {
                            linkedList.add(track);
                        }
                        if (track.getHandler().equals("soun")) {
                            linkedList2.add(track);
                        }
                    }
                }
                Movie movie2 = new Movie();
                if (!linkedList.isEmpty()) {
                    movie2.addTrack(new AppendTrack((Track[]) linkedList.toArray(new Track[linkedList.size()])));
                }
                if (Build.VERSION.SDK_INT < 24 && !linkedList2.isEmpty()) {
                    movie2.addTrack(new AppendTrack((Track[]) linkedList2.toArray(new Track[linkedList2.size()])));
                }
                Container build = new DefaultMp4Builder().build(movie2);
                FileChannel channel = new RandomAccessFile(String.format(this.targetFile, new Object[0]), "rw").getChannel();
                build.writeContainer(channel);
                channel.close();
                RecorderService.this.indexFile();
                Iterator it2 = RecorderService.arrPart.iterator();
                while (it2.hasNext()) {
                    File file = new File((String) it2.next());
                    if (file.exists()) {
                        file.delete();
                    }
                }
                int unused = RecorderService.part = 0;
                RecorderService.arrPart.clear();
                Log.e(Const.TAG, "finish mergeVideos");
                return null;
            } catch (Exception e) {
                Log.e(Const.TAG, "Error merging media files. exception: " + e.getMessage());
                return null;
            }
        }

        /* access modifiers changed from: protected */
        public void onPreExecute() {
            super.onPreExecute();
            Log.e(Const.TAG, "onPreExcute");
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Void r2) {
            super.onPostExecute(r2);
            Log.e(Const.TAG, "onPostExcute");
            RecorderService.this.stopForeground(true);
        }
    }
}
