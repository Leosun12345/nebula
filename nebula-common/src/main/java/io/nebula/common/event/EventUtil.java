package io.nebula.common.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EventUtil {
    private static final Logger log = LoggerFactory.getLogger(EventUtil.class);

    public Object throwEvent(String event, Object... objs) {
        EventListener<?> listener = EventLoader.getEventListener(event);
        if (listener == null) {
            return null;
        }
        try {
            return listener.notify(objs);
        } catch (Exception e) {
            log.error("Event [{}] execution failed", event, e);
            return null;
        }
    }
}
