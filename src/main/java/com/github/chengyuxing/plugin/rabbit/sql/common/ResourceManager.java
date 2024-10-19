package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.intellij.openapi.project.Project;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ResourceManager {
    private static volatile ResourceManager instance;
    private final Map<Project, Resource> cache = new ConcurrentHashMap<>();

    private ResourceManager() {
    }

    public static ResourceManager getInstance() {
        if (instance == null) {
            synchronized (ResourceManager.class) {
                if (instance == null) {
                    instance = new ResourceManager();
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

    public static final class Resource implements AutoCloseable {
        private final Project project;
        private final Map<String, Object> dynamicSqlParamHistory;

        public Resource(Project project) {
            this.project = project;
            this.dynamicSqlParamHistory = new HashMap<>();
        }

        public Map<String, Object> getDynamicSqlParamHistory() {
            return dynamicSqlParamHistory;
        }

        public Project getProject() {
            return project;
        }

        @Override
        public void close() {
            dynamicSqlParamHistory.clear();
        }
    }
}
