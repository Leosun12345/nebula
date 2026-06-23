package io.nebula.connect.event;

import io.nebula.common.event.EventListener;
import io.nebula.common.event.EventTag;
import io.nebula.common.event.Listener;
import io.nebula.connect.client.WebClient;

@Listener(eventTag = EventTag.ENGINE_OFFLINE_BEFORE)
public class OfflineBeforeListener implements EventListener<Boolean> {

    @Override
    public Boolean notify(Object... objs) {
        WebClient client = (WebClient) objs[0];
        // 下线前检查
        return true;
    }
}
