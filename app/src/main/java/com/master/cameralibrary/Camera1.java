package com.master.cameralibrary;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.view.SurfaceHolder;
import androidx.collection.SparseArrayCompat;
import com.master.cameralibrary.CameraViewImpl;
import com.master.cameralibrary.PreviewImpl;
import com.testlubu.screenrecorder.common.PrefUtils;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicBoolean;

class Camera1 extends CameraViewImpl {
    private static final SparseArrayCompat<String> FLASH_MODES = new SparseArrayCompat<>();
    private static final int INVALID_CAMERA_ID = -1;
    private final AtomicBoolean isPictureCaptureInProgress = new AtomicBoolean(false);
    private AspectRatio mAspectRatio;
    private boolean mAutoFocus;
    Camera mCamera;
    private int mCameraId;
    private final Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
    private Camera.Parameters mCameraParameters;
    private int mDisplayOrientation;
    private int mFacing;
    private int mFlash;
    private final SizeMap mPictureSizes = new SizeMap();
    private final SizeMap mPreviewSizes = new SizeMap();
    private boolean mShowingPreview;

    private boolean isLandscape(int i) {
        return i == 90 || i == 270;
    }

    static {
        FLASH_MODES.put(0, "off");
        FLASH_MODES.put(1, "on");
        FLASH_MODES.put(2, "torch");
        FLASH_MODES.put(3, PrefUtils.VALUE_ORIENTATION);
        FLASH_MODES.put(4, "red-eye");
    }

