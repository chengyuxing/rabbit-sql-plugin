package com.github.chengyuxing.plugin.rabbit.sql.plugins.database;

import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.util.Map;
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

    public static class Resource implements AutoCloseable {
        private final Project project;
        private DatabaseId selected;

        public Resource(Project project) {
            this.project = project;
        }

        public void setSelected(DatabaseId selected) {
            this.selected = selected;
        }

        public DatabaseId getSelected() {
            return selected;
        }

        public Map<DatabaseId, Icon> getConfiguredDatabases() {
            return Map.of();
        }

        @Override
        public void close() {
        }
    }
}
