package io.nebula.connect.event;

import io.nebula.common.event.EventListener;
import io.nebula.common.event.EventTag;
import io.nebula.common.event.Listener;
import io.nebula.connect.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Listener(eventTag = EventTag.ENGINE_ONLINE)
public class OnlineListener implements EventListener {
    private static final Logger log = LoggerFactory.getLogger(OnlineListener.class);

    @Override
    public Object notify(Object... objs) {
        WebClient client = (WebClient) objs[0];
        log.info("User {} is online", client.getUserId());
        // 业务逻辑: 更新缓存、通知好友等
        return null;
    }
}
