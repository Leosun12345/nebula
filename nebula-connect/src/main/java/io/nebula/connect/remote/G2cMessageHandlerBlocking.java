package io.nebula.connect.remote;

import com.alibaba.fastjson.JSONObject;
import io.nebula.common.redis.QueueUtil;
import io.nebula.common.worker.WorkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class G2cMessageHandlerBlocking implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(G2cMessageHandlerBlocking.class);
    private static final long BLOCK_TIMEOUT = 30;
    private static final int BATCH_SIZE = 1000;

    @Value("${server.address:127.0.0.1}")
    private String host;
    @Value("${server.port:8090}")
    private String port;

    @Autowired
    private RemoteConnect remoteConnect;
    @Autowired
    private QueueUtil queueUtil;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String queue = "G2C_" + host + "_" + port;

        new Thread(() -> {
            while (WorkManager.getInstance().isShuttingDown) {
                try {
                    List<String> messages = queueUtil.pullBlockingBatch(queue, BATCH_SIZE,BLOCK_TIMEOUT, TimeUnit.SECONDS);
                    for (String msg : messages) {
                        handleMessage(msg);
                    }
                } catch (Exception e) {
                    log.error("G2C handler error", e);
                }
            }
        }, "G2C-Handler").start();

        log.info("G2C message handler started for queue: {}", queue);
    }

    private void handleMessage(String json) {
        try {
            JSONObject obj = JSONObject.parseObject(json);
            String api = obj.getString("api");
            if ("G2C_SEND".equals(api)) {
                RemoteConnect.SendRequest request = JSONObject.toJavaObject(obj, RemoteConnect.SendRequest.class);
                remoteConnect.sendMessage(request);
            } else if ("G2C_SEND_AND_CLOSE".equals(api)) {
                RemoteConnect.SendRequest request = JSONObject.toJavaObject(obj, RemoteConnect.SendRequest.class);
                remoteConnect.sendAndClose(request);
            }
        } catch (Exception e) {
            log.error("Handle message error: {}", json, e);
        }
    }
}
