package com.qidi.nettyme.demos.util;

import java.util.concurrent.Callable;

/**
 * 带有traceId的子线程的实现类，子线程的traceId从主线程中获得带有traceId的子线程的实现类，子线程的traceId从主线程中获得
 * 返回结果的实现类线程
 *
 * @author maqidi
 * @version 1.0
 * @create 2024-07-01 10:08
 */
public class TraceCallable<V> implements Callable<V> {
    private final Callable<V> task;
    private final String traceId;

    public TraceCallable(Callable<V> task, String traceId) {
        this.task = task;
        this.traceId = traceId;
    }

    @Override
    public V call() throws Exception {
        try {
            TraceIdUtil.setTraceId(traceId);
            return task.call();
        } finally {
            TraceIdUtil.clear();
        }
    }
}
