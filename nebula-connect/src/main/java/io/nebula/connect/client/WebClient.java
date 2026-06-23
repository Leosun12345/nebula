package io.nebula.connect.client;

import io.nebula.connect.manager.CacheOnline;
import jakarta.websocket.Session;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class WebClient {
    private final Map<String, Object> attributes = new HashMap<>();
    private Session session;
    private String userId;

    public boolean isAlive() {
        return session != null && session.isOpen();
    }

    public boolean isSameClient(Session session) {
        return this.session != null && this.session.getId().equals(session.getId());
    }

    public void activeClose() throws Exception {
        CacheOnline.WebClientManager.getInstance().offLine(this);
        if (isAlive()) {
            session.close();
        }
    }
}
