package io.nebula.logic.remote;

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

/**
 * C2G 消息处理器 (阻塞消费)
 * 从 Redis 队列中拉取 Connect 层投递的消息
 *
 * @author leo
 * @since 1.0.0
 */
@Component
public class C2gMessageHandlerBlocking implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(C2gMessageHandlerBlocking.class);
    private static final long BLOCK_TIMEOUT = 30;
    private static final int BATCH_SIZE = 1000;

    @Value("${server.address:127.0.0.1}")
    private String host;
    @Value("${server.port:8080}")
    private String port;

    @Autowired
    private QueueUtil queueUtil;

    @Autowired
    private RemoteLogic remoteLogic;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String queueName = "C2G_" + host + "_" + port;

        // 启动时清理旧队列 (可选)
        // queueUtil.deleteQueue(queueName);

        // 启动消费者线程
        new Thread(() -> {
            while (WorkManager.getInstance().isShuttingDown) {
                try {
                    List<String> messages = queueUtil.pullBlockingBatch(queueName, BATCH_SIZE, BLOCK_TIMEOUT, TimeUnit.SECONDS);
                    for (String message : messages) {
                        if (message != null) {
                            handleMessage(message);
                        }
                    }
                } catch (Exception e) {
                    log.error("C2G handler error", e);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            log.info("C2G message handler stopped");
        }, "C2G-Handler-" + port).start();

        log.info("C2G message handler started for queue: {}", queueName);
    }

    private void handleMessage(String json) {
        try {
            JSONObject obj = QueueUtil.unwrapMessage(json);
            if (obj == null) {
                log.warn("Invalid message format: {}", json);
                return;
            }

            String api = obj.getString("api");
            log.debug("C2G message: api={}", api);

            // 路由到对应的 RemoteLogic 方法
            switch (api) {
                case "C2G_DISPATCH_MSG":
                    RemoteLogic.DispatchRequest dispatchReq = obj.toJavaObject(RemoteLogic.DispatchRequest.class);
                    remoteLogic.dispatchMsg(dispatchReq);
                    break;

                case "C2G_ROOM_CREATE":
                    RemoteLogic.CreateRoomRequest createReq = obj.toJavaObject(RemoteLogic.CreateRoomRequest.class);
                    remoteLogic.roomCreate(createReq);
                    break;

                case "C2G_ROOM_JOIN":
                    RemoteLogic.JoinRoomRequest joinReq = obj.toJavaObject(RemoteLogic.JoinRoomRequest.class);
                    remoteLogic.roomJoin(joinReq);
                    break;

                case "C2G_ROOM_QUICK_JOIN":
                    RemoteLogic.QuickJoinRequest quickReq = obj.toJavaObject(RemoteLogic.QuickJoinRequest.class);
                    remoteLogic.roomQuickJoin(quickReq);
                    break;

                case "C2G_ROOM_EXISTS":
                    RemoteLogic.RoomExistsRequest existsReq = obj.toJavaObject(RemoteLogic.RoomExistsRequest.class);
                    remoteLogic.roomExists(existsReq);
                    break;

                case "C2G_ROOM_DETAIL":
                    RemoteLogic.RoomDetailRequest detailReq = obj.toJavaObject(RemoteLogic.RoomDetailRequest.class);
                    remoteLogic.roomDetail(detailReq);
                    break;

                default:
                    log.warn("Unknown C2G api: {}", api);
            }

        } catch (Exception e) {
            log.error("Handle C2G message error: {}", json, e);
        }
    }

    /**
     * 测试消息
     */
    public static class TestMessage {
        public String api;
        public String data;
        public long timestamp;
    }
}
