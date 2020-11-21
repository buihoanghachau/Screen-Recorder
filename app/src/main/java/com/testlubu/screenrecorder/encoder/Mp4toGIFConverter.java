package com.testlubu.screenrecorder.encoder;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import com.testlubu.screenrecorder.common.Const;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Mp4toGIFConverter {
    private Context context;
    private long maxDur;
    private MediaMetadataRetriever mediaMetadataRetriever;
    private Uri videoUri;

    public Mp4toGIFConverter(Context context2) {
        this();
        this.context = context2;
    }

    private Mp4toGIFConverter() {
        this.maxDur = 5000;
        this.mediaMetadataRetriever = new MediaMetadataRetriever();
    }

    public void setVideoUri(Uri uri) {
        this.videoUri = uri;
    }

    public void convertToGif() {
        try {
            this.mediaMetadataRetriever.setDataSource(this.context, this.videoUri);
            this.maxDur = (long) Double.parseDouble(this.mediaMetadataRetriever.extractMetadata(9));
            Log.d(Const.TAG, "max dur is" + this.maxDur);
            new TaskSaveGIF().execute(new Void[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            Toast.makeText(this.context, "Something Wrong!", 1).show();
        }
    }

    public class TaskSaveGIF extends AsyncTask<Void, Integer, String> {
        ProgressDialog dialog = new ProgressDialog(Mp4toGIFConverter.this.context);

        public TaskSaveGIF() {
        }

        private String getGifFIleName() {
            return Mp4toGIFConverter.this.videoUri.getLastPathSegment().replace("mp4", "gif");
        }

        /* access modifiers changed from: protected */
        public String doInBackground(Void... voidArr) {
            String file = Environment.getExternalStorageDirectory().toString();
            File file2 = new File(file + File.separator + Const.APPDIR, getGifFIleName());
            try {
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file2));
                bufferedOutputStream.write(genGIF());
                bufferedOutputStream.flush();
                bufferedOutputStream.close();
                return file2.getAbsolutePath() + " Saved";
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return e.getMessage();
            } catch (IOException e2) {
                e2.printStackTrace();
                return e2.getMessage();
            }
        }

        /* access modifiers changed from: protected */
        public void onPreExecute() {
            this.dialog.setTitle("Please wait. Saving GIF");
            this.dialog.setCancelable(false);
            this.dialog.setProgressStyle(1);
            this.dialog.setMax(100);
            this.dialog.show();
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(String str) {
            Toast.makeText(Mp4toGIFConverter.this.context, str, 1).show();
            this.dialog.cancel();
        }

        /* access modifiers changed from: protected */
        public void onProgressUpdate(Integer... numArr) {
            this.dialog.setProgress(numArr[0].intValue());
            Log.d(Const.TAG, "Gif save progress: " + numArr[0]);
        }

        private byte[] genGIF() {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            GifEncoder gifEncoder = new GifEncoder();
            gifEncoder.setDelay(1000);
            gifEncoder.setRepeat(0);
            gifEncoder.setQuality(15);
            gifEncoder.setFrameRate(20.0f);
            gifEncoder.start(byteArrayOutputStream);
            for (int i = 0; i < 100; i += 10) {
                long j = (Mp4toGIFConverter.this.maxDur * ((long) i)) / 100;
                Log.d(Const.TAG, "GIF GETTING FRAME AT: " + j + "ms");
                gifEncoder.addFrame(Mp4toGIFConverter.this.mediaMetadataRetriever.getFrameAtTime(j));
                publishProgress(Integer.valueOf(i));
            }
            gifEncoder.addFrame(Mp4toGIFConverter.this.mediaMetadataRetriever.getFrameAtTime(Mp4toGIFConverter.this.maxDur));
            publishProgress(100);
            gifEncoder.finish();
            return byteArrayOutputStream.toByteArray();
        }
    }
}
