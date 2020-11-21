package com.master.cameralibrary;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.master.cameralibrary.CameraViewImpl;
import com.master.cameralibrary.PreviewImpl;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;

@TargetApi(21)
class Camera2 extends CameraViewImpl {
    private static final SparseIntArray INTERNAL_FACINGS = new SparseIntArray();
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final String TAG = "Camera2";
    private AspectRatio mAspectRatio = Constants.DEFAULT_ASPECT_RATIO;
    private boolean mAutoFocus;
    CameraDevice mCamera;
    private CameraCharacteristics mCameraCharacteristics;
    private final CameraDevice.StateCallback mCameraDeviceCallback = new CameraDevice.StateCallback() {
        /* class com.master.cameralibrary.Camera2.AnonymousClass1 */

        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Camera2 camera2 = Camera2.this;
            camera2.mCamera = cameraDevice;
            camera2.mCallback.onCameraOpened();
            Camera2.this.startCaptureSession();
        }

        public void onClosed(@NonNull CameraDevice cameraDevice) {
            Camera2.this.mCallback.onCameraClosed();
        }

        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            Camera2.this.mCamera = null;
        }

        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            Log.e(Camera2.TAG, "onError: " + cameraDevice.getId() + " (" + i + ")");
            Camera2.this.mCamera = null;
        }
    };
    private String mCameraId;
    private final CameraManager mCameraManager;
    PictureCaptureCallback mCaptureCallback = new PictureCaptureCallback() {
        /* class com.master.cameralibrary.Camera2.AnonymousClass3 */

        @Override // com.master.cameralibrary.Camera2.PictureCaptureCallback
        public void onPrecaptureRequired() {
            Camera2.this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, 1);
            setState(3);
            try {
                Camera2.this.mCaptureSession.capture(Camera2.this.mPreviewRequestBuilder.build(), this, null);
                Camera2.this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, 0);
            } catch (CameraAccessException e) {
                Log.e(Camera2.TAG, "Failed to run precapture sequence.", e);
            }
        }

        @Override // com.master.cameralibrary.Camera2.PictureCaptureCallback
        public void onReady() {
            Camera2.this.captureStillPicture();
        }
    };
    CameraCaptureSession mCaptureSession;
    private int mDisplayOrientation;
    private int mFacing;
    private int mFlash;
    private ImageReader mImageReader;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        /* class com.master.cameralibrary.Camera2.AnonymousClass4 */

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x002d, code lost:
            if (r4 != null) goto L_0x002f;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x002f, code lost:
            if (r0 != null) goto L_0x0031;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
            r4.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x0035, code lost:
            r4 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0036, code lost:
            r0.addSuppressed(r4);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x003a, code lost:
            r4.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:7:0x0029, code lost:
            r1 = move-exception;
         */
        public void onImageAvailable(ImageReader imageReader) {
            Image acquireNextImage = imageReader.acquireNextImage();
            Image.Plane[] planes = acquireNextImage.getPlanes();
            if (planes.length > 0) {
                ByteBuffer buffer = planes[0].getBuffer();
                byte[] bArr = new byte[buffer.remaining()];
                buffer.get(bArr);
                Camera2.this.mCallback.onPictureTaken(bArr);
            }
            if (acquireNextImage != null) {
                acquireNextImage.close();
                return;
            }
            return;
        }
    };
    private final SizeMap mPictureSizes = new SizeMap();
    CaptureRequest.Builder mPreviewRequestBuilder;
    private final SizeMap mPreviewSizes = new SizeMap();
    private final CameraCaptureSession.StateCallback mSessionCallback = new CameraCaptureSession.StateCallback() {
        /* class com.master.cameralibrary.Camera2.AnonymousClass2 */

        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
            if (Camera2.this.mCamera != null) {
                Camera2 camera2 = Camera2.this;
                camera2.mCaptureSession = cameraCaptureSession;
                camera2.updateAutoFocus();
                Camera2.this.updateFlash();
                try {
                    Camera2.this.mCaptureSession.setRepeatingRequest(Camera2.this.mPreviewRequestBuilder.build(), Camera2.this.mCaptureCallback, null);
                } catch (CameraAccessException e) {
                    Log.e(Camera2.TAG, "Failed to start camera preview because it couldn't access camera", e);
                } catch (IllegalStateException e2) {
                    Log.e(Camera2.TAG, "Failed to start camera preview.", e2);
                }
            }
        }

        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
            Log.e(Camera2.TAG, "Failed to configure capture session.");
        }

        public void onClosed(@NonNull CameraCaptureSession cameraCaptureSession) {
            if (Camera2.this.mCaptureSession != null && Camera2.this.mCaptureSession.equals(cameraCaptureSession)) {
                Camera2.this.mCaptureSession = null;
            }
        }
    };

    static {
        INTERNAL_FACINGS.put(0, 1);
        INTERNAL_FACINGS.put(1, 0);
    }

    Camera2(CameraViewImpl.Callback callback, PreviewImpl previewImpl, Context context) {
        super(callback, previewImpl);
        this.mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        this.mPreview.setCallback(new PreviewImpl.Callback() {
            /* class com.master.cameralibrary.Camera2.AnonymousClass5 */

            @Override // com.master.cameralibrary.PreviewImpl.Callback
            public void onSurfaceChanged() {
                Camera2.this.startCaptureSession();
            }
        });
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.CameraViewImpl
    public boolean start() {
        if (!chooseCameraIdByFacing()) {
            return false;
        }
        collectCameraInfo();
        prepareImageReader();
        startOpeningCamera();
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.CameraViewImpl
    public void stop() {
        CameraCaptureSession cameraCaptureSession = this.mCaptureSession;
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            this.mCaptureSession = null;
        }
        CameraDevice cameraDevice = this.mCamera;
        if (cameraDevice != null) {
            cameraDevice.close();
            this.mCamera = null;
        }
        ImageReader imageReader = this.mImageReader;
        if (imageReader != null) {
            imageReader.close();
            this.mImageReader = null;
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
        return this.mPreviewSizes.ratios();
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.CameraViewImpl
    public boolean setAspectRatio(AspectRatio aspectRatio) {
        if (aspectRatio == null || aspectRatio.equals(this.mAspectRatio) || !this.mPreviewSizes.ratios().contains(aspectRatio)) {
            return false;
        }
        this.mAspectRatio = aspectRatio;
        prepareImageReader();
        CameraCaptureSession cameraCaptureSession = this.mCaptureSession;
        if (cameraCaptureSession == null) {
            return true;
        }
        cameraCaptureSession.close();
        this.mCaptureSession = null;
        startCaptureSession();
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.CameraViewImpl
    public AspectRatio getAspectRatio() {
        return this.mAspectRatio;
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.CameraViewImpl
    public void setAutoFocus(boolean z) {
        if (this.mAutoFocus != z) {
            this.mAutoFocus = z;
            if (this.mPreviewRequestBuilder != null) {
                updateAutoFocus();
                CameraCaptureSession cameraCaptureSession = this.mCaptureSession;
                if (cameraCaptureSession != null) {
                    try {
                        cameraCaptureSession.setRepeatingRequest(this.mPreviewRequestBuilder.build(), this.mCaptureCallback, null);
                    } catch (CameraAccessException unused) {
                        this.mAutoFocus = !this.mAutoFocus;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.CameraViewImpl
    public boolean getAutoFocus() {
        return this.mAutoFocus;
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.CameraViewImpl
    public void setFlash(int i) {
        int i2 = this.mFlash;
        if (i2 != i) {
            this.mFlash = i;
            if (this.mPreviewRequestBuilder != null) {
                updateFlash();
                CameraCaptureSession cameraCaptureSession = this.mCaptureSession;
                if (cameraCaptureSession != null) {
                    try {
                        cameraCaptureSession.setRepeatingRequest(this.mPreviewRequestBuilder.build(), this.mCaptureCallback, null);
                    } catch (CameraAccessException unused) {
                        this.mFlash = i2;
                    }
                }
            }
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
        if (this.mAutoFocus) {
            lockFocus();
        } else {
            captureStillPicture();
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.master.cameralibrary.CameraViewImpl
    public void setDisplayOrientation(int i) {
        this.mDisplayOrientation = i;
        this.mPreview.setDisplayOrientation(this.mDisplayOrientation);
    }

    private boolean chooseCameraIdByFacing() {
        try {
            int i = INTERNAL_FACINGS.get(this.mFacing);
            String[] cameraIdList = this.mCameraManager.getCameraIdList();
            if (cameraIdList.length != 0) {
                for (String str : cameraIdList) {
                    CameraCharacteristics cameraCharacteristics = this.mCameraManager.getCameraCharacteristics(str);
                    Integer num = (Integer) cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                    if (!(num == null || num.intValue() == 2)) {
                        Integer num2 = (Integer) cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                        if (num2 == null) {
                            throw new NullPointerException("Unexpected state: LENS_FACING null");
                        } else if (num2.intValue() == i) {
                            this.mCameraId = str;
                            this.mCameraCharacteristics = cameraCharacteristics;
                            return true;
                        }
                    }
                }
                this.mCameraId = cameraIdList[0];
                this.mCameraCharacteristics = this.mCameraManager.getCameraCharacteristics(this.mCameraId);
                Integer num3 = (Integer) this.mCameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                if (num3 == null || num3.intValue() == 2) {
                    return false;
                }
                Integer num4 = (Integer) this.mCameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (num4 != null) {
                    int size = INTERNAL_FACINGS.size();
                    for (int i2 = 0; i2 < size; i2++) {
                        if (INTERNAL_FACINGS.valueAt(i2) == num4.intValue()) {
                            this.mFacing = INTERNAL_FACINGS.keyAt(i2);
                            return true;
                        }
                    }
                    this.mFacing = 0;
                    return true;
                }
                throw new NullPointerException("Unexpected state: LENS_FACING null");
            }
            throw new RuntimeException("No camera available.");
        } catch (CameraAccessException e) {
            throw new RuntimeException("Failed to get a list of camera devices", e);
        }
    }

    private void collectCameraInfo() {
        StreamConfigurationMap streamConfigurationMap = (StreamConfigurationMap) this.mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (streamConfigurationMap != null) {
            this.mPreviewSizes.clear();
            Size[] outputSizes = streamConfigurationMap.getOutputSizes(this.mPreview.getOutputClass());
            for (Size size : outputSizes) {
                int width = size.getWidth();
                int height = size.getHeight();
                if (width <= MAX_PREVIEW_WIDTH && height <= MAX_PREVIEW_HEIGHT) {
                    this.mPreviewSizes.add(new com.master.cameralibrary.Size(width, height));
                }
            }
            this.mPictureSizes.clear();
            collectPictureSizes(this.mPictureSizes, streamConfigurationMap);
            for (AspectRatio aspectRatio : this.mPreviewSizes.ratios()) {
                if (!this.mPictureSizes.ratios().contains(aspectRatio)) {
                    this.mPreviewSizes.remove(aspectRatio);
                }
            }
            if (!this.mPreviewSizes.ratios().contains(this.mAspectRatio)) {
                this.mAspectRatio = this.mPreviewSizes.ratios().iterator().next();
                return;
            }
            return;
        }
        throw new IllegalStateException("Failed to get configuration map: " + this.mCameraId);
    }

    /* access modifiers changed from: protected */
    public void collectPictureSizes(SizeMap sizeMap, StreamConfigurationMap streamConfigurationMap) {
        Size[] outputSizes = streamConfigurationMap.getOutputSizes(256);
        for (Size size : outputSizes) {
            this.mPictureSizes.add(new com.master.cameralibrary.Size(size.getWidth(), size.getHeight()));
        }
    }

    private void prepareImageReader() {
        ImageReader imageReader = this.mImageReader;
        if (imageReader != null) {
            imageReader.close();
        }
        com.master.cameralibrary.Size last = this.mPictureSizes.sizes(this.mAspectRatio).last();
        this.mImageReader = ImageReader.newInstance(last.getWidth(), last.getHeight(), 256, 2);
        this.mImageReader.setOnImageAvailableListener(this.mOnImageAvailableListener, null);
    }

    private void startOpeningCamera() {
        try {
            this.mCameraManager.openCamera(this.mCameraId, this.mCameraDeviceCallback, (Handler) null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to open camera: " + this.mCameraId, e);
        }
    }

    /* access modifiers changed from: package-private */
    public void startCaptureSession() {
        if (isCameraOpened() && this.mPreview.isReady() && this.mImageReader != null) {
            com.master.cameralibrary.Size chooseOptimalSize = chooseOptimalSize();
            this.mPreview.setBufferSize(chooseOptimalSize.getWidth(), chooseOptimalSize.getHeight());
            Surface surface = this.mPreview.getSurface();
            try {
                this.mPreviewRequestBuilder = this.mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                this.mPreviewRequestBuilder.addTarget(surface);
                this.mCamera.createCaptureSession(Arrays.asList(surface, this.mImageReader.getSurface()), this.mSessionCallback, null);
            } catch (CameraAccessException unused) {
                throw new RuntimeException("Failed to start camera session");
            }
        }
    }

    private com.master.cameralibrary.Size chooseOptimalSize() {
        int width = this.mPreview.getWidth();
        int height = this.mPreview.getHeight();
        if (width < height) {
            height = width;
            width = height;
        }
        SortedSet<com.master.cameralibrary.Size> sizes = this.mPreviewSizes.sizes(this.mAspectRatio);
        for (com.master.cameralibrary.Size size : sizes) {
            if (size.getWidth() >= width && size.getHeight() >= height) {
                return size;
            }
        }
        return sizes.last();
    }

    /* access modifiers changed from: package-private */
    public void updateAutoFocus() {
        if (this.mAutoFocus) {
            int[] iArr = (int[]) this.mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
            if (iArr == null || iArr.length == 0 || (iArr.length == 1 && iArr[0] == 0)) {
                this.mAutoFocus = false;
                this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, 0);
                return;
            }
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, 4);
            return;
        }
        this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, 0);
    }

    /* access modifiers changed from: package-private */
    public void updateFlash() {
        int i = this.mFlash;
        if (i == 0) {
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, 1);
            this.mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, 0);
        } else if (i == 1) {
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, 3);
            this.mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, 0);
        } else if (i == 2) {
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, 1);
            this.mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, 2);
        } else if (i == 3) {
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, 2);
            this.mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, 0);
        } else if (i == 4) {
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, 4);
            this.mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, 0);
        }
    }

    private void lockFocus() {
        this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, 1);
        try {
            this.mCaptureCallback.setState(1);
            this.mCaptureSession.capture(this.mPreviewRequestBuilder.build(), this.mCaptureCallback, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to lock focus.", e);
        }
    }

    /* access modifiers changed from: package-private */
    public void captureStillPicture() {
        try {
            CaptureRequest.Builder createCaptureRequest = this.mCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            createCaptureRequest.addTarget(this.mImageReader.getSurface());
            createCaptureRequest.set(CaptureRequest.CONTROL_AF_MODE, this.mPreviewRequestBuilder.get(CaptureRequest.CONTROL_AF_MODE));
            int i = this.mFlash;
            int i2 = 1;
            if (i == 0) {
                createCaptureRequest.set(CaptureRequest.CONTROL_AE_MODE, 1);
                createCaptureRequest.set(CaptureRequest.FLASH_MODE, 0);
            } else if (i == 1) {
                createCaptureRequest.set(CaptureRequest.CONTROL_AE_MODE, 3);
            } else if (i == 2) {
                createCaptureRequest.set(CaptureRequest.CONTROL_AE_MODE, 1);
                createCaptureRequest.set(CaptureRequest.FLASH_MODE, 2);
            } else if (i == 3) {
                createCaptureRequest.set(CaptureRequest.CONTROL_AE_MODE, 2);
            } else if (i == 4) {
                createCaptureRequest.set(CaptureRequest.CONTROL_AE_MODE, 2);
            }
            int intValue = ((Integer) this.mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue();
            CaptureRequest.Key key = CaptureRequest.JPEG_ORIENTATION;
            int i3 = this.mDisplayOrientation;
            if (this.mFacing != 1) {
                i2 = -1;
            }
            createCaptureRequest.set(key, Integer.valueOf(((intValue + (i3 * i2)) + 360) % 360));
            this.mCaptureSession.stopRepeating();
            this.mCaptureSession.capture(createCaptureRequest.build(), new CameraCaptureSession.CaptureCallback() {
                /* class com.master.cameralibrary.Camera2.AnonymousClass6 */

                public void onCaptureCompleted(@NonNull CameraCaptureSession cameraCaptureSession, @NonNull CaptureRequest captureRequest, @NonNull TotalCaptureResult totalCaptureResult) {
                    Camera2.this.unlockFocus();
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Cannot capture a still picture.", e);
        }
    }

    /* access modifiers changed from: package-private */
    public void unlockFocus() {
        this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, 2);
        try {
            this.mCaptureSession.capture(this.mPreviewRequestBuilder.build(), this.mCaptureCallback, null);
            updateAutoFocus();
            updateFlash();
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, 0);
            this.mCaptureSession.setRepeatingRequest(this.mPreviewRequestBuilder.build(), this.mCaptureCallback, null);
            this.mCaptureCallback.setState(0);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to restart camera preview.", e);
        }
    }

    /* access modifiers changed from: private */
    public static abstract class PictureCaptureCallback extends CameraCaptureSession.CaptureCallback {
        static final int STATE_CAPTURING = 5;
        static final int STATE_LOCKED = 2;
        static final int STATE_LOCKING = 1;
        static final int STATE_PRECAPTURE = 3;
        static final int STATE_PREVIEW = 0;
        static final int STATE_WAITING = 4;
        private int mState;

        public abstract void onPrecaptureRequired();

        public abstract void onReady();

        PictureCaptureCallback() {
        }

        /* access modifiers changed from: package-private */
        public void setState(int i) {
            this.mState = i;
        }

        public void onCaptureProgressed(@NonNull CameraCaptureSession cameraCaptureSession, @NonNull CaptureRequest captureRequest, @NonNull CaptureResult captureResult) {
            process(captureResult);
        }

        public void onCaptureCompleted(@NonNull CameraCaptureSession cameraCaptureSession, @NonNull CaptureRequest captureRequest, @NonNull TotalCaptureResult totalCaptureResult) {
            process(totalCaptureResult);
        }

        private void process(@NonNull CaptureResult captureResult) {
            int i = this.mState;
            if (i == 1) {
                Integer num = (Integer) captureResult.get(CaptureResult.CONTROL_AF_STATE);
                if (num != null) {
                    if (num.intValue() == 4 || num.intValue() == 5) {
                        Integer num2 = (Integer) captureResult.get(CaptureResult.CONTROL_AE_STATE);
                        if (num2 == null || num2.intValue() == 2) {
                            setState(5);
                            onReady();
                            return;
                        }
                        setState(2);
                        onPrecaptureRequired();
                    }
                }
            } else if (i == 3) {
                Integer num3 = (Integer) captureResult.get(CaptureResult.CONTROL_AE_STATE);
                if (num3 == null || num3.intValue() == 5 || num3.intValue() == 4 || num3.intValue() == 2) {
                    setState(4);
                }
            } else if (i == 4) {
                Integer num4 = (Integer) captureResult.get(CaptureResult.CONTROL_AE_STATE);
                if (num4 == null || num4.intValue() != 5) {
                    setState(5);
                    onReady();
                }
            }
        }
    }
}
