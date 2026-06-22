package io.nebula.common.scanner;

import java.util.Set;

public interface Find {
    void beforeFind(Set<Class<?>> clazzs);
    void find(Set<Class<?>> clazzs) throws Exception;
    boolean verification(Class<?> clazz);
    void afterFind(Set<Class<?>> clazzs);
    void start() throws Exception;
}
