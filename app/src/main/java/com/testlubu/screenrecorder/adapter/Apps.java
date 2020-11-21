package com.testlubu.screenrecorder.adapter;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;

public class Apps implements Comparable<Apps> {
    private Drawable appIcon;
    private String appName;
    private boolean isSelectedApp;
    private String packageName;

    public Apps(String str, String str2, Drawable drawable) {
        this.appName = str;
        this.packageName = str2;
        this.appIcon = drawable;
    }

    /* access modifiers changed from: package-private */
    public String getAppName() {
        return this.appName;
    }

    public void setAppName(String str) {
        this.appName = str;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String str) {
        this.packageName = str;
    }

    /* access modifiers changed from: package-private */
    public Drawable getAppIcon() {
        return this.appIcon;
    }

    public void setAppIcon(Drawable drawable) {
        this.appIcon = drawable;
    }

    /* access modifiers changed from: package-private */
    public boolean isSelectedApp() {
        return this.isSelectedApp;
    }

    public void setSelectedApp(boolean z) {
        this.isSelectedApp = z;
    }

    public int compareTo(@NonNull Apps apps) {
        return this.appName.compareTo(apps.appName);
    }
}
