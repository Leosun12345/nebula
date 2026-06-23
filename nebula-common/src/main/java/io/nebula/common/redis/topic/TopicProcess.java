package io.nebula.common.redis.topic;

import io.nebula.common.worker.QueueWork;
import io.nebula.common.worker.WorkQueue;

public abstract class TopicProcess extends QueueWork {
    private String message;

    @Override
    public void init(Object... objs) {
        this.message = (String) objs[0];
    }

    @Override
    public WorkQueue getWorkQueue() {
        return () -> channelType().getQueueId();
    }

    @Override
    public void run() {
        try {
            process(message);
        } catch (Exception e) {
            exceptionCallBack(e);
        }
    }

    public abstract String orderType();
    public abstract void process(String message) throws Exception;
    public abstract TopicChannelType channelType();
}
