package com.testlubu.screenrecorder.videoTrimmer.view;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import com.testlubu.screenrecorder.R;
import java.util.List;
import java.util.Vector;

public class Thumb {
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    private Bitmap mBitmap;
    private int mHeightBitmap;
    private int mIndex;
    private float mLastTouchX;
    private float mPos = 0.0f;
    private float mVal = 0.0f;
    private int mWidthBitmap;

    private Thumb() {
    }

    public int getIndex() {
        return this.mIndex;
    }

    private void setIndex(int i) {
        this.mIndex = i;
    }

    public float getVal() {
        return this.mVal;
    }

    public void setVal(float f) {
        this.mVal = f;
    }

    public float getPos() {
        return this.mPos;
    }

    public void setPos(float f) {
        this.mPos = f;
    }

    public Bitmap getBitmap() {
        return this.mBitmap;
    }

    private void setBitmap(@NonNull Bitmap bitmap) {
        this.mBitmap = bitmap;
        this.mWidthBitmap = bitmap.getWidth();
        this.mHeightBitmap = bitmap.getHeight();
    }

    @NonNull
    public static List<Thumb> initThumbs(Resources resources) {
        Vector vector = new Vector();
        for (int i = 0; i < 2; i++) {
            Thumb thumb = new Thumb();
            thumb.setIndex(i);
            if (i == 0) {
                thumb.setBitmap(BitmapFactory.decodeResource(resources, R.drawable.apptheme_text_select_handle_left));
            } else {
                thumb.setBitmap(BitmapFactory.decodeResource(resources, R.drawable.apptheme_text_select_handle_right));
            }
            vector.add(thumb);
        }
        return vector;
    }

    public static int getWidthBitmap(@NonNull List<Thumb> list) {
        return list.get(0).getWidthBitmap();
    }

    public static int getHeightBitmap(@NonNull List<Thumb> list) {
        return list.get(0).getHeightBitmap();
    }

    public float getLastTouchX() {
        return this.mLastTouchX;
    }

    public void setLastTouchX(float f) {
        this.mLastTouchX = f;
    }

    public int getWidthBitmap() {
        return this.mWidthBitmap;
    }

    private int getHeightBitmap() {
        return this.mHeightBitmap;
    }
}
