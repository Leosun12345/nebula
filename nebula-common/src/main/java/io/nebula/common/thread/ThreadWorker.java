package io.nebula.common.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadWorker {
    public static ExecutorService batchExecutor;
    public static ExecutorService syncExecutor;

    public static void init() {
        batchExecutor = Executors.newFixedThreadPool(50,
            r -> { Thread t = new Thread(r, "nebula-batch"); t.setDaemon(true); return t; });
        syncExecutor = Executors.newFixedThreadPool(30,
            r -> { Thread t = new Thread(r, "nebula-sync"); t.setDaemon(true); return t; });
    }

    public static void shutdown() {
        if (batchExecutor != null) { batchExecutor.shutdown(); }
        if (syncExecutor != null) { syncExecutor.shutdown(); }
    }
}
