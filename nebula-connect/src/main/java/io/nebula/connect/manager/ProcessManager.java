package io.nebula.connect.manager;

import io.nebula.common.scanner.DynamicFind;
import io.nebula.connect.process.annotation.IREQ;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProcessManager extends DynamicFind {
    private static final ProcessManager INSTANCE = new ProcessManager();
    private final Map<String, Class<?>> classMap = new HashMap<>();

    public static ProcessManager getInstance() {
        return INSTANCE;
    }

    @Override
    public <T> void findClass(Class<T> clz) throws Exception {
        IREQ req = clz.getAnnotation(IREQ.class);
        if (req != null) {
            for (String code : req.codes()) {
                classMap.put(code, clz);
            }
        }
    }

    @Override
    public boolean verification(Class<?> clazz) {
        return annotationOn(clazz, IREQ.class);
    }

    @Override
    public void beforeFind(Set<Class<?>> clazzs) {}

    @Override
    public void afterFind(Set<Class<?>> clazzs) {}

    public Object getProcess(String code) {
        Class<?> clz = classMap.get(code);
        if (clz == null) return null;
        try {
            return clz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }
}
