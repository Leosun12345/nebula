package io.nebula.connect.manager;

import io.nebula.common.worker.WorkManager;
import io.nebula.connect.client.WebClient;
import io.nebula.connect.work.OnlineWork;
import io.nebula.connect.work.OfflineWork;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class CacheOnline {
    private static final Logger log = LoggerFactory.getLogger(CacheOnline.class);

    public static class WebClientManager {
        private static final WebClientManager INSTANCE = new WebClientManager();
        private final ConcurrentHashMap<String, WebClient> clients = new ConcurrentHashMap<>();

        public static WebClientManager getInstance() {
            return INSTANCE;
        }

        public WebClient getWebClientByUid(String userId) {
            return clients.get(userId);
        }

        public WebClient getWebClientBySession(Session session) {
            for (WebClient client : clients.values()) {
                if (client.isSameClient(session)) {
                    return client;
                }
            }
            return null;
        }

        public void online(WebClient client) throws Exception {
            WebClient old = clients.put(client.getUserId(), client);
            if (old != null && !old.isSameClient(client.getSession())) {
                log.warn("User {} reconnected, closing old session", client.getUserId());
                old.activeClose();
            }
            WorkManager.getInstance().submit(new OnlineWork(), client);
            log.info("User {} online, total: {}", client.getUserId(), clients.size());
        }

        public WebClient offLine(WebClient client) {
            WebClient removed = clients.remove(client.getUserId());
            if (removed != null) {
                WorkManager.getInstance().submit(new OfflineWork(), removed);
                log.info("User {} offline, total: {}", client.getUserId(), clients.size());
            }
            return removed;
        }

        public int onlineTotal() {
            return clients.size();
        }

        public ConcurrentHashMap<String, WebClient> getClients() {
            return clients;
        }
    }
}
