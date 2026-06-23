package io.nebula.logic.core.room;

public interface RoomStatus {
    int ERROR = -5;
    int FREE = -4;
    int CLOSED = -3;
    int CALCULATING = -1;
    int DEFAULT = 0;
}
