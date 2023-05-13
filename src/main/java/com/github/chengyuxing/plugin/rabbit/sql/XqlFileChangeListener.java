package com.github.chengyuxing.plugin.rabbit.sql;

import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.ResourceCache;
import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class XqlFileChangeListener implements BulkFileListener {
    private static final Logger log = Logger.getInstance(XqlFileChangeListener.class);

    @Override
    public void before(@NotNull List<? extends @NotNull VFileEvent> events) {
        events.stream().filter(vfe -> {
            VirtualFile vf = vfe.getFile();
            return vf != null && vf.getExtension() != null;
        }).forEach(vfe -> {
            var vFile = vfe.getFile();
            if (vFile.getName().equals(Constants.CONFIG_NAME)) {
                ResourceCache resourceCache = ResourceCache.getInstance();
                resourceCache.clear(vFile.toNioPath());
                log.warn(vFile.toNioPath() + ", xql resource cache cleared!");
            }
        });
    }

    @Override
    public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
        AtomicReference<VirtualFile> xqlConfig = new AtomicReference<>(null);
        List<VirtualFile> xqlFiles = new ArrayList<>();
        List<VirtualFile> javas = new ArrayList<>();

        events.stream().filter(vfe -> {
            VirtualFile vf = vfe.getFile();
            return vf != null && vf.getExtension() != null;
        }).forEach(vfe -> {
            var vFile = vfe.getFile();
            var ext = vFile.getExtension();
            if (vFile.getName().equals(Constants.CONFIG_NAME)) {
                xqlConfig.set(vFile);
            } else if (Objects.equals(ext, "xql")) {
                xqlFiles.add(vFile);
            } else if (Objects.equals(ext, "java") || Objects.equals(ext, "kt") || Objects.equals(ext, "scala")) {
                boolean valid = vFile.isValid();
                if (valid) {
                    javas.add(vFile);
                }
            }
        });

        ResourceCache resourceCache = ResourceCache.getInstance();

        if (xqlConfig.get() != null) {
            log.debug(Constants.CONFIG_NAME + " changed.");
            resourceCache.initXqlFileManager(xqlConfig.get().toNioPath(), (success, msg) -> {
                if (!success) {
                    Notifications.Bus.notify(new Notification("Rabbit-SQL Notification Group", "XQL file manager", msg, NotificationType.WARNING));
                    log.debug("reload xql file manager failed!");
                } else {
                    log.debug("reload xql file manager success!");
                }
            });
        }

        if (!xqlFiles.isEmpty()) {
            xqlFiles.forEach(vf -> Stream.of(ProjectManager.getInstance().getOpenProjects())
                    .forEach(p -> {
                        var isBelongsProject = ProjectRootManager.getInstance(p).getFileIndex().isInContent(vf);
                        if (isBelongsProject) {
                            var baseDir = PsiUtil.getModuleDir(p, vf);
                            if (baseDir != null) {
                                log.debug("xql file changed: ", p, ": ", xqlFiles);
                                var xqlFileManager = baseDir.resolve(Constants.CONFIG_PATH);
                                resourceCache.initXqlFileManager(xqlFileManager, (success, msg) -> {
                                    if (!success) {
                                        Notifications.Bus.notify(new Notification("Rabbit-SQL Notification Group", "XQL file manager", msg, NotificationType.WARNING));
                                        log.debug("reload xql file manager failed!");
                                    } else {
                                        log.debug("reload xql file manager success!");
                                    }
                                });
                            }
                        }
                    }));
        }

        if (!javas.isEmpty()) {
            javas.forEach(vf -> Stream.of(ProjectManager.getInstance().getOpenProjects())
                    .forEach(p -> {
                        var isBelongsProject = ProjectRootManager.getInstance(p).getFileIndex().isInContent(vf);
                        if (isBelongsProject) {
                            var baseDir = PsiUtil.getModuleDir(p, vf);
                            if (baseDir != null) {
                                log.debug("add new java file to cache: ", p, ": ", vf);
                                var xqlFileManager = baseDir.resolve(Path.of("src", "main", "resources", Constants.CONFIG_NAME));
                                resourceCache.addJava(xqlFileManager, vf.toNioPath());
                                resourceCache.refreshJavas(xqlFileManager);
                                log.debug("refresh java file cache.");
                            }
                        }
                    }));
        }
    }
}
