package io.nebula.common.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkManager {
    private static final Logger log = LoggerFactory.getLogger(WorkManager.class);
    private static final WorkManager INSTANCE = new WorkManager();

    public static final int WORKER_TASK_ALERT_COUNT = 512;
    public static final int WORKER_TASK_ALERT_TIME_COST = 5000;

    private final Map<Long, Worker> workers = new ConcurrentHashMap<>();
    private volatile boolean isShuttingDown = false;
    private ThreadPoolExecutor queueWorkThreadPool;
    private ThreadPoolExecutor threadPoolExecutor;

    private WorkManager() {}

    public static WorkManager getInstance() {
        return INSTANCE;
    }

    public void start(ThreadPoolExecutor queueWorkThreadPool, ThreadPoolExecutor threadPoolExecutor) {
        this.queueWorkThreadPool = queueWorkThreadPool;
        this.threadPoolExecutor = threadPoolExecutor;
        log.info("WorkManager started with custom thread pools");
    }

    public void start() {
        this.queueWorkThreadPool = new ThreadPoolExecutor(
            8, 16, 600, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1024),
            r -> { Thread t = new Thread(r, "nebula-queue-worker"); t.setDaemon(true); return t; },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        this.threadPoolExecutor = new ThreadPoolExecutor(
            8, 256, 30, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            r -> { Thread t = new Thread(r, "nebula-ayn-worker"); t.setDaemon(true); return t; },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        log.info("WorkManager started with default thread pools");
    }

    public void submit(Runnable runnable) {
        if (isShuttingDown) {
            log.warn("WorkManager is shutting down, reject task");
            return;
        }
        if (runnable instanceof Work) {
            submit((Work) runnable);
        } else {
            threadPoolExecutor.execute(runnable);
        }
    }

    public void submit(Work work, Object... objs) {
        work.init(objs);
        submit(work);
    }

    public void submit(Work work) {
        if (isShuttingDown) {
            log.warn("WorkManager is shutting down, reject task");
            return;
        }

        if (work instanceof QueueWork) {
            QueueWork queueWork = (QueueWork) work;
            long queueId = queueWork.getWorkQueue().getId();
            Worker worker = workers.computeIfAbsent(queueId, Worker::new);
            worker.addQueueWork(queueWork);
        } else if (work instanceof AsynchronousWork) {
            threadPoolExecutor.execute(work);
        } else {
            throw new RuntimeException("Unknown work type: " + work.getClass().getName());
        }
    }

    public void shutdown() {
        if (isShuttingDown) return;
        log.info("WorkManager shutting down...");
        isShuttingDown = true;

        workers.values().forEach(w -> w.isStop = true);

        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 30000) {
            int total = workers.values().stream().mapToInt(Worker::getQueueSize).sum();
            if (total <= 0) break;
            try { Thread.sleep(1000); } catch (InterruptedException e) { break; }
        }

        shutdownExecutor(threadPoolExecutor, "threadPoolExecutor");
        shutdownExecutor(queueWorkThreadPool, "queueWorkThreadPool");
        workers.clear();
        log.info("WorkManager shutdown completed");
    }

    private void shutdownExecutor(ThreadPoolExecutor executor, String name) {
        if (executor == null) return;
        try {
            executor.shutdown();
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private class Worker {
        private final long queueId;
        private final Queue<QueueWork> queueWorks = new ConcurrentLinkedQueue<>();
        private final AtomicInteger queueSizeCounter = new AtomicInteger(0);
        private final AtomicInteger state = new AtomicInteger(0);
        private volatile boolean isStop = false;

        Worker(long queueId) { this.queueId = queueId; }

        void addQueueWork(QueueWork work) {
            if (isStop) return;
            queueWorks.offer(work);
            queueSizeCounter.incrementAndGet();

            if (state.get() == 0 && state.compareAndSet(0, 1)) {
                processSendTasks();
            }
        }

        private void processSendTasks() {
            QueueWork task = queueWorks.poll();
            if (task == null) {
                if (state.compareAndSet(1, 0)) {
                    if (!queueWorks.isEmpty() && state.compareAndSet(0, 1)) {
                        processSendTasks();
                    }
                }
                return;
            }

            queueSizeCounter.decrementAndGet();

            CompletableFuture
                .runAsync(() -> {
                    try {
                        int size = getQueueSize();
                        if (size >= WORKER_TASK_ALERT_COUNT && size % WORKER_TASK_ALERT_COUNT == 0) {
                            log.warn("队列[{}]任务堆积: {}", queueId, size);
                        }
                        task.run();
                    } catch (Exception e) {
                        task.exceptionCallBack(e);
                    }
                }, queueWorkThreadPool)
                .whenComplete((r, t) -> processSendTasks());
        }

        int getQueueSize() {
            return queueSizeCounter.get();
        }
    }
}
