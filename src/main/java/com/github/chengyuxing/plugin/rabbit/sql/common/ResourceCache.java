package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.github.chengyuxing.common.io.TypedProperties;
import com.github.chengyuxing.common.utils.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.XqlUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.exceptions.DuplicateException;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.chengyuxing.plugin.rabbit.sql.util.XqlUtil.getModuleBaseDir;

public class ResourceCache {
    private static final Logger log = Logger.getInstance(ResourceCache.class);
    private static volatile ResourceCache instance;

    /**
     * key: module dir, value: resource which be owned by project
     */
    private final Map<Path, Resource> cache = new ConcurrentHashMap<>();

    private ResourceCache() {
    }

    public static ResourceCache getInstance() {
        if (instance == null) {
            synchronized (ResourceCache.class) {
                if (instance == null) {
                    instance = new ResourceCache();
                }
            }
        }
        return instance;
    }

    public void clearJavas(Path xqlFileManagerLocation) {
        var key = getModuleBaseDir(xqlFileManagerLocation);
        if (cache.containsKey(key)) {
            cache.get(key).javas.clear();
        }
    }

    public void clearXqlFiles(Path xqlFileManagerLocation) {
        var key = getModuleBaseDir(xqlFileManagerLocation);
        if (cache.containsKey(key)) {
            cache.get(key).xqlFileManager.clearFiles();
            cache.get(key).xqlFileManager.clearSqlResources();
        }
    }

    public void clearAll() {
        cache.clear();
    }

    public void clear(Path xqlFileManagerLocation) {
        var key = getModuleBaseDir(xqlFileManagerLocation);
        if (cache.containsKey(key)) {
            var resource = cache.get(key);
            resource.xqlFileManager.clearFiles();
            resource.xqlFileManager.clearSqlResources();
            resource.javas.clear();
            cache.remove(key);
        }
    }

    public Resource getResource(Path xqlFileManagerLocation) {
        var key = getModuleBaseDir(xqlFileManagerLocation);
        return cache.get(key);
    }

    public Resource getResource(Project project, VirtualFile virtualFile) {
        return cache.get(PsiUtil.getModuleDir(project, virtualFile));
    }

    public Resource getResource(Project project, PsiElement element) {
        return cache.get(PsiUtil.getModuleDir(project, element));
    }

    public Resource getResource(PsiElement element) {
        var key = PsiUtil.getModuleDir(element);
        return cache.get(key);
    }

    public void initXqlFileManager(Project project) {
        // e.g
        // file:///Users/chengyuxing/IdeaProjects/sbp-test1/src/test/java
        // file:///Users/chengyuxing/IdeaProjects/sbp-test1/src/main/resources
        // file:///Users/chengyuxing/IdeaProjects/sbp-test1/src/main/java
        Stream.of(ProjectRootManager.getInstance(project).getContentSourceRoots()).forEach(vf -> {
            var xqlFileManager = vf.toNioPath().resolve(Constants.CONFIG_NAME);
            if (XqlUtil.xqlFileManagerExists(xqlFileManager)) {
                log.info("project opened: " + xqlFileManager + ", found xql config, init!");
                ResourceCache resourceCache = ResourceCache.getInstance();
                resourceCache.initJavas(xqlFileManager);
                resourceCache.initXqlFileManager(xqlFileManager, (success, msg) -> {
                    if (success) {
                        Notifications.Bus.notify(new Notification("Rabbit-SQL Notification Group", "XQL file manager", "XQL file Manager initialized!", NotificationType.INFORMATION), project);
                    } else {
                        Notifications.Bus.notify(new Notification("Rabbit-SQL Notification Group", "XQL file manager", msg, NotificationType.WARNING), project);
                    }
                });
            }
        });
    }

