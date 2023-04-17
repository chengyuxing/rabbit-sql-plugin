package com.github.chengyuxing.plugin.rabbit.sql;

import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.ResourceCache;
import com.github.chengyuxing.plugin.rabbit.sql.util.XqlUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.roots.ProjectRootManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class XqlConfigLifecycleListener implements ProjectManagerListener {
    private static final Logger log = Logger.getInstance(XqlConfigLifecycleListener.class);
    private static final Set<Project> openedProjects = new HashSet<>();

    @Override
    public void projectClosing(@NotNull Project project) {
        openedProjects.remove(project);
        Stream.of(ProjectRootManager.getInstance(project).getContentSourceRoots()).forEach(vf -> {
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
        if (!openedProjects.contains(project)) {
            openedProjects.add(project);
            ResourceCache.getInstance().initXqlFileManager(project);
        }
    }
}
