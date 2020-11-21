package com.testlubu.screenrecorder.ui.activities;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import androidx.core.internal.view.SupportMenu;
import com.testlubu.screenrecorder.BaseActivity;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.common.Const;
import com.testlubu.screenrecorder.common.Utils;
import com.testlubu.screenrecorder.services.BrushService;
import com.testlubu.screenrecorder.services.FloatingControlService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ScreenShotActivity extends BaseActivity {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int NOTIFICATION_ID = 161;
    private int IMAGES_PRODUCED = 0;
    private String STORE_DIRECTORY;
    private int VIRTUAL_DISPLAY_FLAGS = 9;
    private Handler handler = new Handler();
    private int mDensity;
    private Display mDisplay;
    private int mHeight;
    private ImageReader mImageReader;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mMediaProjectionManager;
    private NotificationManager mNotificationManager;
    private int mResultCode = 0;
    private Intent mResultData = null;
    private VirtualDisplay mVirtualDisplay;
    private int mWidth;
    private DisplayMetrics metrics;
    private Runnable runnable = new Runnable() {
        /* class com.testlubu.screenrecorder.ui.activities.ScreenShotActivity.AnonymousClass1 */

        public void run() {
            ScreenShotActivity.this.activeScreenCapture();
        }
    };
    private int type = 0;

    /* access modifiers changed from: private */
    public class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        private ImageAvailableListener() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0101, code lost:
            r3 = r8;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:0x012c, code lost:
            r12 = e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:39:0x012d, code lost:
            r3 = r8;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:0x0133, code lost:
            r12 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:0x0134, code lost:
            r12.printStackTrace();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:45:0x0139, code lost:
            r5.recycle();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:47:0x013e, code lost:
            r4.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:53:0x0155, code lost:
            r3.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:55:0x015a, code lost:
            r5.recycle();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:57:0x015f, code lost:
            r12 = new android.content.Intent(com.testlubu.screenrecorder.common.Const.ACTION_SCREEN_SHOT);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:58:0x0165, code lost:
            r4.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:60:0x016a, code lost:
            if (r3 != null) goto L_0x016c;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:61:0x016c, code lost:
            r3.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:62:0x016f, code lost:
            if (r4 != null) goto L_0x0171;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:63:0x0171, code lost:
            r4.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:64:0x0175, code lost:
            r12 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:65:0x0176, code lost:
            r12.printStackTrace();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:66:0x0179, code lost:
            if (0 != 0) goto L_0x017b;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:67:0x017b, code lost:
            r3.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:68:0x017e, code lost:
            if (r4 == null) goto L_0x0180;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:69:0x0180, code lost:
            r12 = new android.content.Intent(com.testlubu.screenrecorder.common.Const.ACTION_SCREEN_SHOT);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:70:0x0186, code lost:
            r4.close();
         */
        /* JADX WARNING: Exception block dominator not found, dom blocks: [B:15:0x00f3, B:40:0x012f] */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x00f3 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:40:0x012f */
        /* JADX WARNING: Removed duplicated region for block: B:18:0x00f8 A[Catch:{ Exception -> 0x012c, Throwable -> 0x0101 }] */
        /* JADX WARNING: Removed duplicated region for block: B:20:0x00fd A[Catch:{ Exception -> 0x012c, Throwable -> 0x0101 }] */
        /* JADX WARNING: Removed duplicated region for block: B:22:? A[ExcHandler: Throwable (unused java.lang.Throwable), SYNTHETIC, Splitter:B:15:0x00f3] */
        /* JADX WARNING: Removed duplicated region for block: B:38:0x012c A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:15:0x00f3] */
        /* JADX WARNING: Removed duplicated region for block: B:40:0x012f A[SYNTHETIC, Splitter:B:40:0x012f] */
        /* JADX WARNING: Removed duplicated region for block: B:45:0x0139  */
        /* JADX WARNING: Removed duplicated region for block: B:47:0x013e  */
        /* JADX WARNING: Removed duplicated region for block: B:53:0x0155  */
        /* JADX WARNING: Removed duplicated region for block: B:55:0x015a  */
        /* JADX WARNING: Removed duplicated region for block: B:57:0x015f  */
        /* JADX WARNING: Removed duplicated region for block: B:58:0x0165  */
        /* JADX WARNING: Removed duplicated region for block: B:59:? A[ExcHandler: Throwable (unused java.lang.Throwable), SYNTHETIC, Splitter:B:4:0x0012] */
        /* JADX WARNING: Removed duplicated region for block: B:61:0x016c  */
        /* JADX WARNING: Removed duplicated region for block: B:63:0x0171  */
        /* JADX WARNING: Removed duplicated region for block: B:72:0x018b A[SYNTHETIC, Splitter:B:72:0x018b] */
        /* JADX WARNING: Removed duplicated region for block: B:77:0x0195  */
        /* JADX WARNING: Unknown top exception splitter block from list: {B:15:0x00f3=Splitter:B:15:0x00f3, B:40:0x012f=Splitter:B:40:0x012f} */
        public void onImageAvailable(ImageReader imageReader) {
            Intent intent;
            Image acquireLatestImage;
            Bitmap createBitmap;
            FileOutputStream fileOutputStream;
            FileOutputStream fileOutputStream2 = null;
            try {
                acquireLatestImage = ScreenShotActivity.this.mImageReader.acquireLatestImage();
                if (acquireLatestImage != null) {
                    try {
                        Image.Plane[] planes = acquireLatestImage.getPlanes();
                        ByteBuffer buffer = planes[0].getBuffer();
                        int pixelStride = planes[0].getPixelStride();
                        createBitmap = Bitmap.createBitmap(ScreenShotActivity.this.mWidth + ((planes[0].getRowStride() - (ScreenShotActivity.this.mWidth * pixelStride)) / pixelStride), ScreenShotActivity.this.mHeight, Bitmap.Config.ARGB_8888);
                        createBitmap.copyPixelsFromBuffer(buffer);
                        if (acquireLatestImage != null) {
                            acquireLatestImage.close();
                        }
                        if (ScreenShotActivity.this.IMAGES_PRODUCED == 0) {
                            String str = ScreenShotActivity.this.STORE_DIRECTORY + "/Screenshot_" + ScreenShotActivity.this.getDateTime() + ".png";
                            fileOutputStream = new FileOutputStream(str);
                            try {
                                Utils.CropBitmapTransparency(Bitmap.createBitmap(createBitmap, 0, 0, ScreenShotActivity.this.mWidth, ScreenShotActivity.this.mHeight)).compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                                acquireLatestImage.close();
                                ScreenShotActivity.this.IMAGES_PRODUCED++;
                                ScreenShotActivity.this.stopBrushService();
                                ScreenShotActivity.this.showNotificationScreenshot(str);
                                ScreenShotActivity.this.stopScreenCapture();
                                ScreenShotActivity.this.sendBroadcast(new Intent(Const.ACTION_SCREEN_SHOT).putExtra("capture", 1));
                                ScreenShotActivity.this.tearDownMediaProjection();
                                ScreenShotActivity.this.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.parse("file://" + str)));
                                ScreenShotActivity.this.IMAGES_PRODUCED = 0;
                            } catch (Exception e) {
                                e.printStackTrace();
                                if (imageReader != null) {
                                    try {
                                        imageReader.close();
                                    } catch (Exception e2) {
                                        e2.printStackTrace();
                                    } catch (Throwable unknown) {
                                    }
                                    fileOutputStream.close();
                                    if (createBitmap != null) {
                                    }
                                    if (acquireLatestImage != null) {
                                    }
                                }
                                if (createBitmap != null) {
                                    createBitmap.recycle();
                                }
                                if (acquireLatestImage == null) {
                                    intent = new Intent(Const.ACTION_SCREEN_SHOT);
                                    ScreenShotActivity.this.sendBroadcast(intent.putExtra("capture", 1));
                                    return;
                                }
                                acquireLatestImage.close();
                            } catch (Throwable unknown) {
                                fileOutputStream.close();
                                if (createBitmap != null) {
                                }
                                if (acquireLatestImage != null) {
                                }
                            }
                            try {
                                fileOutputStream.close();
                                if (createBitmap != null) {
                                    createBitmap.recycle();
                                }
                                if (acquireLatestImage != null) {
                                    acquireLatestImage.close();
                                }
                                fileOutputStream2 = fileOutputStream;
                            } catch (Exception e3) {
                            } catch (Throwable unused) {
                            }
                        }
                        ScreenShotActivity.this.IMAGES_PRODUCED++;
                    } catch (Exception e4) {
                        Exception e5 = e4;
                        e5.printStackTrace();
                    } catch (Throwable unused2) {
                    }
                }
                if (fileOutputStream2 != null) {
                    try {
                        fileOutputStream2.close();
                    } catch (Exception e6) {
                        e6.printStackTrace();
                    }
                }
                if (acquireLatestImage != null) {
                    acquireLatestImage.close();
                }
                ScreenShotActivity.this.sendBroadcast(new Intent(Const.ACTION_SCREEN_SHOT).putExtra("capture", 1));
            } catch (Exception e7) {
                e7.printStackTrace();
                if (0 != 0) {
                    try {
                        fileOutputStream2.close();
                    } catch (IOException e8) {
                        e8.printStackTrace();
                    }
                }
                intent = new Intent(Const.ACTION_SCREEN_SHOT);
            } catch (Throwable th) {
                ScreenShotActivity.this.sendBroadcast(new Intent(Const.ACTION_SCREEN_SHOT).putExtra("capture", 1));
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopBrushService() {
        if (this.type == 1001) {
            stopService(new Intent(this, BrushService.class));
        }
    }

    /* access modifiers changed from: protected */
    @Override // androidx.core.app.ComponentActivity, androidx.appcompat.app.AppCompatActivity, com.testlubu.screenrecorder.BaseActivity, androidx.fragment.app.FragmentActivity
    @SuppressLint({"WrongConstant"})
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (FloatingControlService.isCountdown) {
            finish();
            return;
        }
        sendBroadcast(new Intent(Const.ACTION_SCREEN_SHOT).putExtra("capture", 0));
        if (Build.VERSION.SDK_INT < 23 || checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
            if (!(getIntent() == null || getIntent().getExtras() == null)) {
                this.type = ((Integer) getIntent().getExtras().get(BrushService.BUNDLE_TYPE)).intValue();
            }
            this.metrics = getResources().getDisplayMetrics();
            this.mDensity = this.metrics.densityDpi;
            this.mDisplay = getWindowManager().getDefaultDisplay();
            this.mNotificationManager = (NotificationManager) getSystemService("notification");
            this.mMediaProjectionManager = (MediaProjectionManager) getSystemService("media_projection");
            this.handler.postDelayed(this.runnable, 200);
            return;
        }
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 131);
    }

    private void setUpMediaProjection() {
        try {
            if (this.mMediaProjection == null) {
                this.mMediaProjection = this.mMediaProjectionManager.getMediaProjection(this.mResultCode, this.mResultData);
            }
        } catch (Exception unused) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void activeScreenCapture() {
        startActivityForResult(this.mMediaProjectionManager.createScreenCaptureIntent(), 100);
    }

    public void setUpVirtualDisplay() {
        Point point = new Point();
        this.mDisplay.getSize(point);
        this.mWidth = point.x;
        this.mHeight = point.y;
        StringBuilder sb = new StringBuilder();
        sb.append("Size: ");
        sb.append(this.mWidth);
        sb.append(" ");
        sb.append(this.mHeight);
        this.mImageReader = ImageReader.newInstance(this.mWidth, this.mHeight, 1, 2);
        this.mVirtualDisplay = this.mMediaProjection.createVirtualDisplay(Const.APPDIR, this.mWidth, this.mHeight, this.mDensity, this.VIRTUAL_DISPLAY_FLAGS, this.mImageReader.getSurface(), null, null);
        this.IMAGES_PRODUCED = 0;
        this.mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), null);
    }

    public String getDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().getTime());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showNotificationScreenshot(String str) {
        Utils.showDialogResult(getApplicationContext(), str);
        Bitmap decodeResource = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        BitmapFactory.decodeFile(str);
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

    @Override // androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback, androidx.fragment.app.FragmentActivity
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        try {
            finish();
        } catch (Exception unused) {
        }
    }

    /* access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i != 100) {
            return;
        }
        if (i2 != -1) {
            Toast.makeText(this, getString(R.string.permission_deny), 0).show();
            stopScreenCapture();
            tearDownMediaProjection();
            finish();
            return;
        }
        this.mResultData = intent;
        this.mResultCode = i2;
        new Handler().postDelayed(new Runnable() {
            /* class com.testlubu.screenrecorder.ui.activities.ScreenShotActivity.AnonymousClass2 */

            public void run() {
                ScreenShotActivity.this.startCaptureScreen();
            }
        }, 250);
        finish();
    }

    public void startCaptureScreen() {
        if (this.mResultCode != 0 && this.mResultData != null) {
            if (this.mMediaProjection != null) {
                tearDownMediaProjection();
            }
            setUpMediaProjection();
            if (this.mMediaProjection != null) {
                SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                String string = getString(R.string.savelocation_key);
                File file = new File(defaultSharedPreferences.getString(string, Environment.getExternalStorageDirectory() + File.separator + Const.APPDIR));
                String string2 = getString(R.string.savelocation_key);
                this.STORE_DIRECTORY = defaultSharedPreferences.getString(string2, Environment.getExternalStorageDirectory() + File.separator + Const.APPDIR);
                if (file.exists() || file.mkdirs()) {
                    setUpVirtualDisplay();
                    return;
                }
                stopScreenCapture();
                tearDownMediaProjection();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopScreenCapture() {
        VirtualDisplay virtualDisplay = this.mVirtualDisplay;
        if (virtualDisplay != null) {
            virtualDisplay.release();
            this.mVirtualDisplay = null;
            ImageReader imageReader = this.mImageReader;
            if (imageReader != null) {
                imageReader.setOnImageAvailableListener(null, null);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void tearDownMediaProjection() {
        MediaProjection mediaProjection = this.mMediaProjection;
        if (mediaProjection != null) {
            mediaProjection.stop();
            this.mMediaProjection = null;
        }
    }

    public boolean isScreenshotActived() {
        return this.mResultCode == -1 && this.mResultData != null;
    }
}
