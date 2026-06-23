package io.nebula.logic.core.room;

public interface RoomStatus {
    int ERROR = -5;
    int FREE = -4;
    int CLOSED = -3;
    int WAIT_NEXT_ROUND = -2;
    int CALCULATING = -1;
    int DEFAULT = 0;
    int PIPEI_DEFAULT = -7;
    int PIPEI_FINISH = -8;
    int PIPEI_READY = -9;
}
