package com.testlubu.screenrecorder.videoTrimmer.utils;

import java.text.DecimalFormat;

public class Toolbox {
    public static String converTime(String str) {
        DecimalFormat decimalFormat = new DecimalFormat("00");
        try {
            int parseFloat = (int) Float.parseFloat(str);
            if (parseFloat == 0) {
                return "00:00";
            }
            if (parseFloat < 60) {
                return "00:" + decimalFormat.format((long) parseFloat);
            }
            int i = parseFloat / 60;
            int i2 = parseFloat % 60;
            if (i < 60) {
                return decimalFormat.format((long) i) + ":" + decimalFormat.format((long) i2);
            }
            return decimalFormat.format((long) (i / 60)) + ":" + decimalFormat.format((long) (i % 60)) + ":" + decimalFormat.format((long) i2);
        } catch (Exception unused) {
            return "00:00";
        }
    }
}
