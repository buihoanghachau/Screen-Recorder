package com.testlubu.screenrecorder.videoTrimmer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.videoTrimmer.interfaces.OnRangeSeekBarListener;
import java.util.ArrayList;
import java.util.List;

public class RangeSeekBarView extends View {
    private static final String TAG = "RangeSeekBarView";
    private int currentThumb;
    private boolean mFirstRun;
    private int mHeightTimeLine;
    private final Paint mLine;
    private List<OnRangeSeekBarListener> mListeners;
    private float mMaxWidth;
    private float mPixelRangeMax;
    private float mPixelRangeMin;
    private float mScaleRangeMax;
    private final Paint mShadow;
    private float mThumbHeight;
    private float mThumbWidth;
    private List<Thumb> mThumbs;
    private int mViewWidth;

    public RangeSeekBarView(@NonNull Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public RangeSeekBarView(@NonNull Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mShadow = new Paint();
        this.mLine = new Paint();
        this.currentThumb = 0;
        init();
    }

    private void init() {
        this.mThumbs = Thumb.initThumbs(getResources());
        this.mThumbWidth = (float) Thumb.getWidthBitmap(this.mThumbs);
        this.mThumbHeight = (float) Thumb.getHeightBitmap(this.mThumbs);
        this.mScaleRangeMax = 100.0f;
        this.mHeightTimeLine = getContext().getResources().getDimensionPixelOffset(R.dimen.frames_video_height);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.mFirstRun = true;
        int color = ContextCompat.getColor(getContext(), R.color.shadow_color);
        this.mShadow.setAntiAlias(true);
        this.mShadow.setColor(color);
        this.mShadow.setAlpha(177);
        int color2 = ContextCompat.getColor(getContext(), R.color.line_color);
        this.mLine.setAntiAlias(true);
        this.mLine.setColor(color2);
        this.mLine.setAlpha(ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION);
    }

    public void initMaxWidth() {
        this.mMaxWidth = this.mThumbs.get(1).getPos() - this.mThumbs.get(0).getPos();
        onSeekStop(this, 0, this.mThumbs.get(0).getVal());
        onSeekStop(this, 1, this.mThumbs.get(1).getVal());
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        this.mViewWidth = resolveSizeAndState(getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth(), i, 1);
        setMeasuredDimension(this.mViewWidth, resolveSizeAndState(getPaddingBottom() + getPaddingTop() + ((int) this.mThumbHeight) + this.mHeightTimeLine, i2, 1));
        this.mPixelRangeMin = 0.0f;
        this.mPixelRangeMax = ((float) this.mViewWidth) - this.mThumbWidth;
        if (this.mFirstRun) {
            for (int i3 = 0; i3 < this.mThumbs.size(); i3++) {
                Thumb thumb = this.mThumbs.get(i3);
                float f = (float) i3;
                thumb.setVal(this.mScaleRangeMax * f);
                thumb.setPos(this.mPixelRangeMax * f);
            }
            int i4 = this.currentThumb;
            onCreate(this, i4, getThumbValue(i4));
            this.mFirstRun = false;
        }
    }

    /* access modifiers changed from: protected */
    public void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawShadow(canvas);
        drawThumbs(canvas);
    }

