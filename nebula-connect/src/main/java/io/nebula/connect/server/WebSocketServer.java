package io.nebula.connect.server;

import io.nebula.common.event.EventTag;
import io.nebula.common.event.EventUtil;
import io.nebula.connect.WebSocketContext;
import io.nebula.connect.manager.CacheOnline;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.nebula.connect.client.WebClient;

@ServerEndpoint("/ws/{token}")
public class WebSocketServer {
    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        // 简易鉴权 - 实际应通过EventUtil调用业务层
        log.info("WebSocket connected: session={}, token={}", session.getId(), token);

        // 创建WebClient
        WebClient client = WebClient.builder()
            .session(session)
            .userId("temp_user_" + session.getId())
            .build();

        // 上线
        CacheOnline.WebClientManager.getInstance().online(client);

        // 触发上线事件
        EventUtil eventUtil = WebSocketContext.getInstance(EventUtil.class);
        eventUtil.throwEvent(EventTag.ENGINE_ONLINE, client);
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        log.info("Received message: {}", message);
        // 消息路由由ProcessManager处理
    }

    @OnClose
    public void onClose(Session session) throws Exception {
        log.info("WebSocket closed: session={}", session.getId());
        WebClient client = CacheOnline.WebClientManager.getInstance().getWebClientBySession(session);
        if (client != null) {
            client.activeClose();
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket error: session={}", session.getId(), error);
    }
}
