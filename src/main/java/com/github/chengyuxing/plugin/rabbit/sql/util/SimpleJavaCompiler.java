package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.common.tuple.Pair;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SimpleJavaCompiler {
    private static volatile SimpleJavaCompiler instance;
    private final JavaCompiler COMPILER = ToolProvider.getSystemJavaCompiler();

    private SimpleJavaCompiler() {

    }

    public static SimpleJavaCompiler getInstance() {
        if (instance == null) {
            synchronized (SimpleJavaCompiler.class) {
                if (instance == null) {
                    instance = new SimpleJavaCompiler();
                }
            }
        }
        return instance;
    }

    public List<Class<?>> compile(List<Pair<String, Path>> javaFiles, ClassLoader parent) {
        var files = javaFiles.stream().map(p -> {
            try {
                var source = Files.readString(p.getItem2(), StandardCharsets.UTF_8);
                return new StringJavaFileObject(source, p.getItem1());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).collect(Collectors.toList());
        final MyJavaFileManager JAVA_FILE_MANAGER = new MyJavaFileManager(COMPILER.getStandardFileManager(null, null, StandardCharsets.UTF_8));
        var task = COMPILER.getTask(null, JAVA_FILE_MANAGER, new DiagnosticCollector<>(), null, null, files);
        var success = task.call();
        if (success) {
            var res = JAVA_FILE_MANAGER.getByteArrayJavaFileObjects();
            if (res.isEmpty()) {
                return List.of();
            }
            return res.stream().map(o -> {
                try {
                    ByteArrayOutputStream out = (ByteArrayOutputStream) o.openOutputStream();
                    var bytes = out.toByteArray();
                    out.close();
                    ClassLoader classLoader;
                    if (Objects.isNull(parent)) {
                        classLoader = new ClassLoader() {
                            @Override
                            protected Class<?> findClass(String name) {
                                return defineClass(name, bytes, 0, bytes.length);
                            }
                        };
                    } else {
                        classLoader = new ClassLoader(parent) {
                            @Override
                            protected Class<?> findClass(String name) {
                                return defineClass(name, bytes, 0, bytes.length);
                            }
                        };
                    }
                    return classLoader.loadClass(o.getClassName());
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
        }
        return List.of();
    }

    public Class<?> compile(String className, Path javaFile, ClassLoader parent) {
        return compile(List.of(Pair.of(className, javaFile)), parent)
                .stream()
                .filter(c -> c.getName().equals(className))
                .findFirst()
                .orElse(null);
    }

    public Class<?> compile(String className, Path javaFile) {
        return compile(className, javaFile, null);
    }

    static class StringJavaFileObject extends SimpleJavaFileObject {
        private final String content;

        protected StringJavaFileObject(String content, String className) {
            super(URI.create("string:///" + className.replace(".", "/") + ".java"), Kind.SOURCE);
            this.content = content;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return content;
        }
    }

    static class ByteArrayJavaFileObject extends SimpleJavaFileObject {
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        private final String className;

        public ByteArrayJavaFileObject(String name) {
            super(URI.create("class:///" + name.replace(".", "/") + ".class"), Kind.CLASS);
            className = name;
        }

        public String getClassName() {
            return className;
        }

        @Override
        public OutputStream openOutputStream() {
            return outputStream;
        }
    }

    static class MyJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
        public MyJavaFileManager(JavaFileManager fileManager) {
            super(fileManager);
        }

        private final Set<ByteArrayJavaFileObject> byteArrayJavaFileObjects = new HashSet<>();

        public Set<ByteArrayJavaFileObject> getByteArrayJavaFileObjects() {
            return byteArrayJavaFileObjects;
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            if (JavaFileObject.Kind.CLASS == kind) {
                ByteArrayJavaFileObject o = new ByteArrayJavaFileObject(className);
                byteArrayJavaFileObjects.add(o);
                return o;
            } else {
                return super.getJavaFileForOutput(location, className, kind, sibling);
            }
        }
    }
}
