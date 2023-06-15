package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.intellij.database.console.JdbcConsole;
import com.intellij.database.console.session.DatabaseSessionManager;
import com.intellij.database.dataSource.DatabaseConnectionPoint;
import com.intellij.database.dataSource.LocalDataSourceManager;
import com.intellij.database.model.DasDataSource;
import com.intellij.database.psi.DataSourceManager;
import com.intellij.openapi.project.Project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DatasourceCache {
    private static volatile DatasourceCache instance;
    /**
     * key: project dir, value: database resource which be owned by project
     */
    private final Map<Project, Resource> cache = new ConcurrentHashMap<>();

    private DatasourceCache() {
    }

    public static DatasourceCache getInstance() {
        if (instance == null) {
            synchronized (DatasourceCache.class) {
                if (instance == null) {
                    instance = new DatasourceCache();
                }
            }
        }
        return instance;
    }

    public void clear(Project project) {
        if (project == null) return;
        if (cache.containsKey(project)) {
            var resource = cache.remove(project);
            resource.paramsHistory.clear();
            resource.consoles.forEach((k, v) -> v.dispose());
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

    public static class Resource {
        private final Project project;
        private final Map<String, JdbcConsole> consoles;
        private final Map<String, Object> paramsHistory;

        public Resource(Project project) {
            this.project = project;
            this.consoles = new HashMap<>();
            this.paramsHistory = new HashMap<>();
        }

        public Map<String, Object> getParamsHistory() {
            return paramsHistory;
        }

        public JdbcConsole getConsole(String name) {
            if (name == null) return null;
            if (consoles.containsKey(name)) {
                var console = consoles.get(name);
                if (!console.isValid()) {
                    consoles.remove(name);
                }
            }
            if (!consoles.containsKey(name)) {
                DataSourceManager.getManagers(project).stream()
                        .filter(dsm -> dsm instanceof LocalDataSourceManager)
                        .findFirst()
                        .ifPresent(dsm -> {
                            var dss = dsm.getDataSources();
                            for (var ds : dss) {
                                var cfg = ds.getConnectionConfig();
                                if (cfg != null) {
                                    var k = ds.getName();
                                    if (name.equals(k)) {
                                        var session = DatabaseSessionManager.openSession(project, (DatabaseConnectionPoint) cfg, "Rabbit-SQ-Plugin");
                                        var v = JdbcConsole.newConsole(project)
                                                .fromDataSource(ds)
                                                .useSession(session)
                                                .build();
                                        consoles.put(k, v);
                                        break;
                                    }
                                }
                            }
                        });
            }
            if (consoles.containsKey(name)) {
                return consoles.get(name);
            }
            return null;
        }

        public List<String> getConfiguredDatabases() {
            return DataSourceManager.getManagers(project).stream()
                    .filter(dsm -> dsm instanceof LocalDataSourceManager)
                    .flatMap(dsm -> dsm.getDataSources().stream())
                    .filter(ds -> ds.getConnectionConfig() != null)
                    .map(DasDataSource::getName)
                    .collect(Collectors.toList());
        }
    }
}
