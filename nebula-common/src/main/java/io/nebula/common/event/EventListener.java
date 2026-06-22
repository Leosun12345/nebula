package io.nebula.common.event;

@FunctionalInterface
public interface EventListener<T> {
    T notify(Object... objs);
}
