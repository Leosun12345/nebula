package io.nebula.common.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class QueueWork implements Work, Runnable {
    private static final Logger log = LoggerFactory.getLogger(QueueWork.class);

    public abstract WorkQueue getWorkQueue();

    public String queueClass() {
        return getClass().getSimpleName();
    }

    public void exceptionCallBack(Throwable e) {
        log.error("{} QueueWork error", getClass().getSimpleName(), e);
    }
}
