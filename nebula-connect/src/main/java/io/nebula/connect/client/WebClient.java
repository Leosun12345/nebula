package io.nebula.connect.client;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.nebula.connect.manager.CacheOnline;
import io.nebula.connect.model.MessageOut;
import io.nebula.common.exception.HttpErrorCodeEnum;
import jakarta.websocket.Session;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@Data
@Builder
public class WebClient {
    private static final Logger log = LoggerFactory.getLogger(WebClient.class);

    private final Map<String, Object> attributes = new HashMap<>();
    private Session session;
    private String userId;
    private long lastHeartbeatTime;

    // ==================== nativeSend ====================

    public void nativeSend(String code, String subCode, int sequence, JSONObject response) {
        nativeSend(null, code, subCode, sequence, response);
    }

    public void nativeSend(Callable<?> callable, String code, String subCode,
                           int sequence, JSONObject response) {
        if (!isAlive()) {
            log.debug("WebClient is not alive, skip send to {}", userId);
            return;
        }

        MessageOut out = MessageOut.builder()
            .code(code)
            .subCode(subCode)
            .sequence(sequence)
            .response(response)
            .build();

        String msg = JSONObject.toJSONString(out, SerializerFeature.DisableCircularReferenceDetect);

        if (!"HEART_BEAT".equals(code)) {
            log.debug("Send to {}: {}", userId, msg);
        }

        try {
            session.getBasicRemote().sendText(msg);
            log.info("✅ Message sent to userId={}, code={}, subCode={}", userId, code, subCode);
        } catch (IOException e) {
            log.error("Failed to send message to {}: {}", userId, e.getMessage(), e);
            try {
                activeClose();
            } catch (Exception ex) {
                log.error("Failed to close session after send error", ex);
            }
            return;
        }

        if (callable != null) {
            try {
                callable.call();
            } catch (Exception e) {
                log.error("Callback execution failed for {}: {}", userId, e.getMessage(), e);
            }
        }
    }

    public void nativeSendAndClose(String code, String subCode, int sequence, JSONObject response) {
        nativeSend(() -> { activeClose(); return null; }, code, subCode, sequence, response);
    }

    public void nativeSendError(HttpErrorCodeEnum errorCode) {
        JSONObject error = new JSONObject();
        error.put("errorCode", errorCode.getCode());
        error.put("msg", errorCode.getMessage());
        nativeSend("BUSINESS", "NOTIFY_ERROR", 0, error);
    }

    // ==================== 辅助方法 ====================

    public boolean isAlive() {
        return session != null && session.isOpen();
    }

    public boolean isSameClient(Session session) {
        return this.session != null && this.session.getId().equals(session.getId());
    }

    public void activeClose() throws Exception {
        if (isAlive()) {
            session.close();
        }
        CacheOnline.WebClientManager.getInstance().offLine(this);
    }

    public void updateHeartbeat() {
        this.lastHeartbeatTime = System.currentTimeMillis();
    }

    public boolean isHeartbeatTimeout(long timeoutMillis) {
        return System.currentTimeMillis() - lastHeartbeatTime > timeoutMillis;
    }
}
