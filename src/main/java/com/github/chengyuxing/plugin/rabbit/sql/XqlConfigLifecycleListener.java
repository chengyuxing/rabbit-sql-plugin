package com.github.chengyuxing.plugin.rabbit.sql;

import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.ResourceCache;
import com.github.chengyuxing.plugin.rabbit.sql.util.XqlUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.roots.ProjectRootManager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class XqlConfigLifecycleListener implements ProjectManagerListener {
    private static final Logger log = Logger.getInstance(XqlConfigLifecycleListener.class);
    private static final Set<Project> openedProjects = new HashSet<>();

    @Override
    public void projectClosing(@NotNull Project project) {
        openedProjects.remove(project);
        Arrays.stream(ProjectRootManager.getInstance(project).getContentSourceRoots()).forEach(vf -> {
            var xqlFileManager = vf.toNioPath().resolve(Constants.CONFIG_NAME);
            if (XqlUtil.xqlFileManagerExists(xqlFileManager)) {
                ResourceCache resourceCache = ResourceCache.getInstance();
                resourceCache.clear(xqlFileManager);
                log.info("clear cache of relation: " + xqlFileManager);
            }
        });
    }

    @Override
    public void projectOpened(@NotNull Project project) {
        // e.g
        // file:///Users/chengyuxing/IdeaProjects/sbp-test1/src/test/java
        // file:///Users/chengyuxing/IdeaProjects/sbp-test1/src/main/resources
        // file:///Users/chengyuxing/IdeaProjects/sbp-test1/src/main/java
        if (!openedProjects.contains(project)) {
            openedProjects.add(project);
            Arrays.stream(ProjectRootManager.getInstance(project).getContentSourceRoots()).forEach(vf -> {
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
    }
}
