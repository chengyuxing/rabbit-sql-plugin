package com.github.chengyuxing.plugin.rabbit.sql;

import com.github.chengyuxing.plugin.rabbit.sql.common.ResourceCache;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

import static com.github.chengyuxing.plugin.rabbit.sql.common.Globals.openedProjects;

public class InitXqlConfigOnStartup implements StartupActivity {
    private static final Logger log = Logger.getInstance(InitXqlConfigOnStartup.class);

    @Override
    public void runActivity(@NotNull Project project) {
        log.debug("current project: " + project);
        if (!openedProjects.contains(project)) {
            openedProjects.add(project);
            ResourceCache.getInstance().initXqlFileManager(project);
        }
    }
}
