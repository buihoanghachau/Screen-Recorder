package com.testlubu.screenrecorder.encoder;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import java.io.IOException;
import java.io.OutputStream;

public class GifEncoder {
    protected boolean closeStream = false;
    protected int colorDepth;
    protected byte[] colorTab;
    protected int delay = 0;
    protected int dispose = -1;
    protected boolean firstFrame = true;
    protected int height;
    protected Bitmap image;
    protected byte[] indexedPixels;
    protected OutputStream out;
    protected int palSize = 7;
    protected byte[] pixels;
    protected int repeat = -1;
    protected int sample = 10;
    protected boolean sizeSet = false;
    protected boolean started = false;
    protected int transIndex;
    protected int transparent = -1;
    protected boolean[] usedEntry = new boolean[256];
    protected int width;
    protected int x = 0;
    protected int y = 0;

    public void setDelay(int i) {
        this.delay = i / 10;
    }

    public void setDispose(int i) {
        if (i >= 0) {
            this.dispose = i;
        }
    }

    public void setRepeat(int i) {
        if (i >= 0) {
            this.repeat = i;
        }
    }

    public void setTransparent(int i) {
        this.transparent = i;
    }

    public boolean addFrame(Bitmap bitmap) {
        if (bitmap == null || !this.started) {
            return false;
        }
        try {
            if (!this.sizeSet) {
                setSize(bitmap.getWidth(), bitmap.getHeight());
            }
            this.image = bitmap;
            getImagePixels();
            analyzePixels();
            if (this.firstFrame) {
                writeLSD();
                writePalette();
                if (this.repeat >= 0) {
                    writeNetscapeExt();
                }
            }
            writeGraphicCtrlExt();
            writeImageDesc();
            if (!this.firstFrame) {
                writePalette();
            }
            writePixels();
            this.firstFrame = false;
            return true;
        } catch (IOException unused) {
            return false;
        }
    }

    public boolean finish() {
        boolean z;
        if (!this.started) {
            return false;
        }
        this.started = false;
        try {
            this.out.write(59);
            this.out.flush();
            if (this.closeStream) {
                this.out.close();
            }
            z = true;
        } catch (IOException unused) {
            z = false;
        }
        this.transIndex = 0;
        this.out = null;
        this.image = null;
        this.pixels = null;
        this.indexedPixels = null;
        this.colorTab = null;
        this.closeStream = false;
        this.firstFrame = true;
        return z;
    }

    public void setFrameRate(float f) {
        if (f != 0.0f) {
            this.delay = (int) (100.0f / f);
        }
    }

    public void setQuality(int i) {
        if (i < 1) {
            i = 1;
        }
        this.sample = i;
    }

    public void setSize(int i, int i2) {
        this.width = i;
        this.height = i2;
        if (this.width < 1) {
            this.width = 320;
        }
        if (this.height < 1) {
            this.height = 240;
        }
        this.sizeSet = true;
    }

    public void setPosition(int i, int i2) {
        this.x = i;
        this.y = i2;
    }

    public boolean start(OutputStream outputStream) {
        boolean z = false;
        if (outputStream == null) {
            return false;
        }
        this.closeStream = false;
        this.out = outputStream;
        try {
            writeString("GIF89a");
            z = true;
        } catch (IOException unused) {
        }
        this.started = z;
        return z;
    }

    /* access modifiers changed from: protected */
    public void analyzePixels() {
        byte[] bArr = this.pixels;
        int length = bArr.length;
        int i = length / 3;
        this.indexedPixels = new byte[i];
        NeuQuant neuQuant = new NeuQuant(bArr, length, this.sample);
        this.colorTab = neuQuant.process();
        int i2 = 0;
        int i3 = 0;
        while (true) {
            byte[] bArr2 = this.colorTab;
            if (i3 >= bArr2.length) {
                break;
            }
            byte b = bArr2[i3];
            int i4 = i3 + 2;
            bArr2[i3] = bArr2[i4];
            bArr2[i4] = b;
            this.usedEntry[i3 / 3] = false;
            i3 += 3;
        }
        int i5 = 0;
        while (i2 < i) {
            byte[] bArr3 = this.pixels;
            int i6 = i5 + 1;
            int i7 = i6 + 1;
            int map = neuQuant.map(bArr3[i5] & 255, bArr3[i6] & 255, bArr3[i7] & 255);
            this.usedEntry[map] = true;
            this.indexedPixels[i2] = (byte) map;
            i2++;
            i5 = i7 + 1;
        }
        this.pixels = null;
        this.colorDepth = 8;
        this.palSize = 7;
        int i8 = this.transparent;
        if (i8 != -1) {
            this.transIndex = findClosest(i8);
        }
    }

