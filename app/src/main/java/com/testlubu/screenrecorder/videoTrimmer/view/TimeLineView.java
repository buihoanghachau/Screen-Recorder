package com.testlubu.screenrecorder.videoTrimmer.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.LongSparseArray;
import android.view.View;
import androidx.annotation.NonNull;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.videoTrimmer.utils.BackgroundExecutor;
import com.testlubu.screenrecorder.videoTrimmer.utils.UiThreadExecutor;

public class TimeLineView extends View {
    private LongSparseArray<Bitmap> mBitmapList;
    private int mHeightView;
    private Uri mVideoUri;

    public TimeLineView(@NonNull Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public TimeLineView(@NonNull Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mBitmapList = null;
        init();
    }

    private void init() {
        this.mHeightView = getContext().getResources().getDimensionPixelOffset(R.dimen.frames_video_height);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        setMeasuredDimension(resolveSizeAndState(getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth(), i, 1), resolveSizeAndState(getPaddingBottom() + getPaddingTop() + this.mHeightView, i2, 1));
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        if (i != i3) {
            getBitmap(i);
        }
    }

    private void getBitmap(final int i) {
        BackgroundExecutor.execute(new BackgroundExecutor.Task("", 0, "") {
            /* class com.testlubu.screenrecorder.videoTrimmer.view.TimeLineView.AnonymousClass1 */

            @Override // com.testlubu.screenrecorder.videoTrimmer.utils.BackgroundExecutor.Task
            public void execute() {
                Bitmap createScaledBitmap = null;
                try {
                    LongSparseArray longSparseArray = new LongSparseArray();
                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(TimeLineView.this.getContext(), TimeLineView.this.mVideoUri);
                    int i = TimeLineView.this.mHeightView;
                    int i2 = TimeLineView.this.mHeightView;
                    int ceil = (int) Math.ceil((double) (((float) i) / ((float) i)));
                    long parseInt = ((long) (Integer.parseInt(mediaMetadataRetriever.extractMetadata(9)) * 1000)) / ((long) ceil);
                    for (int i3 = 0; i3 < ceil; i3++) {
                        long j = (long) i3;
                        try {
                            createScaledBitmap = Bitmap.createScaledBitmap(mediaMetadataRetriever.getFrameAtTime(j * parseInt, 2), i, i2, false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        longSparseArray.put(j, createScaledBitmap);
                    }
                    mediaMetadataRetriever.release();
                    TimeLineView.this.returnBitmaps(longSparseArray);
                } catch (Throwable th) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), th);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void returnBitmaps(final LongSparseArray<Bitmap> longSparseArray) {
        UiThreadExecutor.runTask("", new Runnable() {
            /* class com.testlubu.screenrecorder.videoTrimmer.view.TimeLineView.AnonymousClass2 */

            public void run() {
                TimeLineView.this.mBitmapList = longSparseArray;
                TimeLineView.this.invalidate();
            }
        }, 0);
    }

    /* access modifiers changed from: protected */
    public void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (this.mBitmapList != null) {
            canvas.save();
            int i = 0;
            for (int i2 = 0; i2 < this.mBitmapList.size(); i2++) {
                Bitmap bitmap = this.mBitmapList.get((long) i2);
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, (float) i, 0.0f, (Paint) null);
                    i += bitmap.getWidth();
                }
            }
        }
    }

    public void setVideo(@NonNull Uri uri) {
        this.mVideoUri = uri;
    }
}