    public boolean onTouchEvent(@NonNull MotionEvent motionEvent) {
        float x = motionEvent.getX();
        int action = motionEvent.getAction();
        if (action == 0) {
            this.currentThumb = getClosestThumb(x);
            int i = this.currentThumb;
            if (i == -1) {
                return false;
            }
            Thumb thumb = this.mThumbs.get(i);
            thumb.setLastTouchX(x);
            onSeekStart(this, this.currentThumb, thumb.getVal());
            return true;
        } else if (action == 1) {
            int i2 = this.currentThumb;
            if (i2 == -1) {
                return false;
            }
            onSeekStop(this, this.currentThumb, this.mThumbs.get(i2).getVal());
            return true;
        } else if (action != 2) {
            return false;
        } else {
            Thumb thumb2 = this.mThumbs.get(this.currentThumb);
            Thumb thumb3 = this.mThumbs.get(this.currentThumb == 0 ? 1 : 0);
            float lastTouchX = x - thumb2.getLastTouchX();
            float pos = thumb2.getPos() + lastTouchX;
            if (this.currentThumb == 0) {
                if (((float) thumb2.getWidthBitmap()) + pos >= thumb3.getPos()) {
                    thumb2.setPos(thumb3.getPos() - ((float) thumb2.getWidthBitmap()));
                } else {
                    float f = this.mPixelRangeMin;
                    if (pos <= f) {
                        thumb2.setPos(f);
                    } else {
                        checkPositionThumb(thumb2, thumb3, lastTouchX, true);
                        thumb2.setPos(thumb2.getPos() + lastTouchX);
                        thumb2.setLastTouchX(x);
                    }
                }
            } else if (pos <= thumb3.getPos() + ((float) thumb3.getWidthBitmap())) {
                thumb2.setPos(thumb3.getPos() + ((float) thumb2.getWidthBitmap()));
            } else {
                float f2 = this.mPixelRangeMax;
                if (pos >= f2) {
                    thumb2.setPos(f2);
                } else {
                    checkPositionThumb(thumb3, thumb2, lastTouchX, false);
                    thumb2.setPos(thumb2.getPos() + lastTouchX);
                    thumb2.setLastTouchX(x);
                }
            }
            setThumbPos(this.currentThumb, thumb2.getPos());
            invalidate();
            return true;
        }
    }

    private void checkPositionThumb(@NonNull Thumb thumb, @NonNull Thumb thumb2, float f, boolean z) {
        if (!z || f >= 0.0f) {
            if (!z && f > 0.0f && (thumb2.getPos() + f) - thumb.getPos() > this.mMaxWidth) {
                thumb.setPos((thumb2.getPos() + f) - this.mMaxWidth);
                setThumbPos(0, thumb.getPos());
            }
        } else if (thumb2.getPos() - (thumb.getPos() + f) > this.mMaxWidth) {
            thumb2.setPos(thumb.getPos() + f + this.mMaxWidth);
            setThumbPos(1, thumb2.getPos());
        }
    }

    private int getUnstuckFrom(int i) {
        float val = this.mThumbs.get(i).getVal();
        for (int i2 = i - 1; i2 >= 0; i2--) {
            if (this.mThumbs.get(i2).getVal() != val) {
                return i2 + 1;
            }
        }
        return 0;
    }

    private float pixelToScale(int i, float f) {
        float f2 = this.mPixelRangeMax;
        float f3 = (f * 100.0f) / f2;
        if (i == 0) {
            return f3 + ((((this.mThumbWidth * f3) / 100.0f) * 100.0f) / f2);
        }
        return f3 - (((((100.0f - f3) * this.mThumbWidth) / 100.0f) * 100.0f) / f2);
    }

    private float scaleToPixel(int i, float f) {
        float f2 = (this.mPixelRangeMax * f) / 100.0f;
        if (i == 0) {
            return f2 - ((f * this.mThumbWidth) / 100.0f);
        }
        return f2 + (((100.0f - f) * this.mThumbWidth) / 100.0f);
    }

    private void calculateThumbValue(int i) {
        if (i < this.mThumbs.size() && !this.mThumbs.isEmpty()) {
            Thumb thumb = this.mThumbs.get(i);
            thumb.setVal(pixelToScale(i, thumb.getPos()));
            onSeek(this, i, thumb.getVal());
        }
    }

    private void calculateThumbPos(int i) {
        if (i < this.mThumbs.size() && !this.mThumbs.isEmpty()) {
            Thumb thumb = this.mThumbs.get(i);
            thumb.setPos(scaleToPixel(i, thumb.getVal()));
        }
    }

    private float getThumbValue(int i) {
        return this.mThumbs.get(i).getVal();
    }

    public void setThumbValue(int i, float f) {
        this.mThumbs.get(i).setVal(f);
        calculateThumbPos(i);
        invalidate();
    }

