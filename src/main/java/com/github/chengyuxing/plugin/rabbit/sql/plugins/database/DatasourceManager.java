package com.github.chengyuxing.plugin.rabbit.sql.plugins.database;

import com.intellij.database.console.JdbcConsole;
import com.intellij.database.console.session.DatabaseSessionManager;
import com.intellij.database.dataSource.LocalDataSourceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;

import javax.swing.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class DatasourceManager {
    private static volatile DatasourceManager instance;
    /**
     * key: project dir, value: database resource which be owned by project
     */
    private final Map<Project, Resource> cache = new ConcurrentHashMap<>();

    private DatasourceManager() {
    }

    public static DatasourceManager getInstance() {
        if (instance == null) {
            synchronized (DatasourceManager.class) {
                if (instance == null) {
                    instance = new DatasourceManager();
                }
            }
        }
        return instance;
    }

    public void clear(Project project) {
        if (project == null) return;
        if (cache.containsKey(project)) {
            var resource = cache.remove(project);
            resource.close();
        }
    }

    public Resource getResource(Project project) {
        if (project == null) {
            return null;
        }
        if (!cache.containsKey(project)) {
            cache.put(project, new Resource(project));
        }
        return cache.get(project);
    }

    public static class DatabaseId {
        private final String name;
        private final String id;

        public DatabaseId(String name, String id) {
            this.name = name;
            this.id = id;
        }

        public static DatabaseId of(String name, String id) {
            return new DatabaseId(name, id);
        }

        public static DatabaseId empty(String placeholder) {
            return of(placeholder, "");
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public final boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DatabaseId)) return false;

            DatabaseId that = (DatabaseId) o;
            return Objects.equals(getName(), that.getName()) && Objects.equals(getId(), that.getId());
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(getName());
            result = 31 * result + Objects.hashCode(getId());
            return result;
        }
    }

    public static class Resource implements AutoCloseable {
        private final Project project;
        private final Map<DatabaseId, JdbcConsole> consoles;
        private final Map<String, Object> paramsHistory;
        private DatabaseId selected;

        public Resource(Project project) {
            this.project = project;
            this.consoles = new HashMap<>();
            this.paramsHistory = new HashMap<>();
        }

        public Map<String, Object> getParamsHistory() {
            return paramsHistory;
        }

        public void setSelected(DatabaseId selected) {
            this.selected = selected;
        }

        public DatabaseId getSelected() {
            return selected;
        }

        public JdbcConsole getConsole(DatabaseId id) {
            if (id == null) return null;
            if (consoles.containsKey(id)) {
                var console = consoles.get(id);
                if (!console.isValid()) {
                    consoles.remove(id);
                }
            }
            if (!consoles.containsKey(id)) {
                var dss = LocalDataSourceManager.getInstance(project).getDataSources();
                for (var ds : dss) {
                    var cfg = ds.getConnectionConfig();
                    if (Objects.isNull(cfg)) {
                        continue;
                    }
                    if (!Objects.equals(id, DatabaseId.of(ds.getName(), ds.getUniqueId()))) {
                        continue;
                    }
                    var console = JdbcConsole.getActiveConsoles(project)
                            .stream()
                            .filter(c -> c.getDataSource() == ds)
                            .findFirst()
                            .map(c -> {
                                var session = c.getSession();
                                session.setAutoCommit(false);
                                session.setTitle("Rabbit-SQL-Plugin");
                                return c;
                            }).orElseGet(() -> {
                                var session = DatabaseSessionManager.getSession(project, ds, "Rabbit-SQL-Plugin");
                                session.setAutoCommit(false);
                                return JdbcConsole.newConsole(project)
                                        .fromDataSource(ds)
                                        .useSession(session)
                                        .build();
                            });
                    consoles.put(id, console);
                    break;
                }
            }
            if (consoles.containsKey(id)) {
                return consoles.get(id);
            }
            return null;
        }

        public Map<DatabaseId, Icon> getConfiguredDatabases() {
            Map<DatabaseId, Icon> dsInfo = new LinkedHashMap<>();
            LocalDataSourceManager.getInstance(project)
                    .getDataSources()
                    .stream()
                    .filter(ds -> Objects.nonNull(ds.getConnectionConfig()))
                    .forEach(ds -> dsInfo.put(DatabaseId.of(ds.getName(), ds.getUniqueId()), ds.getIcon(Iconable.ICON_FLAG_VISIBILITY)));
            return dsInfo;
        }

        @Override
        public void close() {
            consoles.forEach((i, c) -> c.dispose());
            paramsHistory.clear();
        }
    }
}