    Camera1(CameraViewImpl.Callback callback, PreviewImpl previewImpl) {
        super(callback, previewImpl);
        previewImpl.setCallback(new PreviewImpl.Callback() {
            /* class com.master.cameralibrary.Camera1.AnonymousClass1 */

            @Override // com.master.cameralibrary.PreviewImpl.Callback
            public void onSurfaceChanged() {
                if (Camera1.this.mCamera != null) {
                    Camera1.this.setUpPreview();
                    Camera1.this.adjustCameraParameters();
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.CameraViewImpl
    public boolean start() {
        chooseCamera();
        openCamera();
        if (this.mPreview.isReady()) {
            setUpPreview();
        }
        this.mShowingPreview = true;
        this.mCamera.startPreview();
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.CameraViewImpl
    public void stop() {
        Camera camera = this.mCamera;
        if (camera != null) {
            camera.stopPreview();
        }
        this.mShowingPreview = false;
        releaseCamera();
    }

    /* access modifiers changed from: package-private */
    @SuppressLint({"NewApi"})
    public void setUpPreview() {
        try {
            if (this.mPreview.getOutputClass() == SurfaceHolder.class) {
                boolean z = this.mShowingPreview && Build.VERSION.SDK_INT < 14;
                if (z) {
                    this.mCamera.stopPreview();
                }
                this.mCamera.setPreviewDisplay(this.mPreview.getSurfaceHolder());
                if (z) {
                    this.mCamera.startPreview();
                    return;
                }
                return;
            }
            this.mCamera.setPreviewTexture((SurfaceTexture) this.mPreview.getSurfaceTexture());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.CameraViewImpl
    public boolean isCameraOpened() {
        return this.mCamera != null;
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.CameraViewImpl
    public void setFacing(int i) {
        if (this.mFacing != i) {
            this.mFacing = i;
            if (isCameraOpened()) {
                stop();
                start();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.CameraViewImpl
    public int getFacing() {
        return this.mFacing;
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.CameraViewImpl
    public Set<AspectRatio> getSupportedAspectRatios() {
        SizeMap sizeMap = this.mPreviewSizes;
        for (AspectRatio aspectRatio : sizeMap.ratios()) {
            if (this.mPictureSizes.sizes(aspectRatio) == null) {
                sizeMap.remove(aspectRatio);
            }
        }
        return sizeMap.ratios();
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.CameraViewImpl
    public boolean setAspectRatio(AspectRatio aspectRatio) {
        if (this.mAspectRatio == null || !isCameraOpened()) {
            this.mAspectRatio = aspectRatio;
            return true;
        } else if (this.mAspectRatio.equals(aspectRatio)) {
            return false;
        } else {
            if (this.mPreviewSizes.sizes(aspectRatio) != null) {
                this.mAspectRatio = aspectRatio;
                adjustCameraParameters();
                return true;
            }
            throw new UnsupportedOperationException(aspectRatio + " is not supported");
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.CameraViewImpl
    public AspectRatio getAspectRatio() {
        return this.mAspectRatio;
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.CameraViewImpl
    public void setAutoFocus(boolean z) {
        if (this.mAutoFocus != z && setAutoFocusInternal(z)) {
            this.mCamera.setParameters(this.mCameraParameters);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.CameraViewImpl
    public boolean getAutoFocus() {
        if (!isCameraOpened()) {
            return this.mAutoFocus;
        }
        String focusMode = this.mCameraParameters.getFocusMode();
        return focusMode != null && focusMode.contains("continuous");
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.CameraViewImpl
    public void setFlash(int i) {
        if (i != this.mFlash && setFlashInternal(i)) {
            this.mCamera.setParameters(this.mCameraParameters);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.CameraViewImpl
    public int getFlash() {
        return this.mFlash;
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.CameraViewImpl
    public void takePicture() {
        if (!isCameraOpened()) {
            throw new IllegalStateException("Camera is not ready. Call start() before takePicture().");
        } else if (getAutoFocus()) {
            this.mCamera.cancelAutoFocus();
            this.mCamera.autoFocus(new Camera.AutoFocusCallback() {
                /* class com.master.cameralibrary.Camera1.AnonymousClass2 */

                public void onAutoFocus(boolean z, Camera camera) {
                    Camera1.this.takePictureInternal();
                }
            });
        } else {
            takePictureInternal();
        }
    }

    /* access modifiers changed from: package-private */
    public void takePictureInternal() {
        if (!this.isPictureCaptureInProgress.getAndSet(true)) {
            this.mCamera.takePicture(null, null, null, new Camera.PictureCallback() {
                /* class com.master.cameralibrary.Camera1.AnonymousClass3 */

                public void onPictureTaken(byte[] bArr, Camera camera) {
                    Camera1.this.isPictureCaptureInProgress.set(false);
                    Camera1.this.mCallback.onPictureTaken(bArr);
                    camera.cancelAutoFocus();
                    camera.startPreview();
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.CameraViewImpl
    public void setDisplayOrientation(int i) {
        if (this.mDisplayOrientation != i) {
            this.mDisplayOrientation = i;
            if (isCameraOpened()) {
                this.mCameraParameters.setRotation(calcCameraRotation(i));
                this.mCamera.setParameters(this.mCameraParameters);
                boolean z = this.mShowingPreview && Build.VERSION.SDK_INT < 14;
                if (z) {
                    this.mCamera.stopPreview();
                }
                this.mCamera.setDisplayOrientation(calcDisplayOrientation(i));
                if (z) {
                    this.mCamera.startPreview();
                }
            }
        }
    }

    private void chooseCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, this.mCameraInfo);
            if (this.mCameraInfo.facing == this.mFacing) {
                this.mCameraId = i;
                return;
            }
        }
        this.mCameraId = -1;
    }

    private void openCamera() {
        if (this.mCamera != null) {
            releaseCamera();
        }
        this.mCamera = Camera.open(this.mCameraId);
        this.mCameraParameters = this.mCamera.getParameters();
        this.mPreviewSizes.clear();
        for (Camera.Size size : this.mCameraParameters.getSupportedPreviewSizes()) {
            this.mPreviewSizes.add(new Size(size.width, size.height));
        }
        this.mPictureSizes.clear();
        for (Camera.Size size2 : this.mCameraParameters.getSupportedPictureSizes()) {
            this.mPictureSizes.add(new Size(size2.width, size2.height));
        }
        if (this.mAspectRatio == null) {
            this.mAspectRatio = Constants.DEFAULT_ASPECT_RATIO;
        }
        adjustCameraParameters();
        this.mCamera.setDisplayOrientation(calcDisplayOrientation(this.mDisplayOrientation));
        this.mCallback.onCameraOpened();
    }

    private AspectRatio chooseAspectRatio() {
        Iterator<AspectRatio> it = this.mPreviewSizes.ratios().iterator();
        AspectRatio aspectRatio = null;
        while (it.hasNext()) {
            aspectRatio = it.next();
            if (aspectRatio.equals(Constants.DEFAULT_ASPECT_RATIO)) {
                break;
            }
        }
        return aspectRatio;
    }

    /* access modifiers changed from: package-private */
    public void adjustCameraParameters() {
        SortedSet<Size> sizes = this.mPreviewSizes.sizes(this.mAspectRatio);
        if (sizes == null) {
            this.mAspectRatio = chooseAspectRatio();
            sizes = this.mPreviewSizes.sizes(this.mAspectRatio);
        }
        Size chooseOptimalSize = chooseOptimalSize(sizes);
        Size last = this.mPictureSizes.sizes(this.mAspectRatio).last();
        if (this.mShowingPreview) {
            this.mCamera.stopPreview();
        }
        this.mCameraParameters.setPreviewSize(chooseOptimalSize.getWidth(), chooseOptimalSize.getHeight());
        this.mCameraParameters.setPictureSize(last.getWidth(), last.getHeight());
        this.mCameraParameters.setRotation(calcCameraRotation(this.mDisplayOrientation));
        setAutoFocusInternal(this.mAutoFocus);
        setFlashInternal(this.mFlash);
        this.mCamera.setParameters(this.mCameraParameters);
        if (this.mShowingPreview) {
            this.mCamera.startPreview();
        }
    }

    private Size chooseOptimalSize(SortedSet<Size> sortedSet) {
        if (!this.mPreview.isReady()) {
            return sortedSet.first();
        }
        int width = this.mPreview.getWidth();
        int height = this.mPreview.getHeight();
        if (isLandscape(this.mDisplayOrientation)) {
            height = width;
            width = height;
        }
        Size size = null;
        Iterator<Size> it = sortedSet.iterator();
        while (it.hasNext()) {
            size = it.next();
            if (width <= size.getWidth() && height <= size.getHeight()) {
                break;
            }
        }
        return size;
    }

    private void releaseCamera() {
        Camera camera = this.mCamera;
        if (camera != null) {
            camera.release();
            this.mCamera = null;
            this.mCallback.onCameraClosed();
        }
    }

    private int calcDisplayOrientation(int i) {
        if (this.mCameraInfo.facing == 1) {
            return (360 - ((this.mCameraInfo.orientation + i) % 360)) % 360;
        }
        return ((this.mCameraInfo.orientation - i) + 360) % 360;
    }

    private int calcCameraRotation(int i) {
        if (this.mCameraInfo.facing == 1) {
            return (this.mCameraInfo.orientation + i) % 360;
        }
        return ((this.mCameraInfo.orientation + i) + (isLandscape(i) ? 180 : 0)) % 360;
    }

    private boolean setAutoFocusInternal(boolean z) {
        this.mAutoFocus = z;
        if (!isCameraOpened()) {
            return false;
        }
        List<String> supportedFocusModes = this.mCameraParameters.getSupportedFocusModes();
        if (z && supportedFocusModes.contains("continuous-picture")) {
            this.mCameraParameters.setFocusMode("continuous-picture");
            return true;
        } else if (supportedFocusModes.contains("fixed")) {
            this.mCameraParameters.setFocusMode("fixed");
            return true;
        } else if (supportedFocusModes.contains("infinity")) {
            this.mCameraParameters.setFocusMode("infinity");
            return true;
        } else {
            this.mCameraParameters.setFocusMode(supportedFocusModes.get(0));
            return true;
        }
    }

    private boolean setFlashInternal(int i) {
        if (isCameraOpened()) {
            List<String> supportedFlashModes = this.mCameraParameters.getSupportedFlashModes();
            String str = FLASH_MODES.get(i);
            if (supportedFlashModes == null || !supportedFlashModes.contains(str)) {
                String str2 = FLASH_MODES.get(this.mFlash);
                if (supportedFlashModes != null && supportedFlashModes.contains(str2)) {
                    return false;
                }
                this.mCameraParameters.setFlashMode("off");
                this.mFlash = 0;
                return true;
            }
            this.mCameraParameters.setFlashMode(str);
            this.mFlash = i;
            return true;
        }
        this.mFlash = i;
        return false;
    }
}
