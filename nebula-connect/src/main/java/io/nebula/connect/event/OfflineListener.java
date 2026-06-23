package io.nebula.connect.event;

import io.nebula.common.event.EventListener;
import io.nebula.common.event.EventTag;
import io.nebula.common.event.Listener;
import io.nebula.connect.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Listener(eventTag = EventTag.ENGINE_OFFLINE)
public class OfflineListener implements EventListener {
    private static final Logger log = LoggerFactory.getLogger(OfflineListener.class);

    @Override
    public Object notify(Object... objs) {
        WebClient client = (WebClient) objs[0];
        log.info("User {} is offline", client.getUserId());
        // 业务逻辑: 清理缓存、通知游戏服等
        return null;
    }
}
