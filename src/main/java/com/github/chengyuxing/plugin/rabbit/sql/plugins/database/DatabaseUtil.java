package com.github.chengyuxing.plugin.rabbit.sql.plugins.database;

import com.intellij.database.run.ConsoleDataRequest;
import com.intellij.database.util.DbImplUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;

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
        var manager = ToolWindowManager.getInstance(project);
        var dbToolWindow = manager.getToolWindow("DatabaseView");
        if (dbToolWindow == null) {
            dbToolWindow = manager.getToolWindow("Database");
        }
        if (dbToolWindow != null) {
            dbToolWindow.show(null);
        }
    }
}
