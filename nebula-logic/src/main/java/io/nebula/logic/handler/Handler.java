package io.nebula.logic.handler;


import io.nebula.common.worker.AsynchronousWork;
import io.nebula.logic.core.room.Room;

public abstract class Handler extends AsynchronousWork {
    protected Room room;
    protected Object user;
    protected int sequence;
    protected String params;

    @Override
    public void init(Object... objs) {
        this.room = (Room) objs[0];
        this.user = objs[1];
        this.sequence = (int) objs[2];
        this.params = (String) objs[3];
    }

    public abstract void handle(Room room, Object user, int sequence, String params);

    @Override
    public void run() {
        handle(room, user, sequence, params);
    }
}
