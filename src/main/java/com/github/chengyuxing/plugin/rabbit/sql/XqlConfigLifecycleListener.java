package com.github.chengyuxing.plugin.rabbit.sql;

import com.github.chengyuxing.common.utils.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.common.Store;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class XqlConfigLifecycleListener implements ProjectManagerListener {
    private static final Logger log = Logger.getInstance(XqlConfigLifecycleListener.class);

    @Override
    public void projectClosing(@NotNull Project project) {
        Store.INSTANCE.clearAll();
        Store.INSTANCE.projectJavas.clear();
        log.info("project closing, clear all cache! ");
    }

    @Override
    public void projectOpened(@NotNull Project project) {
        if (project.getBasePath() != null) {
            Path basePath = Path.of(project.getBasePath());
            Store.INSTANCE.basePath.set(basePath);
            Path src = basePath.resolve("src");

            if (Store.INSTANCE.xqlConfigExists()) {
                findJava2Cache(src);
                Store.INSTANCE.initXqlFiles((success, error) -> {
                    if (success) {
                        Notifications.Bus.notify(new Notification("Rabbit-SQL Notification Group", "XQL file manager", "XQL file Manager initialized!", NotificationType.INFORMATION));
                    } else {
                        Notifications.Bus.notify(new Notification("Rabbit-SQL Notification Group", "XQL file manager", error + "<br>Please change another name.", NotificationType.WARNING));
                    }
                });
                log.info("project opened, found xql config, init!");
            }
        }
    }

    private void findJava2Cache(Path rootPath) {
        if (Files.exists(rootPath)) {
            try (Stream<Path> pathStream = Files.find(rootPath, 15, (p, a) -> a.isRegularFile() && StringUtil.endsWiths(p.toString(), ".java", ".scala", ".kt"))) {
                pathStream.forEach(Store.INSTANCE.projectJavas::add);
            } catch (IOException e) {
                log.warn("find java error.", e);
            }
        }
    }
}
