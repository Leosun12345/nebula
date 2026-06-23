package io.nebula.connect.event;

import io.nebula.common.event.EventListener;
import io.nebula.common.event.EventTag;
import io.nebula.common.event.Listener;
import io.nebula.connect.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Listener(eventTag = EventTag.ENGINE_ONLINE_BEFORE)
public class OnlineBeforeListener implements EventListener<Boolean> {
    private static final Logger log = LoggerFactory.getLogger(OnlineBeforeListener.class);

    @Override
    public Boolean notify(Object... objs) {
        WebClient client = (WebClient) objs[0];
        // 业务检查: 是否允许上线
        // 例如: 检查用户是否被封禁
        log.info("Online before check for user: {}", client.getUserId());
        return true; // 返回false则阻止上线
    }
}
