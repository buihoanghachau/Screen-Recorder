package com.testlubu.screenrecorder.services;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import androidx.core.internal.view.SupportMenu;
import androidx.recyclerview.widget.RecyclerView;
import com.raed.drawingview.BrushView;
import com.raed.drawingview.DrawingView;
import com.raed.drawingview.brushes.BrushSettings;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.adapter.ColorAdapter;
import com.testlubu.screenrecorder.common.Const;
import com.testlubu.screenrecorder.common.Utils;
import com.testlubu.screenrecorder.ui.activities.ScreenShotActivity;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class BrushService extends Service implements View.OnTouchListener, View.OnClickListener {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final String BUNDLE_TYPE = "TYPE";
    private static final int NOTIFICATION_ID = 161;
    public static final int TYPE = 1001;
    private ColorAdapter colorAdapter;
    private DrawingView drawingView;
    private ConstraintLayout mLayout;
    private NotificationManager mNotificationManager;
    private WindowManager.LayoutParams mParams;
    private String path = "";
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
        this.mLayout = (ConstraintLayout) ((LayoutInflater) getApplicationContext().getSystemService("layout_inflater")).inflate(R.layout.layout_main_brush, (ViewGroup) null);
        this.drawingView = (DrawingView) this.mLayout.findViewById(R.id.drawview);
        RecyclerView recyclerView = (RecyclerView) this.mLayout.findViewById(R.id.rcv);
        final ConstraintLayout constraintLayout = (ConstraintLayout) this.mLayout.findViewById(R.id.container_color);
        final LinearLayout linearLayout = (LinearLayout) this.mLayout.findViewById(R.id.layout_brush);
        ImageView imageView = (ImageView) this.mLayout.findViewById(R.id.imgCamera);
        ImageView imageView2 = (ImageView) this.mLayout.findViewById(R.id.imgPaint);
        ImageView imageView3 = (ImageView) this.mLayout.findViewById(R.id.imgEraser);
        ImageView imageView4 = (ImageView) this.mLayout.findViewById(R.id.imgUndo);
        SeekBar seekBar = (SeekBar) this.mLayout.findViewById(R.id.size_seek_bar);
        ((ImageView) this.mLayout.findViewById(R.id.imgClose)).setOnClickListener(this);
        ((ImageView) this.mLayout.findViewById(R.id.imv_close)).setOnClickListener(new View.OnClickListener() {
            /* class com.testlubu.screenrecorder.services.BrushService.AnonymousClass1 */

            public void onClick(View view) {
                constraintLayout.setVisibility(8);
            }
        });
        this.mParams = new WindowManager.LayoutParams(-1, -1, 2038, 8, -3);
        if (Build.VERSION.SDK_INT < 26) {
            this.mParams.type = 2005;
        }
        final BrushView brushView = (BrushView) this.mLayout.findViewById(R.id.brush_view);
        brushView.setDrawingView(this.drawingView);
        final BrushSettings brushSettings = this.drawingView.getBrushSettings();
        brushSettings.setSelectedBrush(0);
        this.drawingView.setUndoAndRedoEnable(true);
        imageView3.setOnClickListener(new View.OnClickListener() {
            /* class com.testlubu.screenrecorder.services.BrushService.AnonymousClass2 */

            public void onClick(View view) {
                brushSettings.setSelectedBrush(4);
            }
        });
        imageView4.setOnClickListener(new View.OnClickListener() {
            /* class com.testlubu.screenrecorder.services.BrushService.AnonymousClass3 */

            public void onClick(View view) {
                BrushService.this.drawingView.undo();
            }
        });
        imageView2.setOnClickListener(new View.OnClickListener() {
            /* class com.testlubu.screenrecorder.services.BrushService.AnonymousClass4 */

            public void onClick(View view) {
                brushSettings.setSelectedBrush(0);
                constraintLayout.setVisibility(0);
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            /* class com.testlubu.screenrecorder.services.BrushService.AnonymousClass5 */

            public void onClick(View view) {
                linearLayout.setVisibility(8);
                BrushService.this.screenShot();
            }
        });
        this.windowManager.addView(this.mLayout, this.mParams);
        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /* class com.testlubu.screenrecorder.services.BrushService.AnonymousClass6 */

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                brushSettings.setSelectedBrushSize(((float) i) / 100.0f);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                brushView.setVisibility(0);
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                brushView.setVisibility(8);
            }
        });
        this.colorAdapter = new ColorAdapter(this, initColors(), new ColorAdapter.OnClick() {
            /* class com.testlubu.screenrecorder.services.BrushService.AnonymousClass7 */

            @Override // com.testlubu.screenrecorder.adapter.ColorAdapter.OnClick
            public void onClickColor(int i) {
                brushSettings.setColor(i);
            }
        });
        recyclerView.setAdapter(this.colorAdapter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void screenShot() {
        Intent intent = new Intent(this, ScreenShotActivity.class);
        intent.putExtra(BUNDLE_TYPE, 1001);
        intent.setFlags(268435456);
        startActivity(intent);
    }

    private ArrayList<Integer> initColors() {
        ArrayList<Integer> arrayList = new ArrayList<>();
        arrayList.add(Integer.valueOf(Color.parseColor("#ffffff")));
        arrayList.add(Integer.valueOf(Color.parseColor("#039BE5")));
        arrayList.add(Integer.valueOf(Color.parseColor("#00ACC1")));
        arrayList.add(Integer.valueOf(Color.parseColor("#00897B")));
        arrayList.add(Integer.valueOf(Color.parseColor("#FDD835")));
        arrayList.add(Integer.valueOf(Color.parseColor("#FFB300")));
        arrayList.add(Integer.valueOf(Color.parseColor("#F4511E")));
        return arrayList;
    }

    private void saveResult(Bitmap bitmap) {
        String str = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.savelocation_key), Environment.getExternalStorageDirectory() + File.separator + Const.APPDIR) + "/Screenshot_" + getDateTime() + ".png";
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(str));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        showNotificationScreenshot(str);
        sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.parse("file://" + str)));
        stopSelf();
    }

    private void showNotificationScreenshot(String str) {
        Utils.showDialogResult(getApplicationContext(), str);
        Bitmap decodeResource = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(FileProvider.getUriForFile(this, "com.testlubu.screenrecorder.provider", new File(str)), "image/*");
        intent.addFlags(1);
        PendingIntent activity = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "");
        builder.setContentIntent(activity).setContentTitle(getString(R.string.share_intent_notification_title_photo)).setContentText(getString(R.string.share_intent_notification_content_photo)).setSmallIcon(R.drawable.ic_notification).setLargeIcon(Bitmap.createScaledBitmap(decodeResource, 128, 128, false)).setAutoCancel(true);
        this.mNotificationManager.cancel(NOTIFICATION_ID);
        if (Utils.isAndroid26()) {
            NotificationChannel notificationChannel = new NotificationChannel("my_channel_id", "NOTIFICATION_CHANNEL_NAME", 4);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(SupportMenu.CATEGORY_MASK);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            builder.setChannelId("my_channel_id");
            this.mNotificationManager.createNotificationChannel(notificationChannel);
        }
        this.mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public String getDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().getTime());
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        initView();
        return 2;
    }

    public void onClick(View view) {
        if (view.getId() == R.id.imgClose) {
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
