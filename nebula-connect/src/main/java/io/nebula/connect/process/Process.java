package io.nebula.connect.process;

import io.nebula.common.worker.AsynchronousWork;
import io.nebula.connect.client.WebClient;
import io.nebula.connect.model.MessageIn;

public abstract class Process<T> extends AsynchronousWork {
    protected MessageIn message;
    protected WebClient client;

    @Override
    public void init(Object... objs) {
        this.message = (MessageIn) objs[0];
        this.client = (WebClient) objs[1];
    }

    public abstract void process(WebClient client, T request, String code, String subCode, int sequence);
}
