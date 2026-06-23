package io.nebula.common.scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Set;

public abstract class DynamicFind {
    private static final Logger log = LoggerFactory.getLogger(DynamicFind.class);

    private String packagePath = "io.nebula";

    public void setPackagePath(String packagePath) {
        this.packagePath = packagePath;
    }

    protected String getPackageScanPath() {
        return packagePath;
    }

    public synchronized void start() {
        try {
            Set<Class<?>> clazzs = PackageScanner.scanPackages(getPackageScanPath());
            if (!clazzs.isEmpty()) {
                beforeFind(clazzs);
                find(clazzs);
                afterFind(clazzs);
            }
        } catch (Exception e) {
            log.error("DynamicFind start failed", e);
            throw new RuntimeException("DynamicFind start failed", e);
        }
    }

    public void find(Set<Class<?>> clazzs) throws Exception {
        for (Class<?> clz : clazzs) {
            findOne(clz);
        }
    }

    public void findOne(Class<?> clazz) throws Exception {
        if (verification(clazz)) {
            findClass(clazz);
        }
    }

    public abstract <T> void findClass(Class<T> clz) throws Exception;
    public abstract boolean verification(Class<?> clazz);
    public abstract void beforeFind(Set<Class<?>> clazzs);
    public abstract void afterFind(Set<Class<?>> clazzs);

    public boolean isAbstract(Class<?> clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    public boolean isInter(Class<?> clazz) {
        return clazz.isInterface();
    }

    public boolean interfaceOn(Class<?> clazz, Class<?> inter) {
        if (clazz.isInterface() || clazz.isAnonymousClass() || isAbstract(clazz)) {
            return false;
        }
        for (Class<?> clz : clazz.getInterfaces()) {
            if (clz.equals(inter)) {
                return true;
            }
        }
        return false;
    }

    public boolean superClassOn(Class<?> clazz, Class<?> superClz) {
        if (clazz.isInterface() || clazz.isAnonymousClass() || isAbstract(clazz)) {
            return false;
        }
        return clazz.getSuperclass().equals(superClz);
    }

    public boolean annotationOn(Class<?> clazz, Class<? extends Annotation> annotationClz) {
        if (clazz.isInterface() || clazz.isAnonymousClass() || isAbstract(clazz)) {
            return false;
        }
        return clazz.isAnnotationPresent(annotationClz);
    }

    public boolean haveSuperInterfaceOn(Class<?> clazz, Class<?> superInter) {
        if (clazz == null) return false;
        for (Class<?> clz : clazz.getInterfaces()) {
            if (clz.equals(superInter)) {
                return true;
            }
            if (haveSuperInterfaceOn(clz, superInter)) {
                return true;
            }
        }
        return haveSuperInterfaceOn(clazz.getSuperclass(), superInter);
    }
}