    /* access modifiers changed from: protected */
    public int findClosest(int i) {
        byte[] bArr = this.colorTab;
        if (bArr == null) {
            return -1;
        }
        int i2 = (i >> 16) & 255;
        int i3 = (i >> 8) & 255;
        int i4 = 0;
        int i5 = (i >> 0) & 255;
        int length = bArr.length;
        int i6 = 0;
        int i7 = 16777216;
        while (i4 < length) {
            byte[] bArr2 = this.colorTab;
            int i8 = i4 + 1;
            int i9 = i2 - (bArr2[i4] & 255);
            int i10 = i8 + 1;
            int i11 = i3 - (bArr2[i8] & 255);
            int i12 = i5 - (bArr2[i10] & 255);
            int i13 = (i9 * i9) + (i11 * i11) + (i12 * i12);
            int i14 = i10 / 3;
            if (this.usedEntry[i14] && i13 < i7) {
                i7 = i13;
                i6 = i14;
            }
            i4 = i10 + 1;
        }
        return i6;
    }

    /* access modifiers changed from: protected */
    public void getImagePixels() {
        int width2 = this.image.getWidth();
        int height2 = this.image.getHeight();
        if (!(width2 == this.width && height2 == this.height)) {
            Bitmap createBitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.RGB_565);
            new Canvas(createBitmap).drawBitmap(this.image, 0.0f, 0.0f, new Paint());
            this.image = createBitmap;
        }
        int[] imageData = getImageData(this.image);
        this.pixels = new byte[(imageData.length * 3)];
        for (int i = 0; i < imageData.length; i++) {
            int i2 = imageData[i];
            int i3 = i * 3;
            byte[] bArr = this.pixels;
            int i4 = i3 + 1;
            bArr[i3] = (byte) ((i2 >> 0) & 255);
            bArr[i4] = (byte) ((i2 >> 8) & 255);
            bArr[i4 + 1] = (byte) ((i2 >> 16) & 255);
        }
    }

    /* access modifiers changed from: protected */
    public int[] getImageData(Bitmap bitmap) {
        int width2 = bitmap.getWidth();
        int height2 = bitmap.getHeight();
        int[] iArr = new int[(width2 * height2)];
        bitmap.getPixels(iArr, 0, width2, 0, 0, width2, height2);
        return iArr;
    }

    /* access modifiers changed from: protected */
    public void writeGraphicCtrlExt() throws IOException {
        int i;
        int i2;
        this.out.write(33);
        this.out.write(249);
        this.out.write(4);
        if (this.transparent == -1) {
            i2 = 0;
            i = 0;
        } else {
            i2 = 1;
            i = 2;
        }
        int i3 = this.dispose;
        if (i3 >= 0) {
            i = i3 & 7;
        }
        this.out.write(i2 | (i << 2) | 0 | 0);
        writeShort(this.delay);
        this.out.write(this.transIndex);
        this.out.write(0);
    }

    /* access modifiers changed from: protected */
    public void writeImageDesc() throws IOException {
        this.out.write(44);
        writeShort(this.x);
        writeShort(this.y);
        writeShort(this.width);
        writeShort(this.height);
        if (this.firstFrame) {
            this.out.write(0);
        } else {
            this.out.write(this.palSize | 128);
        }
    }

    /* access modifiers changed from: protected */
    public void writeLSD() throws IOException {
        writeShort(this.width);
        writeShort(this.height);
        this.out.write(this.palSize | 240);
        this.out.write(0);
        this.out.write(0);
    }

    /* access modifiers changed from: protected */
    public void writeNetscapeExt() throws IOException {
        this.out.write(33);
        this.out.write(255);
        this.out.write(11);
        writeString("NETSCAPE2.0");
        this.out.write(3);
        this.out.write(1);
        writeShort(this.repeat);
        this.out.write(0);
    }

    /* access modifiers changed from: protected */
    public void writePalette() throws IOException {
        OutputStream outputStream = this.out;
        byte[] bArr = this.colorTab;
        outputStream.write(bArr, 0, bArr.length);
        int length = 768 - this.colorTab.length;
        for (int i = 0; i < length; i++) {
            this.out.write(0);
        }
    }

    /* access modifiers changed from: protected */
    public void writePixels() throws IOException {
        new LZWEncoder(this.width, this.height, this.indexedPixels, this.colorDepth).encode(this.out);
    }

    /* access modifiers changed from: protected */
    public void writeShort(int i) throws IOException {
        this.out.write(i & 255);
        this.out.write((i >> 8) & 255);
    }

    /* access modifiers changed from: protected */
    public void writeString(String str) throws IOException {
        for (int i = 0; i < str.length(); i++) {
            this.out.write((byte) str.charAt(i));
        }
    }
}
