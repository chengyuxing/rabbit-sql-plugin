package com.github.chengyuxing.plugin.rabbit.sql;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.chengyuxing.plugin.rabbit.sql.XqlFileListenOnStartup.xqlFileManager;

public class XqlFileListener implements BulkFileListener {
    @Override
    public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
        Set<String> added = new HashSet<>();
        Set<String> deleted = new HashSet<>();
        events.stream().filter(vfe -> !vfe.isValid())
                .filter(vfe -> {
                    VirtualFile vf = vfe.getFile();
                    return vf != null && vf.getExtension() != null && vf.getExtension().equals("xql");
                }).forEach(vfe -> {
                    var vFile = vfe.getFile();
                    boolean valid = vFile.isValid();
                    if (valid) {
                        xqlFileManager.add(vFile.getUrl());
                        added.add(vFile.getName());
                    } else {
                        String alias = vFile.getNameWithoutExtension();
                        xqlFileManager.remove(alias);
                        deleted.add(vFile.getName());
                    }
                });
        xqlFileManager.init();
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
