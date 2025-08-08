package com.github.chengyuxing.plugin.rabbit.sql.plugins.database;

import com.intellij.database.run.ConsoleDataRequest;
import com.intellij.database.util.DbImplUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;

import java.lang.reflect.InvocationTargetException;

public class DatabaseUtil {
    private static final Logger log = Logger.getInstance(DatabaseUtil.class);

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

    public static void openDatasourceDialog(Project project, Runnable fail) {
        try {
            openV2023DatasourceDialog(project);
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException |
                 NoSuchMethodException e) {
            log.warn("invoke DataSourceManagerDialog#showDialog error", e);
            toggleDatasourceToolwindow(project);
            fail.run();
        }
    }

    private static void openV2023DatasourceDialog(Project project) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        var datasourceDialogClass = Class.forName("com.intellij.database.view.ui.DataSourceManagerDialog");
        var crediClass = Class.forName("com.intellij.database.access.DatabaseCredentials");
        var showMethod = datasourceDialogClass.getMethod("showDialog", Project.class, Object.class, crediClass);
        showMethod.invoke(datasourceDialogClass, project, null, null);
    }

    private static void toggleDatasourceToolwindow(Project project) {
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
