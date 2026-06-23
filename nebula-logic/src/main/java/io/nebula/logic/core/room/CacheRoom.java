package io.nebula.logic.core.room;

import io.nebula.common.event.EventTag;
import io.nebula.common.event.EventUtil;
import io.nebula.logic.core.LogicContext;
import io.nebula.logic.core.service.LogicService;
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
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    // Spring Bean 引用
    private static final EventUtil eventUtil = LogicContext.getInstance(EventUtil.class);
    private static final LogicService logicService = LogicContext.getInstance(LogicService.class);

    // 查找缓存 (可选: 按 gameId 索引房间列表)
    private final Map<String, List<String>> gameIdRoomIndex = new ConcurrentHashMap<>();

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

        // 更新索引
        gameIdRoomIndex.computeIfAbsent(gameId, k -> new ArrayList<>()).add(roomId);

        // 触发创建事件
        eventUtil.throwEvent(EventTag.GAME_ROOM_CREATED_INIT, room);

        log.info("Room created: roomId={}, gameId={}, total={}",
            roomId, gameId, rooms.size());
        return room;
    }

    /**
     * 释放房间
     */
    public void freeRoom(String roomId) {
        Room room = rooms.remove(roomId);
        if (room != null) {
            room.setRoomStatus(RoomStatus.FREE);

            // 从索引中移除
            List<String> index = gameIdRoomIndex.get(room.getGameId());
            if (index != null) {
                index.remove(roomId);
            }

            log.info("Room freed: roomId={}", roomId);
        }
    }

    /**
     * 获取房间
     */
    public Room getRoomById(String roomId) {
        return rooms.get(roomId);
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }

    // ==================== 核心方法: 查找可用房间 ====================

    /**
     * 查找可用房间 (快速加入)
     *
     * @param gameId 游戏ID
     * @return 可用房间，如果没有则返回 null
     */
    public Room findAvailableRoom(String gameId) {
        return findAvailableRoom(gameId, null);
    }

    /**
     * 查找可用房间 (排除指定用户)
     *
     * @param gameId 游戏ID
     * @param excludeUid 排除的用户ID (用户已在房间内)
     * @return 可用房间，如果没有则返回 null
     */
    public Room findAvailableRoom(String gameId, String excludeUid) {
        long startTime = System.currentTimeMillis();

        // 1. 获取该游戏的所有房间ID列表
        List<String> roomIds = gameIdRoomIndex.get(gameId);
        if (roomIds == null || roomIds.isEmpty()) {
            log.debug("No rooms for gameId: {}", gameId);
            return null;
        }

        // 2. 遍历查找
        List<Room> candidates = new ArrayList<>();

        for (String roomId : roomIds) {
            Room room = rooms.get(roomId);
            if (room == null) {
                continue;
            }

            // 条件检查
            if (!isRoomAvailable(room, excludeUid)) {
                continue;
            }

            candidates.add(room);
        }

        if (candidates.isEmpty()) {
            log.debug("No available room for gameId: {}, excludeUid: {}", gameId, excludeUid);
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

        // 3. 训练房间不参与快速加入
        if (room.isTrain()) {
            return false;
        }

        // 4. 匹配房间不参与快速加入
        if (room.isPipei()) {
            return false;
        }

        // 5. 排除已在房间内的用户
        if (excludeUid != null && room.getUidList().contains(excludeUid)) {
            return false;
        }

        // 6. 点杀房间特殊处理 (只有创建者能进入)
        String pandaUid = room.getPandaRoomCreateUid();
        if (pandaUid != null && !pandaUid.isEmpty()) {
            // 点杀房间只允许创建者进入
            return excludeUid != null && pandaUid.equals(excludeUid);
        }

        return true;
    }

    // ==================== 查找方法变体 ====================

    /**
     * 查找可用房间 (考虑用户最近房间)
     */
    public Room findAvailableRoom(String gameId, String excludeUid, List<String> recentRoomIds) {
        long startTime = System.currentTimeMillis();

        List<String> roomIds = gameIdRoomIndex.get(gameId);
        if (roomIds == null || roomIds.isEmpty()) {
            return null;
        }

        List<Room> candidates = new ArrayList<>();
        Room preferredRoom = null;

        for (String roomId : roomIds) {
            Room room = rooms.get(roomId);
            if (room == null) {
                continue;
            }

            if (!isRoomAvailable(room, excludeUid)) {
                continue;
            }

            // 优先选择用户最近所在的房间
            if (recentRoomIds != null && recentRoomIds.contains(roomId)) {
                preferredRoom = room;
                break;
            }

            candidates.add(room);
        }

        Room selected = preferredRoom != null ? preferredRoom :
            (candidates.isEmpty() ? null : candidates.get(0));

        if (selected != null) {
            log.info("Found available room: roomId={}, preferred={}, cost={}ms",
                selected.getRoomId(), preferredRoom != null,
                System.currentTimeMillis() - startTime);
        }

        return selected;
    }

    /**
     * 查找可用房间 (带扩展条件)
     */
    public Room findAvailableRoom(String gameId, String excludeUid, RoomFilter filter) {
        List<String> roomIds = gameIdRoomIndex.get(gameId);
        if (roomIds == null || roomIds.isEmpty()) {
            return null;
        }

        for (String roomId : roomIds) {
            Room room = rooms.get(roomId);
            if (room == null) {
                continue;
            }

            if (!isRoomAvailable(room, excludeUid)) {
                continue;
            }

            if (filter != null && !filter.accept(room)) {
                continue;
            }

            return room;
        }

        return null;
    }

    /**
     * 获取某游戏所有房间 (用于管理)
     */
    public List<Room> getRoomsByGameId(String gameId) {
        List<String> roomIds = gameIdRoomIndex.get(gameId);
        if (roomIds == null || roomIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Room> result = new ArrayList<>();
        for (String roomId : roomIds) {
            Room room = rooms.get(roomId);
            if (room != null) {
                result.add(room);
            }
        }
        return result;
    }

    // ==================== 索引维护 ====================

    /**
     * 重建索引 (用于恢复)
     */
    public void rebuildIndex() {
        gameIdRoomIndex.clear();
        for (Map.Entry<String, Room> entry : rooms.entrySet()) {
            String roomId = entry.getKey();
            Room room = entry.getValue();
            if (room != null) {
                gameIdRoomIndex.computeIfAbsent(room.getGameId(), k -> new ArrayList<>()).add(roomId);
            }
        }
        log.info("Rebuilt index: {} rooms, {} gameIds",
            rooms.size(), gameIdRoomIndex.size());
    }

    // ==================== 回调接口 ====================

    @FunctionalInterface
    public interface RoomFilter {
        boolean accept(Room room);
    }
}
