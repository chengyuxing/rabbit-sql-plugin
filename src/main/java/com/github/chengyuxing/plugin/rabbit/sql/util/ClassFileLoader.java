package com.github.chengyuxing.plugin.rabbit.sql.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClassFileLoader extends ClassLoader {
    private final Path[] rootDirs;

    public ClassFileLoader(ClassLoader parent, Path... rootDirs) {
        super(parent);
        this.rootDirs = rootDirs.clone();
    }

    public static ClassFileLoader of(ClassLoader parent, Path... rootDirs) {
        return new ClassFileLoader(parent, rootDirs);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        for (Path rootDir : rootDirs) {
            var classPath = rootDir.resolve(name.replace('.', '/') + ".class");
            if (Files.isRegularFile(classPath)) {
                try {
                    byte[] bytes = Files.readAllBytes(classPath);
                    return defineClass(name, bytes, 0, bytes.length);
                } catch (IOException e) {
                    throw new ClassNotFoundException(name, e);
                }
            }
        }
        throw new ClassNotFoundException(name);
    }
}
