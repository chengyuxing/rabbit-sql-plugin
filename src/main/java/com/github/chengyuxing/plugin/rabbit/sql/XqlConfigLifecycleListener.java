package com.github.chengyuxing.plugin.rabbit.sql;

import com.github.chengyuxing.plugin.rabbit.sql.common.ResourceManager;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.database.DatasourceManager;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.FeatureChecker;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;

public class XqlConfigLifecycleListener implements ProjectManagerListener {
    @Override
    public void projectClosing(@NotNull Project project) {
        XQLConfigManager.getInstance().clear(project);
        ResourceManager.getInstance().clear(project);
        if (FeatureChecker.isPluginEnabled(FeatureChecker.DATABASE_PLUGIN_ID)) {
            DatasourceManager.getInstance().clear(project);
        }
    }
}
