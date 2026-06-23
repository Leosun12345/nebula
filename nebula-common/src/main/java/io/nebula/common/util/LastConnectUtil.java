package io.nebula.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LastConnectUtil {
    private static final Logger log = LoggerFactory.getLogger(LastConnectUtil.class);
    private static final LastConnectUtil INSTANCE = new LastConnectUtil();

    private final ConcurrentHashMap<String, LastConnect> cache = new ConcurrentHashMap<>();

    public static LastConnectUtil getInstance() {
        return INSTANCE;
    }

    public void save(LastConnect connect) {
        cache.put(connect.getUid(), connect);
        log.debug("Saved LastConnect: uid={}, host={}, port={}",
            connect.getUid(), connect.getHost(), connect.getPort());
    }

    public LastConnect getLastConnect(String uid) {
        return cache.get(uid);
    }

    public List<LastConnect> getLastConnectBatch(String... uids) {
        List<LastConnect> result = new ArrayList<>();
        for (String uid : uids) {
            LastConnect c = cache.get(uid);
            if (c != null) {
                result.add(c);
            }
        }
        return result;
    }

    public void delete(String uid) {
        cache.remove(uid);
    }
}
