package com.testlubu.screenrecorder;

import android.app.Application;
import java.util.ArrayList;
import java.util.List;

public class RecorderApplication extends Application {
    private static List<BaseActivity> activityList;
    private static RecorderApplication instance;

    public static RecorderApplication getInstance() {
        return instance;
    }

    private static synchronized void setInstance(RecorderApplication recorderApplication) {
        synchronized (RecorderApplication.class) {
            instance = recorderApplication;
        }
    }

    public void onCreate() {
        super.onCreate();
        if (instance == null) {
            setInstance(this);
        }
        activityList = new ArrayList();
    }

    public void doForCreate(BaseActivity baseActivity) {
        activityList.add(baseActivity);
    }

    public void doForFinish(BaseActivity baseActivity) {
        activityList.remove(baseActivity);
    }

    public BaseActivity getTopActivity() {
        if (activityList.isEmpty()) {
            return null;
        }
        List<BaseActivity> list = activityList;
        return list.get(list.size() - 1);
    }

    public void clearAllActivity() {
        for (BaseActivity baseActivity : activityList) {
            baseActivity.clear();
        }
        activityList.clear();
    }
}
