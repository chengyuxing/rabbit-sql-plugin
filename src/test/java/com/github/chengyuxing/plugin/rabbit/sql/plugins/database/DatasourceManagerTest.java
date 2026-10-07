package com.github.chengyuxing.plugin.rabbit.sql.plugins.database;

import com.intellij.database.console.JdbcConsole;
import com.intellij.database.console.session.DatabaseSessionManager;
import com.intellij.database.dataSource.DatabaseConnectionPoint;
import com.intellij.database.dataSource.LocalDataSource;
import com.intellij.database.dataSource.LocalDataSourceManager;
import com.intellij.testFramework.HeavyPlatformTestCase;

public class DatasourceManagerTest extends HeavyPlatformTestCase {
    public void testPluginConsoleKeepsUserConsoleAndSessionUntouched() {
        var dataSources = LocalDataSourceManager.getInstance(getProject());
        var dataSource = LocalDataSource.create("test", "org.h2.Driver", "jdbc:h2:mem:console_isolation", null);
        dataSources.addDataSource(dataSource);
        var userSession = DatabaseSessionManager.openSession(getProject(),
                (DatabaseConnectionPoint) dataSource.getConnectionConfig(), "User Console");
        userSession.setAutoCommit(true);
        var userConsole = JdbcConsole.newConsole(getProject()).fromDataSource(dataSource).useSession(userSession).build();
        var manager = new DatasourceManager(getProject());
        try {
            var id = DatabaseId.of(dataSource.getName(), dataSource.getUniqueId());
            var pluginConsole = manager.getResource().getConsole(id);
            assertNotNull(pluginConsole);
            assertNotSame(userConsole, pluginConsole);
            assertNotSame(userSession, pluginConsole.getSession());
            assertNotSame(userConsole.getVirtualFile(), pluginConsole.getVirtualFile());
            assertSame(pluginConsole, manager.getResource().getConsole(id));
            assertEquals("User Console", userSession.getTitle());
            manager.dispose();
            assertTrue(userConsole.isValid());
        } finally {
            manager.dispose();
            userConsole.dispose();
            dataSources.removeDataSource(dataSource);
        }
    }
}
