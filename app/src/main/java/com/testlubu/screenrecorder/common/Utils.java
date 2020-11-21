package com.testlubu.screenrecorder.common;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
//import com.google.firebase.crashlytics.internal.common.AbstractSpiCall;
import com.testlubu.screenrecorder.ui.activities.DialogResultActivity;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Utils {
    public static void setEnableTouch(Context context, int i) {
    }

    public static boolean isAndroid26() {
        return Build.VERSION.SDK_INT >= 26;
    }

    public static int convertDpToPixel(float f, Context context) {
        return (int) (f * (((float) context.getResources().getDisplayMetrics().densityDpi) / 160.0f));
    }

    public static int convertPixelsToDp(float f, Context context) {
        return (int) (f / (((float) context.getResources().getDisplayMetrics().densityDpi) / 160.0f));
    }

    public static void openURL(Activity activity, String str) {
        try {
            activity.getApplicationContext().startActivity(new Intent("android.intent.action.VIEW", Uri.parse(str)).setFlags(268435456));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showDialogResult(Context context, String str) {
        Intent intent = new Intent(context, DialogResultActivity.class);
        intent.putExtra("path", str);
        intent.addFlags(268435456);
        context.startActivity(intent);
        if (isAppOnForeground(context)) {
            intent.setFlags(268435456);
            context.startActivity(intent);
            return;
        }
        intent.setFlags(268468224);
        try {
            PendingIntent.getActivity(context, (int) (Math.random() * 9999.0d), intent, 134217728).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
            intent.setFlags(268435456);
            context.startActivity(intent);
        }
    }

    public static Calendar toCalendar(long j) {
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(j);
        instance.set(11, 0);
        instance.set(12, 0);
        instance.set(13, 0);
        instance.set(14, 0);
        return instance;
    }

    public static int getScreenWidth(@NonNull Context context) {
        Point point = new Point();
        ((Activity) context).getWindowManager().getDefaultDisplay().getSize(point);
        return point.x;
    }

    public static int getScreenHeight(@NonNull Context context) {
        Point point = new Point();
        ((Activity) context).getWindowManager().getDefaultDisplay().getSize(point);
        return point.y;
    }

    public static boolean isInLandscapeMode(@NonNull Context context) {
        return context.getResources().getConfiguration().orientation == 2;
    }

    public static void createDir() {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + Const.APPDIR);
        if (Environment.getExternalStorageState().equals("mounted") && !file.isDirectory()) {
            file.mkdirs();
        }
    }

    public static void createDirEdited() {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + Const.APPDIR + File.separator + Const.FOLDER_EDITED);
        if (Environment.getExternalStorageState().equals("mounted") && !file.isDirectory()) {
            file.mkdirs();
        }
    }

    public static boolean isServiceRunning(Class<?> cls, Context context) {
        for (ActivityManager.RunningServiceInfo runningServiceInfo : ((ActivityManager) context.getSystemService("activity")).getRunningServices(Integer.MAX_VALUE)) {
            if (cls.getName().equals(runningServiceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String generateSectionTitle(Date date) {
        Calendar calendar = toCalendar(new Date().getTime());
        Calendar calendar2 = toCalendar(date.getTime());
        int abs = (int) Math.abs((calendar2.getTimeInMillis() - calendar.getTimeInMillis()) / 86400000);
        if (calendar.get(1) - calendar2.get(1) != 0) {
            return new SimpleDateFormat("EEEE, dd MMM YYYY", Locale.getDefault()).format(date);
        }
        if (abs != 0) {
            return abs != 1 ? new SimpleDateFormat("EEEE, dd MMM", Locale.getDefault()).format(date) : "Yesterday";
        }
        return "Today";
    }

    public static String getValue(String[] strArr, String[] strArr2, String str) {
        for (int i = 0; i < strArr2.length; i++) {
            if (strArr2[i].equalsIgnoreCase(str)) {
                return strArr[i];
            }
        }
        return strArr[0];
    }

    public static int getPosition(String[] strArr, String str) {
        for (int i = 0; i < strArr.length; i++) {
            if (strArr[i].equalsIgnoreCase(str)) {
                return i;
            }
        }
        return 0;
    }

    public static boolean isAndroid23() {
        return Build.VERSION.SDK_INT >= 23;
    }

    public static Bitmap getBitmapVideo(Context context, File file) {
        ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
        Cursor query = contentResolver.query(MediaStore.Video.Media.getContentUri("external"), new String[]{"_id", "bucket_id", "bucket_display_name", "_data"}, "_data=? ", new String[]{file.getPath()}, null);
        if (query == null || !query.moveToNext()) {
            return null;
        }
        Bitmap thumbnail = MediaStore.Video.Thumbnails.getThumbnail(contentResolver, (long) query.getInt(query.getColumnIndexOrThrow("_id")), 1, null);
        query.close();
        return thumbnail;
    }

    public static int getHeightStatusBar(Context context) {
        int identifier = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (identifier > 0) {
            return context.getResources().getDimensionPixelSize(identifier);
        }
        return 0;
    }

    public static int getHeightNavigationBar(Context context) {
        int identifier = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (identifier > 0) {
            return context.getResources().getDimensionPixelSize(identifier);
        }
        return 0;
    }

    public static File getCacheFile(Context context) {
        File cacheDir = context.getCacheDir();
        File file = new File(cacheDir, System.currentTimeMillis() + ".JPEG");
        if (file.exists()) {
            file.delete();
        }
        return file;
    }

    public static Bitmap CropBitmapTransparency(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int i = -1;
        int height = bitmap.getHeight();
        int i2 = -1;
        int i3 = width;
        int i4 = 0;
        while (i4 < bitmap.getHeight()) {
            int i5 = i2;
            int i6 = i;
            int i7 = i3;
            for (int i8 = 0; i8 < bitmap.getWidth(); i8++) {
                if (((bitmap.getPixel(i8, i4) >> 24) & 255) > 0) {
                    if (i8 < i7) {
                        i7 = i8;
                    }
                    if (i8 > i6) {
                        i6 = i8;
                    }
                    if (i4 < height) {
                        height = i4;
                    }
                    if (i4 > i5) {
                        i5 = i4;
                    }
                }
            }
            i4++;
            i3 = i7;
            i = i6;
            i2 = i5;
        }
        if (i < i3 || i2 < height) {
            return null;
        }
        return Bitmap.createBitmap(bitmap, i3, height, (i - i3) + 1, (i2 - height) + 1);
    }

    public static String getAppUrl(Context context) {
        return "https://play.google.com/store/apps/details?id=" + context.getPackageName();
    }

    public static boolean isAppOnForeground(Context context) {
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
        if (runningAppProcesses == null) {
            return false;
        }
        String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
            if (runningAppProcessInfo.importance == 100 && runningAppProcessInfo.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public static void startActivityAllStage(Context context, Intent intent) {
        if (context instanceof Activity) {
            context.startActivity(intent);
            return;
        }
        try {
            PendingIntent.getActivity(context, (int) (Math.random() * 9999.0d), intent, 134217728).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
            context.startActivity(intent);
        }
    }
}
