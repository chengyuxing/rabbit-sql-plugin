package com.github.chengyuxing.plugin.rabbit.sql;

import com.github.chengyuxing.plugin.rabbit.sql.common.DatasourceCache;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;

public class XqlConfigLifecycleListener implements ProjectManagerListener {
    @Override
    public void projectClosing(@NotNull Project project) {
        XQLConfigManager.getInstance().clear(project);
        DatasourceCache.getInstance().clear(project);
    }
}
