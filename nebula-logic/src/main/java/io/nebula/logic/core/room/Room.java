package io.nebula.logic.core.room;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class Room {
    private final String roomId;
    private final String gameId;
    private final Object roomLock = new Object();
    private int roomStatus = RoomStatus.DEFAULT;
    private final Set<String> onlineList = new HashSet<>();

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
}
