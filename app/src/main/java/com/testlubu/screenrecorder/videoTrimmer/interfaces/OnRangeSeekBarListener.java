package com.testlubu.screenrecorder.videoTrimmer.interfaces;

import com.testlubu.screenrecorder.videoTrimmer.view.RangeSeekBarView;

public interface OnRangeSeekBarListener {
    void onCreate(RangeSeekBarView rangeSeekBarView, int i, float f);

    void onSeek(RangeSeekBarView rangeSeekBarView, int i, float f);

    void onSeekStart(RangeSeekBarView rangeSeekBarView, int i, float f);

    void onSeekStop(RangeSeekBarView rangeSeekBarView, int i, float f);
}
