package com.github.chengyuxing.plugin.rabbit.sql.plugins.database;

import com.intellij.database.run.ConsoleDataRequest;
import com.intellij.database.util.DbImplUtil;
import com.intellij.database.view.ui.DataSourceManagerDialog;
import com.intellij.openapi.project.Project;

public class DatabaseUtil {
    public static boolean executeSQL(String sql, DatasourceManager.Resource resource, DatabaseId databaseId) {
        var console = resource.getConsole(databaseId);
        if (console != null) {
            resource.setSelected(databaseId);
            var request = ConsoleDataRequest.newRequest(console, sql, DbImplUtil.getDbms(console));
            console.getMessageBus().getDataProducer().processRequest(request);
            return true;
        }
        return false;
    }

    public static void openDatasourceDialog(Project project) {
        DataSourceManagerDialog.showDialog(project, null, null);

    }
}
