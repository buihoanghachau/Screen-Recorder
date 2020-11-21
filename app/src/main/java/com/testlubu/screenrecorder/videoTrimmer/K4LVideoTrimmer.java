package com.testlubu.screenrecorder.videoTrimmer;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.videoTrimmer.interfaces.OnK4LVideoListener;
import com.testlubu.screenrecorder.videoTrimmer.interfaces.OnProgressVideoListener;
import com.testlubu.screenrecorder.videoTrimmer.interfaces.OnRangeSeekBarListener;
import com.testlubu.screenrecorder.videoTrimmer.interfaces.OnTrimVideoListener;
import com.testlubu.screenrecorder.videoTrimmer.utils.BackgroundExecutor;
import com.testlubu.screenrecorder.videoTrimmer.utils.TrimVideoUtils;
import com.testlubu.screenrecorder.videoTrimmer.utils.UiThreadExecutor;
import com.testlubu.screenrecorder.videoTrimmer.view.ProgressBarView;
import com.testlubu.screenrecorder.videoTrimmer.view.RangeSeekBarView;
import com.testlubu.screenrecorder.videoTrimmer.view.TimeLineView;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class K4LVideoTrimmer extends FrameLayout {
    private static final int MIN_TIME_FRAME = 1000;
    private static final int SHOW_PROGRESS = 2;
    private static final String TAG = "K4LVideoTrimmer";
    private int mDuration;
    private int mEndPosition;
    private String mFinalPath;
    private SeekBar mHolderTopView;
    private RelativeLayout mLinearVideo;
    private List<OnProgressVideoListener> mListeners;
    private int mMaxDuration;
    private final MessageHandler mMessageHandler;
    private OnK4LVideoListener mOnK4LVideoListener;
    private OnTrimVideoListener mOnTrimVideoListener;
    private long mOriginSizeFile;
    private ImageView mPlayView;
    private RangeSeekBarView mRangeSeekBarView;
    private boolean mResetSeekBar;
    private Uri mSrc;
    private int mStartPosition;
    private TextView mTextSize;
    private TextView mTextTime;
    private TextView mTextTimeFrame;
    private View mTimeInfoContainer;
    private TimeLineView mTimeLineView;
    private int mTimeVideo;
    private ProgressBarView mVideoProgressIndicator;
    private VideoView mVideoView;

    public K4LVideoTrimmer(@NonNull Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public K4LVideoTrimmer(@NonNull Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mDuration = 0;
        this.mTimeVideo = 0;
        this.mStartPosition = 0;
        this.mEndPosition = 0;
        this.mResetSeekBar = true;
        this.mMessageHandler = new MessageHandler(this);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_time_line, (ViewGroup) this, true);
        this.mHolderTopView = (SeekBar) findViewById(R.id.handlerTop);
        this.mVideoProgressIndicator = (ProgressBarView) findViewById(R.id.timeVideoView);
        this.mRangeSeekBarView = (RangeSeekBarView) findViewById(R.id.timeLineBar);
        this.mLinearVideo = (RelativeLayout) findViewById(R.id.layout_surface_view);
        this.mVideoView = (VideoView) findViewById(R.id.video_loader);
        this.mPlayView = (ImageView) findViewById(R.id.icon_video_play);
        this.mTimeInfoContainer = findViewById(R.id.timeText);
        this.mTextSize = (TextView) findViewById(R.id.textSize);
        this.mTextTimeFrame = (TextView) findViewById(R.id.textTimeSelection);
        this.mTextTime = (TextView) findViewById(R.id.textTime);
        this.mTimeLineView = (TimeLineView) findViewById(R.id.timeLineView);
        setUpListeners();
        setUpMargins();
    }

    private void setUpListeners() {
        this.mListeners = new ArrayList();
        this.mListeners.add(new OnProgressVideoListener() {
            /* class com.testlubu.screenrecorder.videoTrimmer.K4LVideoTrimmer.AnonymousClass1 */

            @Override // com.testlubu.screenrecorder.videoTrimmer.interfaces.OnProgressVideoListener
            public void updateProgress(int i, int i2, float f) {
                K4LVideoTrimmer.this.updateVideoProgress(i);
            }
        });
        this.mListeners.add(this.mVideoProgressIndicator);
        findViewById(R.id.btCancel).setOnClickListener(new View.OnClickListener() {
            /* class com.testlubu.screenrecorder.videoTrimmer.K4LVideoTrimmer.AnonymousClass2 */

            public void onClick(View view) {
                K4LVideoTrimmer.this.onCancelClicked();
            }
        });
        findViewById(R.id.btSave).setOnClickListener(new View.OnClickListener() {
            /* class com.testlubu.screenrecorder.videoTrimmer.K4LVideoTrimmer.AnonymousClass3 */

            public void onClick(View view) {
                K4LVideoTrimmer.this.onSaveClicked();
            }
        });
        final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            /* class com.testlubu.screenrecorder.videoTrimmer.K4LVideoTrimmer.AnonymousClass4 */

            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                K4LVideoTrimmer.this.onClickVideoPlayPause();
                return true;
            }
        });
        this.mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            /* class com.testlubu.screenrecorder.videoTrimmer.K4LVideoTrimmer.AnonymousClass5 */

            public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
                if (K4LVideoTrimmer.this.mOnTrimVideoListener == null) {
                    return false;
                }
                OnTrimVideoListener onTrimVideoListener = K4LVideoTrimmer.this.mOnTrimVideoListener;
                onTrimVideoListener.onError("Something went wrong reason : " + i);
                return false;
            }
        });
        this.mVideoView.setOnTouchListener(new View.OnTouchListener() {
            /* class com.testlubu.screenrecorder.videoTrimmer.K4LVideoTrimmer.AnonymousClass6 */

            public boolean onTouch(View view, @NonNull MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });
        this.mRangeSeekBarView.addOnRangeSeekBarListener(new OnRangeSeekBarListener() {
            /* class com.testlubu.screenrecorder.videoTrimmer.K4LVideoTrimmer.AnonymousClass7 */

            @Override // com.testlubu.screenrecorder.videoTrimmer.interfaces.OnRangeSeekBarListener
            public void onCreate(RangeSeekBarView rangeSeekBarView, int i, float f) {
            }

            @Override // com.testlubu.screenrecorder.videoTrimmer.interfaces.OnRangeSeekBarListener
            public void onSeekStart(RangeSeekBarView rangeSeekBarView, int i, float f) {
            }

            @Override // com.testlubu.screenrecorder.videoTrimmer.interfaces.OnRangeSeekBarListener
            public void onSeek(RangeSeekBarView rangeSeekBarView, int i, float f) {
                K4LVideoTrimmer.this.onSeekThumbs(i, f);
            }

            @Override // com.testlubu.screenrecorder.videoTrimmer.interfaces.OnRangeSeekBarListener
            public void onSeekStop(RangeSeekBarView rangeSeekBarView, int i, float f) {
                K4LVideoTrimmer.this.onStopSeekThumbs();
            }
        });
        this.mRangeSeekBarView.addOnRangeSeekBarListener(this.mVideoProgressIndicator);
        this.mHolderTopView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /* class com.testlubu.screenrecorder.videoTrimmer.K4LVideoTrimmer.AnonymousClass8 */

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                K4LVideoTrimmer.this.onPlayerIndicatorSeekChanged(i, z);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                K4LVideoTrimmer.this.onPlayerIndicatorSeekStart();
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                K4LVideoTrimmer.this.onPlayerIndicatorSeekStop(seekBar);
            }
        });
        this.mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            /* class com.testlubu.screenrecorder.videoTrimmer.K4LVideoTrimmer.AnonymousClass9 */

            public void onPrepared(MediaPlayer mediaPlayer) {
                K4LVideoTrimmer.this.onVideoPrepared(mediaPlayer);
            }
        });
        this.mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            /* class com.testlubu.screenrecorder.videoTrimmer.K4LVideoTrimmer.AnonymousClass10 */

            public void onCompletion(MediaPlayer mediaPlayer) {
                K4LVideoTrimmer.this.onVideoCompleted();
            }
        });
    }

    private void setUpMargins() {
        int widthBitmap = this.mRangeSeekBarView.getThumbs().get(0).getWidthBitmap();
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mHolderTopView.getLayoutParams();
        int minimumWidth = widthBitmap - (this.mHolderTopView.getThumb().getMinimumWidth() / 2);
        layoutParams.setMargins(minimumWidth, 0, minimumWidth, 0);
        this.mHolderTopView.setLayoutParams(layoutParams);
        RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) this.mTimeLineView.getLayoutParams();
        layoutParams2.setMargins(widthBitmap, 0, widthBitmap, 0);
        this.mTimeLineView.setLayoutParams(layoutParams2);
        RelativeLayout.LayoutParams layoutParams3 = (RelativeLayout.LayoutParams) this.mVideoProgressIndicator.getLayoutParams();
        layoutParams3.setMargins(widthBitmap, 0, widthBitmap, 0);
        this.mVideoProgressIndicator.setLayoutParams(layoutParams3);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSaveClicked() {
        if (this.mStartPosition > 0 || this.mEndPosition < this.mDuration) {
            this.mPlayView.setVisibility(0);
            this.mVideoView.pause();
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(getContext(), this.mSrc);
            long parseLong = Long.parseLong(mediaMetadataRetriever.extractMetadata(9));
            final File file = new File(this.mSrc.getPath());
            int i = this.mTimeVideo;
            if (i < 1000) {
                int i2 = this.mEndPosition;
                if (parseLong - ((long) i2) > ((long) (1000 - i))) {
                    this.mEndPosition = i2 + (1000 - i);
                } else {
                    int i3 = this.mStartPosition;
                    if (i3 > 1000 - i) {
                        this.mStartPosition = i3 - (1000 - i);
                    }
                }
            }
            OnTrimVideoListener onTrimVideoListener = this.mOnTrimVideoListener;
            if (onTrimVideoListener != null) {
                onTrimVideoListener.onTrimStarted();
            }
            BackgroundExecutor.execute(new BackgroundExecutor.Task("", 0, "") {
                /* class com.testlubu.screenrecorder.videoTrimmer.K4LVideoTrimmer.AnonymousClass11 */

                @Override // com.testlubu.screenrecorder.videoTrimmer.utils.BackgroundExecutor.Task
                public void execute() {
                    try {
                        TrimVideoUtils.startTrim(file, K4LVideoTrimmer.this.getDestinationPath(), (long) K4LVideoTrimmer.this.mStartPosition, (long) K4LVideoTrimmer.this.mEndPosition, K4LVideoTrimmer.this.mOnTrimVideoListener);
                    } catch (Throwable th) {
                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), th);
                    }
                }
            });
            return;
        }
        OnTrimVideoListener onTrimVideoListener2 = this.mOnTrimVideoListener;
        if (onTrimVideoListener2 != null) {
            onTrimVideoListener2.getResult(this.mSrc);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onClickVideoPlayPause() {
        if (this.mVideoView.isPlaying()) {
            this.mPlayView.setVisibility(0);
            this.mMessageHandler.removeMessages(2);
            this.mVideoView.pause();
            return;
        }
        this.mPlayView.setVisibility(8);
        if (this.mResetSeekBar) {
            this.mResetSeekBar = false;
            this.mVideoView.seekTo(this.mStartPosition);
        }
        this.mMessageHandler.sendEmptyMessage(2);
        this.mVideoView.start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onCancelClicked() {
        this.mVideoView.stopPlayback();
        OnTrimVideoListener onTrimVideoListener = this.mOnTrimVideoListener;
        if (onTrimVideoListener != null) {
            onTrimVideoListener.cancelAction();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getDestinationPath() {
        if (this.mFinalPath == null) {
            File externalStorageDirectory = Environment.getExternalStorageDirectory();
            this.mFinalPath = externalStorageDirectory.getPath() + File.separator;
            String str = TAG;
            Log.d(str, "Using default path " + this.mFinalPath);
        }
        return this.mFinalPath;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPlayerIndicatorSeekChanged(int i, boolean z) {
        int i2 = (int) (((long) (this.mDuration * i)) / 1000);
        if (z) {
            int i3 = this.mStartPosition;
            if (i2 < i3) {
                setProgressBarPosition(i3);
                i2 = this.mStartPosition;
            } else {
                int i4 = this.mEndPosition;
                if (i2 > i4) {
                    setProgressBarPosition(i4);
                    i2 = this.mEndPosition;
                }
            }
            setTimeVideo(i2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPlayerIndicatorSeekStart() {
        this.mMessageHandler.removeMessages(2);
        this.mVideoView.pause();
        this.mPlayView.setVisibility(0);
        notifyProgressUpdate(false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPlayerIndicatorSeekStop(@NonNull SeekBar seekBar) {
        this.mMessageHandler.removeMessages(2);
        this.mVideoView.pause();
        this.mPlayView.setVisibility(0);
        int progress = (int) (((long) (this.mDuration * seekBar.getProgress())) / 1000);
        this.mVideoView.seekTo(progress);
        setTimeVideo(progress);
        notifyProgressUpdate(false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onVideoPrepared(@NonNull MediaPlayer mediaPlayer) {
        float videoWidth = ((float) mediaPlayer.getVideoWidth()) / ((float) mediaPlayer.getVideoHeight());
        int width = this.mLinearVideo.getWidth();
        int height = this.mLinearVideo.getHeight();
        float f = (float) width;
        float f2 = (float) height;
        ViewGroup.LayoutParams layoutParams = this.mVideoView.getLayoutParams();
        if (videoWidth > f / f2) {
            layoutParams.width = width;
            layoutParams.height = (int) (f / videoWidth);
        } else {
            layoutParams.width = (int) (videoWidth * f2);
            layoutParams.height = height;
        }
        this.mVideoView.setLayoutParams(layoutParams);
        this.mPlayView.setVisibility(0);
        this.mDuration = this.mVideoView.getDuration();
        setSeekBarPosition();
        setTimeFrames();
        setTimeVideo(0);
        OnK4LVideoListener onK4LVideoListener = this.mOnK4LVideoListener;
        if (onK4LVideoListener != null) {
            onK4LVideoListener.onVideoPrepared();
        }
    }

    private void setSeekBarPosition() {
        int i = this.mDuration;
        int i2 = this.mMaxDuration;
        if (i >= i2) {
            this.mStartPosition = (i / 2) - (i2 / 2);
            this.mEndPosition = (i / 2) + (i2 / 2);
            this.mRangeSeekBarView.setThumbValue(0, (float) ((this.mStartPosition * 100) / i));
            this.mRangeSeekBarView.setThumbValue(1, (float) ((this.mEndPosition * 100) / this.mDuration));
        } else {
            this.mStartPosition = 0;
            this.mEndPosition = i;
        }
        setProgressBarPosition(this.mStartPosition);
        this.mVideoView.seekTo(this.mStartPosition);
        this.mTimeVideo = this.mDuration;
        this.mRangeSeekBarView.initMaxWidth();
    }

    private void setTimeFrames() {
        String string = getContext().getString(R.string.short_seconds);
        this.mTextTimeFrame.setText(String.format("%s %s - %s %s", TrimVideoUtils.stringForTime(this.mStartPosition), string, TrimVideoUtils.stringForTime(this.mEndPosition), string));
    }

    private void setTimeVideo(int i) {
        String string = getContext().getString(R.string.short_seconds);
        this.mTextTime.setText(String.format("%s %s", TrimVideoUtils.stringForTime(i), string));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSeekThumbs(int i, float f) {
        if (i == 0) {
            this.mStartPosition = (int) ((((float) this.mDuration) * f) / 100.0f);
            this.mVideoView.seekTo(this.mStartPosition);
        } else if (i == 1) {
            this.mEndPosition = (int) ((((float) this.mDuration) * f) / 100.0f);
        }
        setProgressBarPosition(this.mStartPosition);
        setTimeFrames();
        this.mTimeVideo = this.mEndPosition - this.mStartPosition;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onStopSeekThumbs() {
        this.mMessageHandler.removeMessages(2);
        this.mVideoView.pause();
        this.mPlayView.setVisibility(0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onVideoCompleted() {
        this.mVideoView.seekTo(this.mStartPosition);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyProgressUpdate(boolean z) {
        if (this.mDuration != 0) {
            int currentPosition = this.mVideoView.getCurrentPosition();
            if (z) {
                for (OnProgressVideoListener onProgressVideoListener : this.mListeners) {
                    int i = this.mDuration;
                    onProgressVideoListener.updateProgress(currentPosition, i, (float) ((currentPosition * 100) / i));
                }
                return;
            }
            int i2 = this.mDuration;
            this.mListeners.get(1).updateProgress(currentPosition, i2, (float) ((currentPosition * 100) / i2));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateVideoProgress(int i) {
        if (this.mVideoView != null) {
            if (i >= this.mEndPosition) {
                this.mMessageHandler.removeMessages(2);
                this.mVideoView.pause();
                this.mPlayView.setVisibility(0);
                this.mResetSeekBar = true;
                return;
            }
            if (this.mHolderTopView != null) {
                setProgressBarPosition(i);
            }
            setTimeVideo(i);
        }
    }

    private void setProgressBarPosition(int i) {
        int i2 = this.mDuration;
        if (i2 > 0) {
            this.mHolderTopView.setProgress((int) ((((long) i) * 1000) / ((long) i2)));
        }
    }

    public void setVideoInformationVisibility(boolean z) {
        this.mTimeInfoContainer.setVisibility(z ? 0 : 8);
    }

    public void setOnTrimVideoListener(OnTrimVideoListener onTrimVideoListener) {
        this.mOnTrimVideoListener = onTrimVideoListener;
    }

    public void setOnK4LVideoListener(OnK4LVideoListener onK4LVideoListener) {
        this.mOnK4LVideoListener = onK4LVideoListener;
    }

    public void setDestinationPath(String str) {
        this.mFinalPath = str + File.separator;
        String str2 = TAG;
        Log.d(str2, "Setting custom path " + this.mFinalPath);
    }

    public void destroy() {
        BackgroundExecutor.cancelAll("", true);
        UiThreadExecutor.cancelAll("");
    }

    public void setMaxDuration(int i) {
        this.mMaxDuration = i * 1000;
    }

    public void setVideoURI(Uri uri) {
        this.mSrc = uri;
        if (this.mOriginSizeFile == 0) {
            this.mOriginSizeFile = new File(this.mSrc.getPath()).length();
            long j = this.mOriginSizeFile / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID;
            if (j > 1000) {
                long j2 = j / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID;
                this.mTextSize.setText(String.format("%s %s", Long.valueOf(j2), getContext().getString(R.string.megabyte)));
            } else {
                this.mTextSize.setText(String.format("%s %s", Long.valueOf(j), getContext().getString(R.string.kilobyte)));
            }
        }
        this.mVideoView.setVideoURI(this.mSrc);
        this.mVideoView.requestFocus();
        this.mTimeLineView.setVideo(this.mSrc);
    }

    /* access modifiers changed from: private */
    public static class MessageHandler extends Handler {
        @NonNull
        private final WeakReference<K4LVideoTrimmer> mView;

        MessageHandler(K4LVideoTrimmer k4LVideoTrimmer) {
            this.mView = new WeakReference<>(k4LVideoTrimmer);
        }

        public void handleMessage(Message message) {
            K4LVideoTrimmer k4LVideoTrimmer = this.mView.get();
            if (k4LVideoTrimmer != null && k4LVideoTrimmer.mVideoView != null) {
                k4LVideoTrimmer.notifyProgressUpdate(true);
                if (k4LVideoTrimmer.mVideoView.isPlaying()) {
                    sendEmptyMessageDelayed(0, 10);
                }
            }
        }
    }
}
