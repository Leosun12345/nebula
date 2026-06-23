package io.nebula.logic.core.room;

import lombok.Data;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.nebula.logic.core.room.RoomStatus.*;

@Data
public class Room {
    private final String roomId;
    private final String gameId;
    private final Object roomLock = new Object();

    private int roomStatus = RoomStatus.DEFAULT;
    /** 关闭原因 */
    private String closeReason;
    /** 关闭时间 */
    private long closeTime;
    private int maxPlayer = 6;
    private int configId;

    private final Set<String> onlineList = new HashSet<>();
    private final List<Object> players = new ArrayList<>();

    public Room(String roomId, String gameId) {
        this.roomId = roomId;
        this.gameId = gameId;
    }

    // ==================== 关闭操作 ====================

    /**
     * 关闭房间
     */
    public void close(String reason) {
        synchronized (roomLock) {
            if (roomStatus == RoomStatus.CLOSED) {
                return;
            }
            this.roomStatus = RoomStatus.CLOSED;
            this.closeReason = reason;
            this.closeTime = System.currentTimeMillis();
        }
    }

    /**
     * 判断房间是否已关闭
     */
    public boolean isClosed() {
        return roomStatus == RoomStatus.CLOSED;
    }

    /**
     * 判断房间是否可加入
     */
    public boolean isJoinable() {
        return !isClosed() && !isFull();
    }

    public boolean isFull() {
        return players.size() >= maxPlayer;
    }

    public boolean isOnline(String uid) {
        return onlineList.contains(uid);
    }

    public void reOnline(String uid) {
        onlineList.add(uid);
    }

    public void offline(String uid) {
        onlineList.remove(uid);
    }

    public long getQueueId() {
        return roomId.hashCode() % 32;
    }

    public int getPlayerNum() {
        return players.size();
    }
}