    public void initXqlFileManager(Path xqlFileManagerLocation, BiConsumer<Boolean, String> reloaded) {
        try {
            var baseDir = getModuleBaseDir(xqlFileManagerLocation);
            if (baseDir != null) {
                // ...src/main/resources
                var resourceRoot = xqlFileManagerLocation.getParent();

                if (!cache.containsKey(baseDir)) {
                    cache.put(baseDir, new Resource());
                }

                var properties = new TypedProperties();
                properties.load(Files.newInputStream(xqlFileManagerLocation));

                log.info("init xql file manager: load " + xqlFileManagerLocation + " " + properties);

                Map<String, String> files = new HashMap<>();
                properties.getMap("files", new HashMap<>())
                        .forEach((k, v) -> {
                            var path = v.trim();
                            if (!path.equals("")) {
                                var abPath = resourceRoot.resolve(path);
                                if (Files.exists(abPath)) {
                                    files.put(k, abPath.toUri().toString());
                                } else {
                                    reloaded.accept(false, path + " not exists.");
                                }
                            }
                        });

                var filenames = properties.getSet("filenames", new HashSet<>())
                        .stream()
                        .map(resourceRoot::resolve)
                        .filter(p -> {
                            var exists = Files.exists(p);
                            if (!exists) {
                                reloaded.accept(false, p.getFileName() + " not exists.");
                            }
                            return exists;
                        })
                        .map(p -> p.toUri().toString())
                        .collect(Collectors.toSet());

                log.info("read files: " + files);
                log.info("read filenames: " + filenames);

                var xqlFileManager = cache.get(baseDir).getXqlFileManager();
                xqlFileManager.clearSqlResources();
                xqlFileManager.clearFiles();

                xqlFileManager.setFiles(files);
                xqlFileManager.setFilenames(filenames);
                xqlFileManager.setCharset(properties.getProperty("charset", "UTF-8"));
                xqlFileManager.setDelimiter(properties.getProperty("delimiter", ";"));
                xqlFileManager.setNamedParamPrefix(properties.getProperty("namedParamPrefix", ":").charAt(0));
                xqlFileManager.init();
                reloaded.accept(true, null);
            }
        } catch (DuplicateException e) {
            reloaded.accept(false, "Warning: " + e.getMessage());
            log.warn(e);
        } catch (UncheckedIOException e) {
            log.warn("xql file removed!");
        } catch (Exception e) {
            reloaded.accept(false, "Error:" + e.getMessage());
            log.error(e);
        }
    }

    public void addJava(Path xqlFileManagerLocation, Path java) {
        var baseDir = getModuleBaseDir(xqlFileManagerLocation);
        if (baseDir != null && cache.containsKey(baseDir)) {
            cache.get(baseDir).getJavas().add(java);
        }
    }

    public void refreshJavas(Path xqlFileManagerLocation) {
        var baseDir = getModuleBaseDir(xqlFileManagerLocation);
        if (baseDir != null && cache.containsKey(baseDir)) {
            cache.get(baseDir).getJavas().removeIf(p -> !Files.exists(p));
        }
    }

    public void initJavas(Path xqlFileManagerLocation) {
        var baseDir = getModuleBaseDir(xqlFileManagerLocation);
        if (baseDir != null) {
            // ...src
            if (!cache.containsKey(baseDir)) {
                cache.put(baseDir, new Resource());
            }

            var sourceRoot = xqlFileManagerLocation.getParent().getParent().getParent();
            if (Files.exists(sourceRoot)) {
                try (Stream<Path> pathStream = Files.find(sourceRoot, 15, (p, a) -> a.isRegularFile() && StringUtil.endsWiths(p.toString(), ".java", ".scala", ".kt"))) {
                    pathStream.forEach(cache.get(baseDir).getJavas()::add);
                } catch (IOException e) {
                    log.error("find java error.", e);
                }
            }
        }
    }

    public static class Resource {
        private final XQLFileManager xqlFileManager = new XQLFileManager();
        private final Set<Path> javas = new HashSet<>();

        public XQLFileManager getXqlFileManager() {
            return xqlFileManager;
        }

        public Set<Path> getJavas() {
            return javas;
        }
    }
}
