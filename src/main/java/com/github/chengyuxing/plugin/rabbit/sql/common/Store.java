package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.github.chengyuxing.common.io.TypedProperties;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.exceptions.DuplicateException;
import com.intellij.openapi.diagnostic.Logger;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

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

    // TODO: 2023/4/11 if current project is a module, what should i do.
    public void initXqlFiles(BiConsumer<Boolean, String> reloaded) {
        try {
            var resourceRoot = resourceRoot();
            if (resourceRoot != null) {
                var defaultConfig = xqlConfig();
                if (defaultConfig != null && Files.exists(defaultConfig)) {
                    var properties = new TypedProperties();
                    properties.load(Files.newInputStream(defaultConfig));

                    Map<String, String> files = new HashMap<>();
                    properties.getMap("files", new HashMap<>())
                            .forEach((k, v) -> {
                                var path = v.trim();
                                if (!path.equals("")) {
                                    var abPath = resourceRoot.resolve(path);
                                    if (Files.exists(abPath)) {
                                        files.put(k, abPath.toUri().toString());
                                    }
                                }
                            });

                    var filenames = properties.getSet("filenames", new HashSet<>())
                            .stream()
                            .map(resourceRoot::resolve)
                            .filter(Files::exists)
                            .map(p -> p.toUri().toString())
                            .collect(Collectors.toSet());

                    xqlFileManager.clearSqlResources();
                    xqlFileManager.clearFiles();
                    xqlFileManager.setFiles(files);
                    xqlFileManager.setFilenames(filenames);
                    xqlFileManager.setCharset(properties.getProperty("charset", "UTF-8"));
                    xqlFileManager.setDelimiter(properties.getProperty("delimiter", ";"));
                    xqlFileManager.setNamedParamPrefix(properties.getProperty("namedParamPrefix", ":").charAt(0));
                    xqlFileManager.init();
                    reloaded.accept(true, null);
                } else {
                    xqlFileManager.clearSqlResources();
                    xqlFileManager.clearFiles();
                }
            }
        } catch (DuplicateException e) {
            reloaded.accept(false, "Warning: " + e.getMessage());
            log.warn(e);
        } catch (UncheckedIOException e) {
            log.info("xql file removed!");
        } catch (Exception e) {
            reloaded.accept(false, "Error:" + e.getMessage());
            log.error(e);
        }
    }
}
