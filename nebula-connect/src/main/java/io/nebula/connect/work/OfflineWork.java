package io.nebula.connect.work;

import io.nebula.common.event.EventTag;
import io.nebula.common.event.EventUtil;
import io.nebula.common.worker.QueueWork;
import io.nebula.common.worker.WorkQueue;
import io.nebula.connect.WebSocketContext;
import io.nebula.connect.client.WebClient;

public class OfflineWork extends QueueWork {
    private WebClient client;

    @Override
    public void init(Object... objs) {
        this.client = (WebClient) objs[0];
    }

    @Override
    public void run() {
        EventUtil eventUtil = WebSocketContext.getInstance(EventUtil.class);
        Boolean ok = (Boolean) eventUtil.throwEvent(EventTag.ENGINE_OFFLINE_BEFORE, client);
        if (ok != null && ok) {
            eventUtil.throwEvent(EventTag.ENGINE_OFFLINE, client);
        }
    }

    @Override
    public WorkQueue getWorkQueue() {
        return () -> 10001L;
    }
}
