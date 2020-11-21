package com.testlubu.screenrecorder.videoTrimmer.utils;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class BackgroundExecutor {
    private static final ThreadLocal<String> CURRENT_SERIAL = new ThreadLocal<>();
    public static final Executor DEFAULT_EXECUTOR = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private static final String TAG = "BackgroundExecutor";
    private static final List<Task> TASKS = new ArrayList();
    private static Executor executor = DEFAULT_EXECUTOR;

    private BackgroundExecutor() {
    }

    private static Future<?> directExecute(Runnable runnable, long j) {
        if (j > 0) {
            Executor executor2 = executor;
            if (executor2 instanceof ScheduledExecutorService) {
                return ((ScheduledExecutorService) executor2).schedule(runnable, j, TimeUnit.MILLISECONDS);
            }
            throw new IllegalArgumentException("The executor set does not support scheduling");
        }
        Executor executor3 = executor;
        if (executor3 instanceof ExecutorService) {
            return ((ExecutorService) executor3).submit(runnable);
        }
        executor3.execute(runnable);
        return null;
    }

    public static synchronized void execute(Task task) {
        synchronized (BackgroundExecutor.class) {
            Future<?> future = null;
            if (task.serial == null || !hasSerialRunning(task.serial)) {
                task.executionAsked = true;
                future = directExecute(task, task.remainingDelay);
            }
            if (!(task.id == null && task.serial == null) && !task.managed.get()) {
                task.future = future;
                TASKS.add(task);
            }
        }
    }

    private static boolean hasSerialRunning(String str) {
        for (Task task : TASKS) {
            if (task.executionAsked && str.equals(task.serial)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public static Task take(String str) {
        int size = TASKS.size();
        for (int i = 0; i < size; i++) {
            if (str.equals(TASKS.get(i).serial)) {
                return TASKS.remove(i);
            }
        }
        return null;
    }

    public static synchronized void cancelAll(String str, boolean z) {
        synchronized (BackgroundExecutor.class) {
            for (int size = TASKS.size() - 1; size >= 0; size--) {
                Task task = TASKS.get(size);
                if (str.equals(task.id)) {
                    if (task.future != null) {
                        task.future.cancel(z);
                        if (!task.managed.getAndSet(true)) {
                            task.postExecute();
                        }
                    } else if (task.executionAsked) {
                        Log.w(TAG, "A task with id " + task.id + " cannot be cancelled (the executor set does not support it)");
                    } else {
                        TASKS.remove(size);
                    }
                }
            }
        }
    }

    public static abstract class Task implements Runnable {
        private boolean executionAsked;
        private Future<?> future;
        private String id;
        private AtomicBoolean managed = new AtomicBoolean();
        private long remainingDelay;
        private String serial;
        private long targetTimeMillis;

        public abstract void execute();

        public Task(String str, long j, String str2) {
            if (!"".equals(str)) {
                this.id = str;
            }
            if (j > 0) {
                this.remainingDelay = j;
                this.targetTimeMillis = System.currentTimeMillis() + j;
            }
            if (!"".equals(str2)) {
                this.serial = str2;
            }
        }

        public void run() {
            if (!this.managed.getAndSet(true)) {
                try {
                    BackgroundExecutor.CURRENT_SERIAL.set(this.serial);
                    execute();
                } finally {
                    postExecute();
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void postExecute() {
            Task take;
            if (this.id != null || this.serial != null) {
                BackgroundExecutor.CURRENT_SERIAL.set(null);
                synchronized (BackgroundExecutor.class) {
                    BackgroundExecutor.TASKS.remove(this);
                    if (!(this.serial == null || (take = BackgroundExecutor.take(this.serial)) == null)) {
                        if (take.remainingDelay != 0) {
                            take.remainingDelay = Math.max(0L, this.targetTimeMillis - System.currentTimeMillis());
                        }
                        BackgroundExecutor.execute(take);
                    }
                }
            }
        }
    }
}
