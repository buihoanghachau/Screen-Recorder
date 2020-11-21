package com.master.cameralibrary;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Size;
import com.master.cameralibrary.CameraViewImpl;

@TargetApi(23)
class Camera2Api23 extends Camera2 {
    Camera2Api23(CameraViewImpl.Callback callback, PreviewImpl previewImpl, Context context) {
        super(callback, previewImpl, context);
    }

    /* access modifiers changed from: protected */
    @Override // com.master.cameralibrary.Camera2
    public void collectPictureSizes(SizeMap sizeMap, StreamConfigurationMap streamConfigurationMap) {
        if (streamConfigurationMap.getHighResolutionOutputSizes(256) != null) {
            Size[] highResolutionOutputSizes = streamConfigurationMap.getHighResolutionOutputSizes(256);
            for (Size size : highResolutionOutputSizes) {
                sizeMap.add(new com.master.cameralibrary.Size(size.getWidth(), size.getHeight()));
            }
        }
        if (sizeMap.isEmpty()) {
            super.collectPictureSizes(sizeMap, streamConfigurationMap);
        }
    }
}
