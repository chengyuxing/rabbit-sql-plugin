package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.openapi.diagnostic.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public enum Store {
    INSTANCE;
    private static final Logger log = Logger.getInstance(Store.class);
    public final AtomicReference<Path> basePath = new AtomicReference<>(null);
    public final Set<Path> projectJavas = new HashSet<>();

    public void refreshJavaFiles() {
        projectJavas.removeIf(java -> !Files.exists(java));
    }

    /**
     * xql file manager to resolve and cache sql.
     */
    // TODO: 2023/4/9 add a XQL File Manager Control Panel for custom settings.
    public final XQLFileManager xqlFileManager = new XQLFileManager();

    public Map<String, String> allXqlFiles() {
        return xqlFileManager.allFiles();
    }

    public void clearAll() {
        xqlFileManager.clearFiles();
        xqlFileManager.clearSqlResources();
    }

    public boolean xqlConfigExists() {
        var p = xqlConfig();
        if (p == null) return false;
        return Files.exists(p);
    }

    public Path resourceRoot() {
        var basePath = INSTANCE.basePath.get();
        if (basePath == null) return null;
        return basePath.resolve(Path.of("src", "main", "resources"));
    }

    public Path xqlConfig() {
        var resourceRoot = resourceRoot();
        if (resourceRoot == null) return null;
        return resourceRoot.resolve(Constants.CONFIG_NAME);
    }

    public void reloadXqlFiles(BiConsumer<Boolean, String> reloaded) {
        try {
            xqlFileManager.init();
            reloaded.accept(true, null);
        } catch (Exception e) {
            reloaded.accept(false, e.getMessage());
            log.warn(e);
        }
    }

    // TODO: 2023/4/11 if current project is a module, what should i do.
    public void initXqlFiles(BiConsumer<Boolean, String> reloaded) {
        try {
            var resourceRoot = resourceRoot();
            if (resourceRoot != null) {
                var defaultConfig = xqlConfig();
                if (defaultConfig != null && Files.exists(defaultConfig)) {
                    var properties = new Properties();
                    Map<String, String> files = new HashMap<>();
                    Set<String> filenames = new HashSet<>();
                    properties.load(Files.newInputStream(defaultConfig));
                    properties.forEach((k, s) -> {
                        String p = k.toString().trim();
                        String v = s.toString().trim();
                        if (p.startsWith("files.") && p.length() > 6 && !v.equals("")) {
                            var alias = p.substring(6);
                            var abPath = resourceRoot.resolve(v);
                            if (Files.exists(abPath)) {
                                files.put(alias, abPath.toUri().toString());
                            }
                        }
                    });
                    Stream.of(properties.getProperty("filenames", "").split(","))
                            .map(String::trim)
                            .filter(name -> !name.equals(""))
                            .map(resourceRoot::resolve)
                            .filter(Files::exists)
                            .forEach(p -> filenames.add(p.toUri().toString()));
                    xqlFileManager.clearSqlResources();
                    xqlFileManager.clearFiles();
                    xqlFileManager.setFiles(files);
                    xqlFileManager.setFilenames(filenames);
                    xqlFileManager.setCharset(properties.getProperty("charset", "UTF-8"));
                    xqlFileManager.setDelimiter(properties.getProperty("delimiter", ";"));
                    reloadXqlFiles(reloaded);
                } else {
                    xqlFileManager.clearSqlResources();
                    xqlFileManager.clearFiles();
                }
            }
        } catch (Exception e) {
            reloaded.accept(false, e.getMessage());
            log.warn(e);
        }
    }
}
