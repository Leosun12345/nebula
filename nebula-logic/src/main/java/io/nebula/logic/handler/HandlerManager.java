package io.nebula.logic.handler;

import io.nebula.common.scanner.DynamicFind;
import io.nebula.logic.handler.annotation.ISubCode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HandlerManager extends DynamicFind {
    private static final HandlerManager INSTANCE = new HandlerManager();
    private final Map<String, Class<?>> classMap = new HashMap<>();

    public static HandlerManager getInstance() {
        return INSTANCE;
    }

    @Override
    public <T> void findClass(Class<T> clz) throws Exception {
        ISubCode subCode = clz.getAnnotation(ISubCode.class);
        if (subCode != null) {
            for (String code : subCode.subCodes()) {
                classMap.put(code, clz);
            }
        }
    }

    @Override
    public boolean verification(Class<?> clazz) {
        return annotationOn(clazz, ISubCode.class);
    }

    @Override
    public void beforeFind(Set<Class<?>> clazzs) {}

    @Override
    public void afterFind(Set<Class<?>> clazzs) {}

    public Object getHandler(String subCode) {
        Class<?> clz = classMap.get(subCode);
        if (clz == null) return null;
        try {
            return clz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }
}
