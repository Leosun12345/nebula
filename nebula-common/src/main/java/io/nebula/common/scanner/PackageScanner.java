package io.nebula.common.scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PackageScanner {
    private static final Logger log = LoggerFactory.getLogger(PackageScanner.class);

    public static Set<Class<?>> scanPackages(String packageName) throws Exception {
        Set<Class<?>> set = new HashSet<>();
        if (packageName == null || packageName.trim().isEmpty()) {
            return set;
        }
        String[] strs = packageName.split(",");
        for (String str : strs) {
            set.addAll(scanPackages(str, true, Thread.currentThread().getContextClassLoader()));
        }
        return set;
    }

    public static Set<Class<?>> scanPackages(String packageName, boolean recursive, ClassLoader classLoader)
        throws Exception {
        Set<Class<?>> classes = new LinkedHashSet<>();
        String packageDirName = packageName.replace('.', '/');

        Enumeration<URL> dirs = classLoader.getResources(packageDirName);
        while (dirs.hasMoreElements()) {
            URL url = dirs.nextElement();
            String protocol = url.getProtocol();

            if ("file".equals(protocol)) {
                String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                findAndAddClassesInPackageByFile(classLoader, packageName, filePath, recursive, classes);
            } else if ("jar".equals(protocol)) {
                JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.charAt(0) == '/') {
                        name = name.substring(1);
                    }
                    if (name.startsWith(packageDirName) && name.endsWith(".class") && !entry.isDirectory()) {
                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                        Class<?> clz = classLoader.loadClass(packageName + '.' + className);
                        classes.add(clz);
                    }
                }
            }
        }
        return classes;
    }

    private static void findAndAddClassesInPackageByFile(ClassLoader classLoader, String packageName,
                                                         String packagePath, boolean recursive, Set<Class<?>> classes)
        throws Exception {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        File[] dirfiles = dir.listFiles((FileFilter) file ->
            (recursive && file.isDirectory()) || file.getName().endsWith(".class"));

        if (dirfiles == null) return;

        for (File file : dirfiles) {
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(classLoader,
                    packageName + "." + file.getName(),
                    file.getAbsolutePath(), recursive, classes);
            } else {
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clz = classLoader.loadClass(packageName + '.' + className);
                    classes.add(clz);
                } catch (Exception e) {
                    log.error("Failed to load class: " + packageName + '.' + className, e);
                }
            }
        }
    }
}
