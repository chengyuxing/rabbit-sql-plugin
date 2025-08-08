package com.github.chengyuxing.plugin.rabbit.sql.plugins.database;

import com.github.chengyuxing.plugin.rabbit.sql.ui.datasource.CreateDatasourceDialog;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

public class DatabaseUtil {
    public static boolean executeSQL(String sql, DatasourceManager.Resource resource, DatabaseId databaseId) {
        return false;
    }

    public static void openDatasourceDialog(Project project) {
        ApplicationManager.getApplication().invokeLater(() -> {
            var dialog = new CreateDatasourceDialog(project);
            dialog.showAndGet();
        });
    }
}
