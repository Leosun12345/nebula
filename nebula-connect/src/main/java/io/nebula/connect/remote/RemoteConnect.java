package io.nebula.connect.remote;

import com.alibaba.fastjson.JSONObject;
import io.nebula.connect.client.WebClient;
import io.nebula.connect.manager.CacheOnline;
import io.nebula.connect.util.IoSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RemoteConnect {
    private static final Logger log = LoggerFactory.getLogger(RemoteConnect.class);

    @PostMapping("/api/g2c/send")
    public String sendMessage(@RequestBody SendRequest request) {
        log.info("G2C send: users={}, subCode={}", request.userIds, request.subCode);

        JSONObject response = JSONObject.parseObject(request.data);
        IoSender.sendMessage(request.code, request.subCode, request.sequence, response,
            request.userIds.toArray(new String[0]));
        return "OK";
    }

    @PostMapping("/api/g2c/sendAndClose")
    public String sendAndClose(@RequestBody SendRequest request) {
        sendMessage(request);
        for (String userId : request.userIds) {
            WebClient client = CacheOnline.WebClientManager.getInstance().getWebClientByUid(userId);
            if (client != null && client.isAlive()) {
                try {
                    client.activeClose();
                } catch (Exception e) {
                    log.error("Close error", e);
                }
            }
        }
        return "OK";
    }

    public static class SendRequest {
        public String code;
        public String subCode;
        public int sequence;
        public String data;
        public List<String> userIds;
    }
}