    private void setThumbPos(int i, float f) {
        this.mThumbs.get(i).setPos(f);
        calculateThumbValue(i);
        invalidate();
    }

    private int getClosestThumb(float f) {
        int i = -1;
        if (!this.mThumbs.isEmpty()) {
            for (int i2 = 0; i2 < this.mThumbs.size(); i2++) {
                float pos = this.mThumbs.get(i2).getPos() + this.mThumbWidth;
                if (f >= this.mThumbs.get(i2).getPos() && f <= pos) {
                    i = this.mThumbs.get(i2).getIndex();
                }
            }
        }
        return i;
    }

    private void drawShadow(@NonNull Canvas canvas) {
        if (!this.mThumbs.isEmpty()) {
            for (Thumb thumb : this.mThumbs) {
                if (thumb.getIndex() == 0) {
                    float pos = thumb.getPos() + ((float) getPaddingLeft());
                    if (pos > this.mPixelRangeMin) {
                        float f = this.mThumbWidth;
                        canvas.drawRect(new Rect((int) f, 0, (int) (pos + f), this.mHeightTimeLine), this.mShadow);
                    }
                } else {
                    float pos2 = thumb.getPos() - ((float) getPaddingRight());
                    if (pos2 < this.mPixelRangeMax) {
                        canvas.drawRect(new Rect((int) pos2, 0, (int) (((float) this.mViewWidth) - this.mThumbWidth), this.mHeightTimeLine), this.mShadow);
                    }
                }
            }
        }
    }

    private void drawThumbs(@NonNull Canvas canvas) {
        if (!this.mThumbs.isEmpty()) {
            for (Thumb thumb : this.mThumbs) {
                if (thumb.getIndex() == 0) {
                    canvas.drawBitmap(thumb.getBitmap(), thumb.getPos() + ((float) getPaddingLeft()), (float) (getPaddingTop() + this.mHeightTimeLine), (Paint) null);
                } else {
                    canvas.drawBitmap(thumb.getBitmap(), thumb.getPos() - ((float) getPaddingRight()), (float) (getPaddingTop() + this.mHeightTimeLine), (Paint) null);
                }
            }
        }
    }

    public void addOnRangeSeekBarListener(OnRangeSeekBarListener onRangeSeekBarListener) {
        if (this.mListeners == null) {
            this.mListeners = new ArrayList();
        }
        this.mListeners.add(onRangeSeekBarListener);
    }

    private void onCreate(RangeSeekBarView rangeSeekBarView, int i, float f) {
        List<OnRangeSeekBarListener> list = this.mListeners;
        if (list != null) {
            for (OnRangeSeekBarListener onRangeSeekBarListener : list) {
                onRangeSeekBarListener.onCreate(rangeSeekBarView, i, f);
            }
        }
    }

    private void onSeek(RangeSeekBarView rangeSeekBarView, int i, float f) {
        List<OnRangeSeekBarListener> list = this.mListeners;
        if (list != null) {
            for (OnRangeSeekBarListener onRangeSeekBarListener : list) {
                onRangeSeekBarListener.onSeek(rangeSeekBarView, i, f);
            }
        }
    }

    private void onSeekStart(RangeSeekBarView rangeSeekBarView, int i, float f) {
        List<OnRangeSeekBarListener> list = this.mListeners;
        if (list != null) {
            for (OnRangeSeekBarListener onRangeSeekBarListener : list) {
                onRangeSeekBarListener.onSeekStart(rangeSeekBarView, i, f);
            }
        }
    }

    private void onSeekStop(RangeSeekBarView rangeSeekBarView, int i, float f) {
        List<OnRangeSeekBarListener> list = this.mListeners;
        if (list != null) {
            for (OnRangeSeekBarListener onRangeSeekBarListener : list) {
                onRangeSeekBarListener.onSeekStop(rangeSeekBarView, i, f);
            }
        }
    }

    public List<Thumb> getThumbs() {
        return this.mThumbs;
    }
}
