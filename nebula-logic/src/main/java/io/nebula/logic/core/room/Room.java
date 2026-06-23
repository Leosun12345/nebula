package io.nebula.logic.core.room;

import lombok.Data;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class Room {
    private final String roomId;
    private final String gameId;
    private final Object roomLock = new Object();

    private int roomStatus = RoomStatus.DEFAULT;
    private int maxPlayer = 6;
    private int configId;
    private boolean train = false;
    private boolean pipei = false;
    private String pandaRoomCreateUid;

    private final Set<String> onlineList = new HashSet<>();
    private final List<Object> players = new ArrayList<>();

    public Room(String roomId, String gameId) {
        this.roomId = roomId;
        this.gameId = gameId;
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

    public boolean isFull() {
        return players.size() >= maxPlayer;
    }

    public List<String> getUidList() {
        List<String> uids = new ArrayList<>();
        for (Object player : players) {
            // 假设 player 有 getUid() 方法
        }
        return uids;
    }
}
