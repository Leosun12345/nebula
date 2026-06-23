package io.nebula.logic.core.room;

import io.nebula.common.event.EventTag;
import io.nebula.common.event.EventUtil;
import io.nebula.logic.core.LogicContext;
import io.nebula.logic.core.service.LogicService;
import io.nebula.logic.util.RoomNodeUtil;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 房间缓存管理器
 *
 * @author leo
 * @since 1.0.0
 */
public class CacheRoom {
    private static final Logger log = LoggerFactory.getLogger(CacheRoom.class);
    private static final CacheRoom INSTANCE = new CacheRoom();

    // 房间缓存
    @Getter
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    // Spring Bean 引用
    private static final EventUtil eventUtil = LogicContext.getInstance(EventUtil.class);
    private static final LogicService logicService = LogicContext.getInstance(LogicService.class);
    private static final RoomNodeUtil roomNodeUtil = LogicContext.getInstance(RoomNodeUtil.class);

    private CacheRoom() {}

    public static CacheRoom getInstance() {
        return INSTANCE;
    }

    // ==================== 房间管理 ====================

    /**
     * 创建房间
     */
    public Room roomCreate(String roomId, String gameId) {
        Room room = new Room(roomId, gameId);
        rooms.put(roomId, room);

        // 注册到Redis节点
        roomNodeUtil.registerRoom(roomId);

        // 触发创建事件
        eventUtil.throwEvent(EventTag.GAME_ROOM_CREATED_INIT, room);

        log.info("Room created: roomId={}, gameId={}, total={}",
            roomId, gameId, rooms.size());
        return room;
    }

    // ==================== 房间关闭 ====================

    /**
     * 关闭单个房间
     */
    public boolean closeRoom(String roomId, String reason) {
        Room room = rooms.get(roomId);
        if (room == null) {
            log.warn("Room not found: {}", roomId);
            return false;
        }

        synchronized (room.getRoomLock()) {
            if (room.isClosed()) {
                log.warn("Room already closed: {}", roomId);
                return false;
            }

            room.close(reason);
            roomNodeUtil.unregisterRoom(roomId);
            log.info("Room closed: roomId={}, reason={}", roomId, reason);
            return true;
        }
    }

    /**
     * 批量关闭房间
     */
    public int closeRooms(List<String> roomIds, String reason) {
        int count = 0;
        for (String roomId : roomIds) {
            if (closeRoom(roomId, reason)) {
                count++;
            }
        }
        log.info("Batch closed {} rooms, reason={}", count, reason);
        return count;
    }

    /**
     * 关闭所有房间 (服务关闭时)
     */
    public int closeAllRooms(String reason) {
        List<String> allIds = new ArrayList<>(rooms.keySet());
        return closeRooms(allIds, reason);
    }

    /**
     * 释放已关闭的空房间 (定时清理)
     */
    public int freeEmptyClosedRooms() {
        List<String> toRemove = new ArrayList<>();

        for (Room room : rooms.values()) {
            if (room.isClosed()) {
                toRemove.add(room.getRoomId());
            }
        }

        for (String roomId : toRemove) {
            rooms.remove(roomId);
            log.debug("Room freed: {}", roomId);
        }

        if (!toRemove.isEmpty()) {
            log.info("Freed {} empty closed rooms", toRemove.size());
        }
        return toRemove.size();
    }

    // ==================== 核心方法: 查找可用房间 ====================
    /**
     * 获取房间
     */
    public Room getRoomById(String roomId) {
        return rooms.get(roomId);
    }


    /**
     * 查找可用房间 (快速加入)
     *
     * @return 可用房间，如果没有则返回 null
     */
    public Room findAvailableRoom() {
        return findAvailableRoom(null,null);
    }

    /**
     * 查找可用房间 (排除指定用户)
     *
     * @param excludeUid 排除的用户ID (用户已在房间内)
     * @return 可用房间，如果没有则返回 null
     */
    public Room findAvailableRoom(String excludeUid, RoomFilter filter) {
        long startTime = System.currentTimeMillis();

        // 2. 遍历查找
        List<Room> candidates = new ArrayList<>();

        for (Room room : rooms.values()) {
            // 条件检查
            if (!isRoomAvailable(room, excludeUid)) {
                continue;
            }

            if (filter != null && !filter.accept(room)) {
                continue;
            }

            candidates.add(room);
        }

        if (candidates.isEmpty()) {
            log.debug("No available room for excludeUid: {}", excludeUid);
            return null;
        }

        // 3. 随机选择一个候选房间
        Collections.shuffle(candidates);
        Room selected = candidates.get(0);

        log.info("Found available room: roomId={}, gameId={}, playerNum={}, maxPlayer={}, cost={}ms",
            selected.getRoomId(), selected.getGameId(),
            selected.getPlayerNum(), selected.getMaxPlayer(),
            System.currentTimeMillis() - startTime);

        return selected;
    }

    /**
     * 检查房间是否可用
     */
    private boolean isRoomAvailable(Room room, String excludeUid) {
        // 1. 状态检查
        int status = room.getRoomStatus();
        if (status != RoomStatus.DEFAULT && status != RoomStatus.WAIT_NEXT_ROUND) {
            return false;
        }

        // 2. 房间是否已满
        if (room.isFull()) {
            return false;
        }

        // 5. 排除已在房间内的用户
        if (excludeUid != null && room.getOnlineList().contains(excludeUid)) {
            return false;
        }

        return true;
    }

    // ==================== 回调接口 ====================

    @FunctionalInterface
    public interface RoomFilter {
        boolean accept(Room room);
    }
}
