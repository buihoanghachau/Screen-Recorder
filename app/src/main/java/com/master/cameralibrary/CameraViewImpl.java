package com.master.cameralibrary;

import android.view.View;
import java.util.Set;

abstract class CameraViewImpl {
    protected final Callback mCallback;
    protected final PreviewImpl mPreview;

    interface Callback {
        void onCameraClosed();

        void onCameraOpened();

        void onPictureTaken(byte[] bArr);
    }

    /* access modifiers changed from: package-private */
    public abstract AspectRatio getAspectRatio();

    /* access modifiers changed from: package-private */
    public abstract boolean getAutoFocus();

    /* access modifiers changed from: package-private */
    public abstract int getFacing();

    /* access modifiers changed from: package-private */
    public abstract int getFlash();

    /* access modifiers changed from: package-private */
    public abstract Set<AspectRatio> getSupportedAspectRatios();

    /* access modifiers changed from: package-private */
    public abstract boolean isCameraOpened();

    /* access modifiers changed from: package-private */
    public abstract boolean setAspectRatio(AspectRatio aspectRatio);

    /* access modifiers changed from: package-private */
    public abstract void setAutoFocus(boolean z);

    /* access modifiers changed from: package-private */
    public abstract void setDisplayOrientation(int i);

    /* access modifiers changed from: package-private */
    public abstract void setFacing(int i);

    /* access modifiers changed from: package-private */
    public abstract void setFlash(int i);

    /* access modifiers changed from: package-private */
    public abstract boolean start();

    /* access modifiers changed from: package-private */
    public abstract void stop();

    /* access modifiers changed from: package-private */
    public abstract void takePicture();

    CameraViewImpl(Callback callback, PreviewImpl previewImpl) {
        this.mCallback = callback;
        this.mPreview = previewImpl;
    }

    /* access modifiers changed from: package-private */
    public View getView() {
        return this.mPreview.getView();
    }
}
