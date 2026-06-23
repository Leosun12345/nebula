package io.nebula.logic.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.nebula.common.util.LastConnect;
import io.nebula.common.util.LastConnectUtil;
import io.nebula.logic.core.LogicContext;
import io.nebula.logic.core.room.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 游戏层消息推送工具
 * 按用户分组 → 查询连接信息 → 投递到对应 Connect 节点
 *
 * @author leo
 * @since 1.0.0
 */
public class IoSender {
    private static final Logger log = LoggerFactory.getLogger(IoSender.class);
    private static final LastConnectUtil lastConnectUtil;

    static {
        lastConnectUtil = LogicContext.getInstance(LastConnectUtil.class);
    }

    /**
     * 向房间内的用户推送消息 (排除指定用户)
     */
    public static void broadMessageWithout(Room room, String subCode, int sequence,
                                           JSONObject response, String... excludes) {
        List<String> userIds = new ArrayList<>();
        // 从房间获取所有玩家
        // ... 实际业务中从 room.getPlayers() 获取
        userIds.removeAll(Arrays.asList(excludes));
        sendMessage(room, subCode, sequence, response, userIds.toArray(new String[0]));
    }

    /**
     * 向指定用户推送消息
     */
    public static void sendMessage(Room room, String subCode, int sequence,
                                   JSONObject response, String... userIds) {
        if (userIds == null || userIds.length == 0) {
            return;
        }

        // 过滤在线用户
        List<String> onlineUsers = new ArrayList<>();
        for (String uid : userIds) {
            if (room != null && room.getOnlineList().contains(uid)) {
                onlineUsers.add(uid);
            }
        }
        if (onlineUsers.isEmpty()) {
            return;
        }

        String[] uids = onlineUsers.toArray(new String[0]);

        // 查询连接信息
        List<LastConnect> connects = lastConnectUtil.getLastConnectBatch(uids);
        if (connects.isEmpty()) {
            return;
        }

        // 按节点分组
        Map<String, List<LastConnect>> grouped = groupByNode(connects);
        String data = JSONObject.toJSONString(response, SerializerFeature.DisableCircularReferenceDetect);

        for (Map.Entry<String, List<LastConnect>> entry : grouped.entrySet()) {
            String[] parts = entry.getKey().split("#");
            String host = parts[0];
            String port = parts[1];
            List<LastConnect> group = entry.getValue();

            // 异步投递到 Connect 节点
            CompletableFuture.runAsync(() -> {
                try {
                    // 远程调用 Connect 的 HTTP 接口
                    // 或投递到 Redis 队列 G2C_{host}_{port}
                    deliverToConnect(host, port, room, subCode, sequence, data, group);
                } catch (Exception e) {
                    log.error("deliver message to connect error", e);
                }
            });
        }
    }

    /**
     * 向指定用户推送消息并关闭连接
     */
    public static void sendMessageAndClose(Room room, String subCode, int sequence,
                                           JSONObject response, String... userIds) {
        // 先推送，再关闭
        sendMessage(room, subCode, sequence, response, userIds);
        // 实际业务中：逐个关闭 WebClient
    }

    /**
     * 按 host#port 分组
     */
    private static Map<String, List<LastConnect>> groupByNode(List<LastConnect> connects) {
        Map<String, List<LastConnect>> result = new HashMap<>();
        for (LastConnect c : connects) {
            String key = c.getHost() + "#" + c.getPort();
            result.computeIfAbsent(key, k -> new ArrayList<>()).add(c);
        }
        return result;
    }

    /**
     * 投递消息到 Connect 节点
     */
    private static void deliverToConnect(String host, String port, Room room,
                                         String subCode, int sequence, String data,
                                         List<LastConnect> connects) {
        // TODO: 实际实现
        // 方式1: HTTP 调用 Connect 的远程接口
        // 方式2: 投递到 Redis 队列 G2C_{host}_{port}
        log.info("Deliver to {}:{} , room={}, subCode={}, users={}",
            host, port, room.getRoomId(), subCode, connects.size());
    }
}
