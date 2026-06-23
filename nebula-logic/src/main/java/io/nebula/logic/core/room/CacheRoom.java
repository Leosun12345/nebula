package io.nebula.logic.core.room;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class CacheRoom {
    private static final Logger log = LoggerFactory.getLogger(CacheRoom.class);
    private static final CacheRoom INSTANCE = new CacheRoom();
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    public static CacheRoom getInstance() {
        return INSTANCE;
    }

    public Room roomCreate(String roomId, String gameId) {
        Room room = new Room(roomId, gameId);
        rooms.put(roomId, room);
        log.info("Room created: {}", roomId);
        return room;
    }

    public Room getRoomById(String roomId) {
        return rooms.get(roomId);
    }

    public void freeRoom(String roomId) {
        Room room = rooms.remove(roomId);
        if (room != null) {
            room.setRoomStatus(RoomStatus.FREE);
            log.info("Room freed: {}", roomId);
        }
    }

}
