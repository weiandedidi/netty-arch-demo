package com.qidi.nettyme.demos.util;

/**
 * 带有traceId的子线程的实现类，子线程的traceId从主线程中获得
 *
 * @author maqidi
 * @version 1.0
 * @create 2024-07-01 10:04
 */
public class TraceRunnable implements Runnable {
    private final Runnable task;
    private final String traceId;

    public TraceRunnable(Runnable task, String traceId) {
        this.task = task;
        this.traceId = traceId;
    }

    @Override
    public void run() {
        try {
            TraceIdUtil.setTraceId(traceId);
            task.run();
        } finally {
            TraceIdUtil.clear();
        }
    }

}
