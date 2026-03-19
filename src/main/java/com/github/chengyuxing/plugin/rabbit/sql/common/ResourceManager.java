package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(Service.Level.PROJECT)
public final class ResourceManager {
    private final Resource resource;

    ResourceManager() {
        this.resource = new Resource();
    }

    public static ResourceManager getInstance(Project project) {
        return project.getService(ResourceManager.class);
    }

    public Resource getResource() {
        return resource;
    }

    public static final class Resource {
        private final Map<String, Object> dynamicSqlParamHistory;
        private final List<String> historyList;

        public Resource() {
            this.dynamicSqlParamHistory = new HashMap<>();
            this.historyList = new ArrayList<>();
        }

        public Map<String, Object> getDynamicSqlParamHistory() {
            return dynamicSqlParamHistory;
        }

        public List<String> getHistoryList() {
            return historyList;
        }
    }
}
