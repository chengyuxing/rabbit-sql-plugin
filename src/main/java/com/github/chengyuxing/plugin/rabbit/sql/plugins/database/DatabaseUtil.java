package com.github.chengyuxing.plugin.rabbit.sql.plugins.database;

import com.intellij.credentialStore.OneTimeString;
import com.intellij.database.access.DatabaseCredentials;
import com.intellij.database.access.DbCredentialManager;
import com.intellij.database.dataSource.DatabaseDriverImpl;
import com.intellij.database.dataSource.DatabaseDriverManager;
import com.intellij.database.dataSource.LocalDataSource;
import com.intellij.database.dataSource.LocalDataSourceManager;
import com.intellij.database.psi.DbPsiFacade;
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
//        LocalDataSource dataSource = new LocalDataSource(true);
//        dataSource.setUrl("jdbc:postgresql://127.0.0.1:5432/postgres?currentSchema=test");
//        dataSource.setUsername("chengyuxing");
//        DatabaseDriverManager.getInstance().getDrivers().forEach(driver -> {
//            System.out.println(driver);
//        });
//        dataSource.setName("chengyuxingoooooo");
//        dataSource.setPasswordStorage(LocalDataSource.Storage.PERSIST);
//        dataSource.setConfiguredByUrl(true);
//        dataSource.setDatabaseDriver(DatabaseDriverManager.getInstance().getDriver("postgresql"));
//        LocalDataSourceManager.getInstance(project).addDataSource(dataSource);
    }
}
