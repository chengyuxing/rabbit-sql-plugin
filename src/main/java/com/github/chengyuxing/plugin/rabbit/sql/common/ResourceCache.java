package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.github.chengyuxing.plugin.rabbit.sql.util.NotificationUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.PathUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.XqlUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.XQLFileManagerConfig;
import com.github.chengyuxing.sql.exceptions.YamlDeserializeException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.chengyuxing.plugin.rabbit.sql.util.XqlUtil.getModuleBaseDir;
import static com.github.chengyuxing.plugin.rabbit.sql.util.XqlUtil.getModuleBaseDirUnchecked;

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

    public Resource getResource(PsiElement element) {
        var key = PsiUtil.getModuleDir(element);
        if (key == null) return null;
        return cache.get(key);
    }

    public Resource getResource(Project project, VirtualFile virtualFile) {
        var key = PsiUtil.getModuleDir(project, virtualFile);
        if (key == null) return null;
        return cache.get(key);
    }

    public void removeResource(Path xqlFileManagerLocation) {
        var key = getModuleBaseDir(xqlFileManagerLocation);
        // perhaps config is removed or renamed to others.
        if (key == null) {
            key = getModuleBaseDirUnchecked(xqlFileManagerLocation);
        }
        if (key == null) return;
        if (cache.containsKey(key)) {
            var resource = cache.remove(key);
            resource.close();
        }
    }

    public void removeResource(Project project, VirtualFile virtualFile) {
        var key = PsiUtil.getModuleDir(project, virtualFile);
        if (key == null) return;
        if (!cache.containsKey(key)) return;
        var res = cache.remove(key);
        res.close();
    }

    public Resource createResource(Project project, Path xqlFileManager) {
        var module = XqlUtil.getModuleBaseDir(xqlFileManager);
        if (module != null) {
            if (!cache.containsKey(module)) {
                var resource = new Resource(project, xqlFileManager);
                cache.put(module, resource);
            }
            return cache.get(module);
        }
        return null;
    }

    public static class Resource implements AutoCloseable {
        private final Path xqlFileManagerLocation;
        private final Path module;
        private final XQLFileManager xqlFileManager;
        private final NotificationExecutor notificationExecutor;

        public Resource(Project project, Path xqlFileManagerLocation) {
            this.xqlFileManagerLocation = xqlFileManagerLocation;
            this.module = PathUtil.backward(this.xqlFileManagerLocation, 4).getFileName();
            this.xqlFileManager = new XQLFileManager() {
                @Override
                protected void loadPipes() {
                    notificationExecutor.addMessage(Message.warning("[" + module + "] plugin is not support load custom pipes, but pipes worked on your project!"));
                }
            };
            this.notificationExecutor = new NotificationExecutor(messages ->
                    messages.forEach(m ->
                            NotificationUtil.showMessage(project, m.getText(), m.getType())), 1500);
        }

        private Set<Message> initXqlFileManager() {
            Set<Message> messages = new HashSet<>();
            try {
                var config = new XQLFileManagerConfig(xqlFileManagerLocation.toUri().toString());
                config.copyStateTo(xqlFileManager);
                Map<String, String> existsFiles = new HashMap<>();
                // classpath filename parse to filesystem path.
                for (Map.Entry<String, String> next : xqlFileManager.getFiles().entrySet()) {
                    var alias = next.getKey();
                    var v = next.getValue();
                    var path = v.trim();
                    if (!path.equals("")) {
                        var resourceRoot = xqlFileManagerLocation.getParent();
                        var abPath = resourceRoot.resolve(path);
                        if (Files.exists(abPath)) {
                            var uri = abPath.toUri().toString();
                            if (existsFiles.containsValue(uri)) {
                                messages.add(Message.warning("[" + path + "] already configured, do not again!"));
                            } else {
                                existsFiles.put(alias, uri);
                            }
                        } else {
                            messages.add(Message.warning("[" + path + "] not exists."));
                        }
                    }
                }
                xqlFileManager.clearFiles();
                xqlFileManager.setFiles(existsFiles);
                xqlFileManager.init();
                messages.add(Message.info("[" + module + "] xql resources updated!"));
            } catch (YamlDeserializeException e) {
                messages.add(Message.error("[" + module + "] xql-file-manager.yml config content invalid."));
                log.warn(e);
            } catch (Exception e) {
                messages.add(Message.error("[" + module + "] error:" + e.getMessage()));
                log.warn(e);
            }
            return messages;
        }

        public void fire(boolean showNotification) {
            var res = initXqlFileManager();
            if (showNotification) {
                notificationExecutor.addMessages(res);
            }
        }

        public void fire() {
            fire(false);
        }

        public XQLFileManager getXqlFileManager() {
            return xqlFileManager;
        }

        @Override
        public void close() {
            xqlFileManager.close();
            notificationExecutor.close();
        }
    }
}
