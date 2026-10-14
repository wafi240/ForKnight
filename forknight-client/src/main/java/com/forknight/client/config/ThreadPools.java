package com.forknight.client.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPools {
    
    public static final ExecutorService IO_POOL = Executors.newFixedThreadPool(4, new ThreadFactory() {
        private final AtomicInteger counter = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "fk-io-" + counter.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    });

    public static final ExecutorService COMPUTE_POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
        private final AtomicInteger counter = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "fk-compute-" + counter.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    });

    public static void init() {
        // Empty initialization trigger
    }

    public static void shutdown() {
        IO_POOL.shutdownNow();
        COMPUTE_POOL.shutdownNow();
    }
}
