package io.nebula.common.redis.topic;

import io.nebula.common.scanner.DynamicFind;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TopicManager extends DynamicFind {
    private static final TopicManager INSTANCE = new TopicManager();
    private final Map<String, Class<?>> processMap = new HashMap<>();

    public static TopicManager getInstance() {
        return INSTANCE;
    }

    @Override
    public <T> void findClass(Class<T> clz) throws Exception {
        TopicProcess process = (TopicProcess) clz.getDeclaredConstructor().newInstance();
        String key = process.orderType() + "-" + process.channelType().getChannel();
        processMap.put(key, clz);
    }

    @Override
    public boolean verification(Class<?> clazz) {
        return superClassOn(clazz, TopicProcess.class);
    }

    @Override
    public void beforeFind(Set<Class<?>> clazzs) {}

    @Override
    public void afterFind(Set<Class<?>> clazzs) {}

    public TopicProcess getProcess(String orderType, String channel) {
        String key = orderType + "-" + channel;
        Class<?> clz = processMap.get(key);
        if (clz == null) return null;
        try {
            return (TopicProcess) clz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }
}
