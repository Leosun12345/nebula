package io.nebula.connect.manager;

import io.nebula.common.worker.WorkManager;
import io.nebula.connect.client.WebClient;
import io.nebula.connect.work.OfflineWork;
import io.nebula.connect.work.OnlineWork;
import jakarta.websocket.Session;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;

public class CacheOnline {

    @Getter
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

        public void online(WebClient client){
            clients.put(client.getUserId(), client);
            WorkManager.getInstance().submit(new OnlineWork(), client);
        }

        public WebClient offLine(WebClient client) {
            WebClient removed = clients.remove(client.getUserId());
            if (removed != null) {
                WorkManager.getInstance().submit(new OfflineWork(), removed);
            }
            return removed;
        }

        public int onlineTotal() {
            return clients.size();
        }

    }
}
