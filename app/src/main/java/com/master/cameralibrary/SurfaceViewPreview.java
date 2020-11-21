package com.master.cameralibrary;

import android.content.Context;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.view.ViewCompat;
import com.testlubu.screenrecorder.R;

class SurfaceViewPreview extends PreviewImpl {
    final SurfaceView mSurfaceView;

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.PreviewImpl
    public void setDisplayOrientation(int i) {
    }

    SurfaceViewPreview(Context context, ViewGroup viewGroup) {
        this.mSurfaceView = (SurfaceView) View.inflate(context, R.layout.surface_view, viewGroup).findViewById(R.id.surface_view);
        SurfaceHolder holder = this.mSurfaceView.getHolder();
        holder.setType(3);
        holder.addCallback(new SurfaceHolder.Callback() {
            /* class com.master.cameralibrary.SurfaceViewPreview.AnonymousClass1 */

            public void surfaceCreated(SurfaceHolder surfaceHolder) {
            }

            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
                SurfaceViewPreview.this.setSize(i2, i3);
                if (!ViewCompat.isInLayout(SurfaceViewPreview.this.mSurfaceView)) {
                    SurfaceViewPreview.this.dispatchSurfaceChanged();
                }
            }

            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                SurfaceViewPreview.this.setSize(0, 0);
            }
        });
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.PreviewImpl
    public Surface getSurface() {
        return getSurfaceHolder().getSurface();
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.PreviewImpl
    public SurfaceHolder getSurfaceHolder() {
        return this.mSurfaceView.getHolder();
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.PreviewImpl
    public View getView() {
        return this.mSurfaceView;
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.PreviewImpl
    public Class getOutputClass() {
        return SurfaceHolder.class;
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.PreviewImpl
    public boolean isReady() {
        return (getWidth() == 0 || getHeight() == 0) ? false : true;
    }
}
