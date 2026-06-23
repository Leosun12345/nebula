package io.nebula.connect.process;

import io.nebula.common.worker.QueueWork;
import io.nebula.common.worker.WorkQueue;
import io.nebula.connect.client.WebClient;
import io.nebula.connect.model.MessageIn;

public abstract class QueueProcess<T> extends QueueWork {
    protected MessageIn message;
    protected WebClient client;

    @Override
    public void init(Object... objs) {
        this.message = (MessageIn) objs[0];
        this.client = (WebClient) objs[1];
    }

    @Override
    public WorkQueue getWorkQueue() {
        return () -> client.getUserId().hashCode() / 32;
    }

    public abstract void process(WebClient client, T request, String code, String subCode, int sequence);

    @Override
    public void run() {
        // 简化版：直接处理，实际应包含异常处理和泛型解析
        try {
            process(client, (T) message.getRequest(), message.getCode(), message.getSubCode(), message.getSequence());
        } catch (Exception e) {
            exceptionCallBack(e);
        }
    }
}
