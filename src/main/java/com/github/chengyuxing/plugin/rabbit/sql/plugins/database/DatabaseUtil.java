package com.github.chengyuxing.plugin.rabbit.sql.plugins.database;

import com.intellij.database.datagrid.DataRequest;
import com.intellij.database.view.ui.DataSourceManagerDialog;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DatabaseUtil {
    public static boolean executeSQL(String sql, DatasourceManager.Resource resource, DatasourceManager.DatabaseId databaseId) {
        var console = resource.getConsole(databaseId);
        if (console != null) {
            resource.setSelected(databaseId);
            var request = new CreateRequest(console, sql, DataRequest.newConstraints(), null);
            console.getMessageBus().getDataProducer().processRequest(request);
            return true;
        }
        return false;
    }

    public static class CreateRequest extends DataRequest.QueryRequest {
        protected CreateRequest(@NotNull Owner owner, @NotNull String query, @NotNull Constraints constraints, @Nullable Object params) {
            super(owner, query, constraints, params);
        }
    }

    public static void openDatasourceDialog(Project project) {
        DataSourceManagerDialog.showDialog(project, null, null);

    }
}
