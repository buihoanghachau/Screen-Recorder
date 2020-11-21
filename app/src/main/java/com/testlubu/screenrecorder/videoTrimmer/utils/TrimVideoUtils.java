package com.testlubu.screenrecorder.videoTrimmer.utils;

import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.FileDataSourceViaHeapImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;
import com.testlubu.screenrecorder.videoTrimmer.interfaces.OnTrimVideoListener;

import java.io.*;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class TrimVideoUtils {
    private static final String TAG = "TrimVideoUtils";

    public static void startTrim(@NonNull File file, @NonNull String str, long j, long j2, @NonNull OnTrimVideoListener onTrimVideoListener) throws IOException {
        String str2 = str + ("MP4_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".mp4");
        File file2 = new File(str2);
        file2.getParentFile().mkdirs();
        Log.d(TAG, "Generated file path " + str2);
        genVideoUsingMp4Parser(file, file2, j, j2, onTrimVideoListener);
    }
    private static void genVideoUsingMp4Parser(@NonNull File file, @NonNull File file2, long j, long j2, @NonNull OnTrimVideoListener onTrimVideoListener) throws IOException {
        Movie build = MovieCreator.build(new FileDataSourceViaHeapImpl(file.getAbsolutePath()));
        List<Track> tracks = build.getTracks();
        build.setTracks(new LinkedList());
        double d = (double) (j / 1000);
        double d2 = (double) (j2 / 1000);
        boolean z = false;
        for (Track track : tracks) {
            if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                if (!z) {
                    d = correctTimeToSyncSample(track, d, false);
                    d2 = correctTimeToSyncSample(track, d2, true);
                    z = true;
                } else {
                    throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
                }
            }
        }
        for (Track track2 : tracks) {
            long j3 = 0;
            double d3 = -1.0d;
            long j4 = -1;
            int i = 0;
            double d4 = 0.0d;
            long j5 = -1;
            while (i < track2.getSampleDurations().length) {
                long j6 = track2.getSampleDurations()[i];
                int i2 = (d4 > d3 ? 1 : (d4 == d3 ? 0 : -1));
                if (i2 > 0 && d4 <= d) {
                    j5 = j3;
                }
                if (i2 > 0 && d4 <= d2) {
                    j4 = j3;
                }
                j3++;
                i++;
                d2 = d2;
                d3 = d4;
                d4 += ((double) j6) / ((double) track2.getTrackMetaData().getTimescale());
            }
            build.addTrack(new AppendTrack(new CroppedTrack(track2, j5, j4)));
            d2 = d2;
        }
        file2.getParentFile().mkdirs();
        if (!file2.exists()) {
            file2.createNewFile();
        }
        Container build2 = new DefaultMp4Builder().build(build);
        FileOutputStream fileOutputStream = new FileOutputStream(file2);
        FileChannel channel = fileOutputStream.getChannel();
        build2.writeContainer(channel);
        channel.close();
        fileOutputStream.close();
        if (onTrimVideoListener != null) {
            onTrimVideoListener.getResult(Uri.parse(file2.toString()));
        }
    }

    private static double correctTimeToSyncSample(@NonNull Track track, double d, boolean z) {
        double[] dArr = new double[track.getSyncSamples().length];
        int i = 0;
        double d2 = 0.0d;
        double d3 = 0.0d;
        long j = 0;
        for (int i2 = 0; i2 < track.getSampleDurations().length; i2++) {
            long j2 = track.getSampleDurations()[i2];
            j++;
            if (Arrays.binarySearch(track.getSyncSamples(), j) >= 0) {
                dArr[Arrays.binarySearch(track.getSyncSamples(), j)] = d3;
            }
            d3 += ((double) j2) / ((double) track.getTrackMetaData().getTimescale());
        }
        int length = dArr.length;
        while (i < length) {
            double d4 = dArr[i];
            if (d4 > d) {
                return z ? d4 : d2;
            }
            i++;
            d2 = d4;
        }
        return dArr[dArr.length - 1];
    }

    public static String stringForTime(int i) {
        int i2 = i / 1000;
        int i3 = i2 % 60;
        int i4 = (i2 / 60) % 60;
        int i5 = i2 / 3600;
        Formatter formatter = new Formatter();
        if (i5 > 0) {
            return formatter.format("%d:%02d:%02d", Integer.valueOf(i5), Integer.valueOf(i4), Integer.valueOf(i3)).toString();
        }
        return formatter.format("%02d:%02d", Integer.valueOf(i4), Integer.valueOf(i3)).toString();
    }
}
