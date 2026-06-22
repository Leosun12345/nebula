package io.nebula.common.worker;

public interface Work extends Runnable {
    void init(Object... objs);
}
