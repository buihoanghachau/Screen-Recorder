package com.testlubu.screenrecorder.videoTrimmer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.videoTrimmer.interfaces.OnProgressVideoListener;
import com.testlubu.screenrecorder.videoTrimmer.interfaces.OnRangeSeekBarListener;

public class ProgressBarView extends View implements OnRangeSeekBarListener, OnProgressVideoListener {
    private final Paint mBackgroundColor;
    private Rect mBackgroundRect;
    private final Paint mProgressColor;
    private int mProgressHeight;
    private Rect mProgressRect;
    private int mViewWidth;

    public ProgressBarView(@NonNull Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ProgressBarView(@NonNull Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mBackgroundColor = new Paint();
        this.mProgressColor = new Paint();
        init();
    }

    private void init() {
        int color = ContextCompat.getColor(getContext(), R.color.progress_color);
        int color2 = ContextCompat.getColor(getContext(), R.color.background_progress_color);
        this.mProgressHeight = getContext().getResources().getDimensionPixelOffset(R.dimen.progress_video_line_height);
        this.mBackgroundColor.setAntiAlias(true);
        this.mBackgroundColor.setColor(color2);
        this.mProgressColor.setAntiAlias(true);
        this.mProgressColor.setColor(color);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        this.mViewWidth = resolveSizeAndState(getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth(), i, 1);
        setMeasuredDimension(this.mViewWidth, resolveSizeAndState(getPaddingBottom() + getPaddingTop() + this.mProgressHeight, i2, 1));
    }

    /* access modifiers changed from: protected */
    public void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawLineBackground(canvas);
        drawLineProgress(canvas);
    }

    private void drawLineBackground(@NonNull Canvas canvas) {
        Rect rect = this.mBackgroundRect;
        if (rect != null) {
            canvas.drawRect(rect, this.mBackgroundColor);
        }
    }

    private void drawLineProgress(@NonNull Canvas canvas) {
        Rect rect = this.mProgressRect;
        if (rect != null) {
            canvas.drawRect(rect, this.mProgressColor);
        }
    }

    @Override // com.testlubu.screenrecorder.videoTrimmer.interfaces.OnRangeSeekBarListener
    public void onCreate(RangeSeekBarView rangeSeekBarView, int i, float f) {
        updateBackgroundRect(i, f);
    }

    @Override // com.testlubu.screenrecorder.videoTrimmer.interfaces.OnRangeSeekBarListener
    public void onSeek(RangeSeekBarView rangeSeekBarView, int i, float f) {
        updateBackgroundRect(i, f);
    }

    @Override // com.testlubu.screenrecorder.videoTrimmer.interfaces.OnRangeSeekBarListener
    public void onSeekStart(RangeSeekBarView rangeSeekBarView, int i, float f) {
        updateBackgroundRect(i, f);
    }

    @Override // com.testlubu.screenrecorder.videoTrimmer.interfaces.OnRangeSeekBarListener
    public void onSeekStop(RangeSeekBarView rangeSeekBarView, int i, float f) {
        updateBackgroundRect(i, f);
    }

    private void updateBackgroundRect(int i, float f) {
        if (this.mBackgroundRect == null) {
            this.mBackgroundRect = new Rect(0, 0, this.mViewWidth, this.mProgressHeight);
        }
        int i2 = (int) ((((float) this.mViewWidth) * f) / 100.0f);
        if (i == 0) {
            this.mBackgroundRect = new Rect(i2, this.mBackgroundRect.top, this.mBackgroundRect.right, this.mBackgroundRect.bottom);
        } else {
            this.mBackgroundRect = new Rect(this.mBackgroundRect.left, this.mBackgroundRect.top, i2, this.mBackgroundRect.bottom);
        }
        updateProgress(0, 0, 0.0f);
    }

    @Override // com.testlubu.screenrecorder.videoTrimmer.interfaces.OnProgressVideoListener
    public void updateProgress(int i, int i2, float f) {
        if (f == 0.0f) {
            this.mProgressRect = new Rect(0, this.mBackgroundRect.top, 0, this.mBackgroundRect.bottom);
        } else {
            this.mProgressRect = new Rect(this.mBackgroundRect.left, this.mBackgroundRect.top, (int) ((((float) this.mViewWidth) * f) / 100.0f), this.mBackgroundRect.bottom);
        }
        invalidate();
    }
}
