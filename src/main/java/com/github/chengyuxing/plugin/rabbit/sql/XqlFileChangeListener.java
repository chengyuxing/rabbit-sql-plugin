package com.github.chengyuxing.plugin.rabbit.sql;

import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.Store;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class XqlFileChangeListener implements BulkFileListener {
    @Override
    public void before(@NotNull List<? extends @NotNull VFileEvent> events) {
        events.stream().filter(vfe -> {
            VirtualFile vf = vfe.getFile();
            return vf != null && vf.getExtension() != null;
        }).forEach(vfe -> {
            var vFile = vfe.getFile();
            if (vFile.getName().equals(Constants.CONFIG_NAME)) {
                Store.INSTANCE.clearAll();
            }
        });
    }

    @Override
    public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
        AtomicBoolean xqlConfigChanged = new AtomicBoolean(false);
        AtomicBoolean xqlFileChanged = new AtomicBoolean(false);
        AtomicBoolean javaFileChanged = new AtomicBoolean(false);
        events.stream().filter(vfe -> {
            VirtualFile vf = vfe.getFile();
            return vf != null && vf.getExtension() != null;
        }).forEach(vfe -> {
            var vFile = vfe.getFile();
            if (vFile.getName().equals(Constants.CONFIG_NAME)) {
                xqlConfigChanged.set(true);
            } else if (Objects.equals(vFile.getExtension(), "xql")) {
                xqlFileChanged.set(true);
            } else if (Objects.equals(vFile.getExtension(), "java")) {
                javaFileChanged.set(true);
                boolean valid = vFile.isValid();
                if (valid) {
                    Store.INSTANCE.projectJavas.add(vFile.toNioPath());
                }
            }
        });
        if (xqlConfigChanged.get() || xqlFileChanged.get()) {
            Store.INSTANCE.initXqlFiles((success, error) -> {
                if (!success) {
                    Notifications.Bus.notify(new Notification("Rabbit-SQL Notification Group", "XQL file manager", error, NotificationType.WARNING));
                }
            });
        }
        if (javaFileChanged.get()) {
            // if file location changed, delete invalid file path.
            Store.INSTANCE.refreshJavaFiles();
        }
    }
}
