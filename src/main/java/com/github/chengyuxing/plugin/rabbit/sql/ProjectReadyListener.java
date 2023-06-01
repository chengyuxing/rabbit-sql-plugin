package com.github.chengyuxing.plugin.rabbit.sql;

import com.github.chengyuxing.plugin.rabbit.sql.common.ResourceCache;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

public class ProjectReadyListener implements DumbService.DumbModeListener {

    @Override
    public void exitDumbMode() {
        var projects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : projects) {
            ResourceCache.getInstance().initXqlFileManager(project);
        }
    }
}
