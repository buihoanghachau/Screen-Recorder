package com.testlubu.screenrecorder.videoTrimmer.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import java.util.HashMap;
import java.util.Map;

public final class UiThreadExecutor {
    private static final Handler HANDLER = new Handler(Looper.getMainLooper()) {
        /* class com.testlubu.screenrecorder.videoTrimmer.utils.UiThreadExecutor.AnonymousClass1 */

        public void handleMessage(Message message) {
            Runnable callback = message.getCallback();
            if (callback != null) {
                callback.run();
                UiThreadExecutor.decrementToken((Token) message.obj);
                return;
            }
            super.handleMessage(message);
        }
    };
    private static final Map<String, Token> TOKENS = new HashMap();

    private UiThreadExecutor() {
    }

    public static void runTask(String str, Runnable runnable, long j) {
        if ("".equals(str)) {
            HANDLER.postDelayed(runnable, j);
            return;
        }
        HANDLER.postAtTime(runnable, nextToken(str), SystemClock.uptimeMillis() + j);
    }

    private static Token nextToken(String str) {
        Token token;
        synchronized (TOKENS) {
            token = TOKENS.get(str);
            if (token == null) {
                token = new Token(str);
                TOKENS.put(str, token);
            }
            token.runnablesCount++;
        }
        return token;
    }

    /* access modifiers changed from: private */
    public static void decrementToken(Token token) {
        String str;
        Token remove;
        synchronized (TOKENS) {
            int i = token.runnablesCount - 1;
            token.runnablesCount = i;
            if (i == 0 && (remove = TOKENS.remove((str = token.id))) != token) {
                TOKENS.put(str, remove);
            }
        }
    }

    public static void cancelAll(String str) {
        Token remove;
        synchronized (TOKENS) {
            remove = TOKENS.remove(str);
        }
        if (remove != null) {
            HANDLER.removeCallbacksAndMessages(remove);
        }
    }

    /* access modifiers changed from: private */
    public static final class Token {
        final String id;
        int runnablesCount;

        private Token(String str) {
            this.runnablesCount = 0;
            this.id = str;
        }
    }
}
