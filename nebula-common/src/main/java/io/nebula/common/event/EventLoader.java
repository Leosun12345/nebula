package io.nebula.common.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(0)
public class EventLoader implements ApplicationRunner, ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(EventLoader.class);
    private ApplicationContext context;
    private static final Map<String, EventListener<?>> EVENT_LISTENER_MAP = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        load();
    }

    public void load() {
        Map<String, Object> listeners = context.getBeansWithAnnotation(Listener.class);
        for (Object bean : listeners.values()) {
            Class<?> clz = bean.getClass();
            Listener annotation = clz.getAnnotation(Listener.class);
            String eventTag = annotation.eventTag();

            if (!isEventListener(clz)) {
                throw new RuntimeException(clz.getSimpleName() + " must implement EventListener");
            }

            EVENT_LISTENER_MAP.put(eventTag, (EventListener<?>) bean);
            log.info("Registered event listener: {} → {}", eventTag, clz.getSimpleName());
        }
        log.info("EventLoader loaded {} listeners", EVENT_LISTENER_MAP.size());
    }

    public static EventListener<?> getEventListener(String event) {
        return EVENT_LISTENER_MAP.get(event);
    }

    private boolean isEventListener(Class<?> clz) {
        for (Class<?> inter : clz.getInterfaces()) {
            if (inter.equals(EventListener.class)) return true;
        }
        return false;
    }
}
