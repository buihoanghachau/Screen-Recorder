package com.testlubu.screenrecorder.videoTrimmer.interfaces;

import android.net.Uri;

public interface OnTrimVideoListener {
    void cancelAction();

    void getResult(Uri uri);

    void onError(String str);

    void onTrimStarted();
}
