package com.github.chengyuxing.plugin.rabbit.sql;

import com.github.chengyuxing.plugin.rabbit.sql.common.Store;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class XqlFileListener implements BulkFileListener {
    @Override
    public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
        Set<String> added = new HashSet<>();
        Set<String> deleted = new HashSet<>();
        events.stream().filter(vfe -> !vfe.isValid())
                .filter(vfe -> {
                    VirtualFile vf = vfe.getFile();
                    return vf != null && vf.getExtension() != null;
                }).forEach(vfe -> {
                    var vFile = vfe.getFile();
                    if (Objects.equals(vFile.getExtension(), "xql")) {
                        boolean valid = vFile.isValid();
                        if (valid) {
                            Store.INSTANCE.xqlFileManager.add(vFile.getUrl());
                            added.add(vFile.getName());
                        } else {
                            String alias = vFile.getNameWithoutExtension();
                            Store.INSTANCE.xqlFileManager.remove(alias);
                            deleted.add(vFile.getName());
                        }
                    } else if (Objects.equals(vFile.getExtension(), "java")) {
                        boolean valid = vFile.isValid();
                        if (valid) {
                            Store.INSTANCE.projectJavas.add(vFile.toNioPath());
                        }
                    }
                });
        Store.INSTANCE.xqlFileManager.init();
        // if file location changed, delete invalid file path.
        Store.INSTANCE.refreshJavaFile();

        String message = "";
        if (!added.isEmpty()) {
            message = String.join(", ", added) + " updated!";
        }
        if (!deleted.isEmpty()) {
            if (!message.equals("")) {
                message += "\n";
            }
            message += String.join(", ", deleted) + " deleted!";
        }
        if (!message.equals("")) {
            Notifications.Bus.notify(new Notification("Rabbit-SQL Notification Group", "XQL file manager", message, NotificationType.INFORMATION));
        }
    }
}
