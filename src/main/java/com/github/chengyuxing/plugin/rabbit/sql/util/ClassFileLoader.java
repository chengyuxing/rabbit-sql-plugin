package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.utils.ResourceUtil;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

public class ClassFileLoader extends ClassLoader {
    private final Path rootDir;

    public ClassFileLoader(ClassLoader parent, Path rootDir) {
        super(parent);
        this.rootDir = rootDir;
    }

    public static ClassFileLoader of(ClassLoader parent, Path rootDir) {
        return new ClassFileLoader(parent, rootDir);
    }

    @Override
    public Class<?> findClass(String name) {
        var classPath = rootDir.resolve(Path.of(ResourceUtil.package2path(name) + ".class"));
        var file = new FileResource(classPath.toUri().toString());
        if (file.exists()) {
            byte[] bytes;
            try {
                bytes = file.readBytes();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return defineClass(name, bytes, 0, bytes.length);
        }
        return null;
    }
}
