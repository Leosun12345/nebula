package io.nebula.logic.core.service;

import io.nebula.logic.core.room.Room;

public interface LogicService {
    void joinRoom(Room room, Object user);
    void exitRoom(Room room, Object user);
    void handleMessage(Room room, Object user, String message);
}
