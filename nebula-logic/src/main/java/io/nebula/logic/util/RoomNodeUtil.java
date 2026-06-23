package io.nebula.logic.util;

import com.alibaba.fastjson.JSONObject;
import io.nebula.common.redis.RedisUtil;
import io.nebula.logic.core.room.RoomNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RoomNodeUtil {
    private static final Logger log = LoggerFactory.getLogger(RoomNodeUtil.class);
    private static final String ROOM_NODE_PREFIX = "room:node:";
    @Autowired
    private RedisUtil redisUtil;

    // 当前节点信息 (所有房间共享)
    private String currentServiceId;
    private String currentHost;
    private String currentPort;

    // 本地房间ID集合 (当前节点承载的所有房间)
    private final Set<String> localRoomIds = new HashSet<>();

    private final Object lock = new Object();

    // ==================== 初始化 ====================

    public void init(String serviceId, String host, String port) {
        synchronized (lock) {
            this.currentServiceId = serviceId;
            this.currentHost = host;
            this.currentPort = port;
            log.info("RoomNodeUtil initialized: serviceId={}, host={}, port={}",
                serviceId, host, port);
        }
    }

    // ==================== 房间节点管理 ====================

    /**
     * 注册房间节点 (创建房间时调用)
     */
    public void registerRoom(String roomId) {
        synchronized (lock) {
            if (currentHost == null) {
                log.warn("RoomNodeUtil not initialized");
                return;
            }

            localRoomIds.add(roomId);

            RoomNode node = RoomNode.builder()
                .roomId(roomId)
                .serviceId(currentServiceId)
                .host(currentHost)
                .port(currentPort)
                .playerCount(0)
                .status(1)
                .lastHeartbeat(System.currentTimeMillis())
                .build();

            doSyncToRedis(roomId, node);
            log.info("Room node registered: roomId={}, node={}", roomId, node.getAddress());
        }
    }

    /**
     * 注销房间节点 (销毁房间时调用)
     */
    public void unregisterRoom(String roomId) {
        synchronized (lock) {
            localRoomIds.remove(roomId);
            redisUtil.delete(ROOM_NODE_PREFIX + roomId);
            log.info("Room node unregistered: roomId={}", roomId);
        }
    }

    /**
     * 更新房间状态 (关闭时状态变为2)
     */
    public void updateRoomStatus(String roomId, int status) {
        synchronized (lock) {
            if (!localRoomIds.contains(roomId)) return;
            RoomNode node = RoomNode.builder()
                .roomId(roomId)
                .serviceId(currentServiceId)
                .host(currentHost)
                .port(currentPort)
                .status(status)
                .lastHeartbeat(System.currentTimeMillis())
                .build();
            doSyncToRedis(roomId, node);
        }
    }

    /**
     * 更新房间人数
     */
    public void updatePlayerCount(String roomId, int count) {
        synchronized (lock) {
            if (localRoomIds.contains(roomId)) {
                RoomNode node = RoomNode.builder()
                    .roomId(roomId)
                    .serviceId(currentServiceId)
                    .host(currentHost)
                    .port(currentPort)
                    .playerCount(count)
                    .lastHeartbeat(System.currentTimeMillis())
                    .build();
                doSyncToRedis(roomId, node);
            }
        }
    }

    /**
     * 更新所有本地房间心跳
     */
    public void updateHeartbeat() {
        synchronized (lock) {
            long now = System.currentTimeMillis();
            for (String roomId : localRoomIds) {
                RoomNode node = RoomNode.builder()
                    .roomId(roomId)
                    .serviceId(currentServiceId)
                    .host(currentHost)
                    .port(currentPort)
                    .lastHeartbeat(now)
                    .build();
                doSyncToRedis(roomId, node);
            }
        }
    }


    // ==================== 查询 ====================

    /**
     * 判断当前节点是否承载某个房间
     */
    public boolean containsRoom(String roomId) {
        synchronized (lock) {
            return localRoomIds.contains(roomId);
        }
    }

    /**
     * 获取当前节点所有房间ID
     */
    public Set<String> getLocalRoomIds() {
        synchronized (lock) {
            return new HashSet<>(localRoomIds);
        }
    }

    /**
     * 根据房间ID获取节点信息 (跨节点路由)
     * 先查本地，再查Redis
     */
    public RoomNode getRoomNode(String roomId) {
        // 1. 先查本地
        synchronized (lock) {
            if (localRoomIds.contains(roomId)) {
                return RoomNode.builder()
                    .roomId(roomId)
                    .serviceId(currentServiceId)
                    .host(currentHost)
                    .port(currentPort)
                    .build();
            }
        }

        // 2. 从Redis查询
        String json = redisUtil.get(ROOM_NODE_PREFIX + roomId);
        if (json == null) {
            return null;
        }

        try {
            return JSONObject.parseObject(json, RoomNode.class);
        } catch (Exception e) {
            log.error("Failed to parse RoomNode: roomId={}", roomId, e);
            return null;
        }
    }

    /**
     * 获取所有房间节点 (Redis)
     */
    public List<RoomNode> getAllRoomNodes() {
        List<RoomNode> nodes = new ArrayList<>();
        Set<String> keys = redisUtil.keys(ROOM_NODE_PREFIX + "*");
        if (keys != null) {
            for (String key : keys) {
                String json = redisUtil.get(key);
                if (json != null) {
                    try {
                        nodes.add(JSONObject.parseObject(json, RoomNode.class));
                    } catch (Exception e) {
                        log.error("Failed to parse node: {}", key, e);
                    }
                }
            }
        }
        return nodes;
    }

    /**
     * 获取所有活跃房间节点 (状态=1)
     */
    public List<RoomNode> getActiveRoomNodes() {
        List<RoomNode> nodes = new ArrayList<>();
        Set<String> keys = redisUtil.keys(ROOM_NODE_PREFIX + "*");
        if (keys != null) {
            for (String key : keys) {
                String json = redisUtil.get(key);
                if (json != null) {
                    try {
                        RoomNode node = JSONObject.parseObject(json, RoomNode.class);
                        if (node.getStatus() == 1) {
                            nodes.add(node);
                        }
                    } catch (Exception e) {
                        log.error("Failed to parse node: {}", key, e);
                    }
                }
            }
        }
        return nodes;
    }

    // ==================== 同步 ====================

    private void doSyncToRedis(String roomId, RoomNode node) {
        String json = JSONObject.toJSONString(node);
        redisUtil.set(ROOM_NODE_PREFIX + roomId, json);
    }

    // ==================== 清理 ====================

    public void cleanTimeoutNodes(long timeoutMs) {
        long now = System.currentTimeMillis();
        List<RoomNode> nodes = getAllRoomNodes();

        for (RoomNode node : nodes) {
            synchronized (lock) {
                if (localRoomIds.contains(node.getRoomId())) {
                    continue;
                }
            }

            if (now - node.getLastHeartbeat() > timeoutMs) {
                redisUtil.delete(ROOM_NODE_PREFIX + node.getRoomId());
                log.warn("Room node timeout cleaned: roomId={}", node.getRoomId());
            }
        }
    }

    public void destroy() {
        synchronized (lock) {
            for (String roomId : localRoomIds) {
                redisUtil.delete(ROOM_NODE_PREFIX + roomId);
            }
            localRoomIds.clear();
            log.info("RoomNodeUtil destroyed");
        }
    }
}
