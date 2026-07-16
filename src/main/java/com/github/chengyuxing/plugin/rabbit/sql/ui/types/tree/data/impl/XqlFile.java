package com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.NodeData;
import com.github.chengyuxing.sql.XQLFileManager;
import org.jetbrains.annotations.NotNull;

public final class XqlFile implements NodeData {
    private final String alias;
    private final String classPathFileName;
    private final String absoluteFilePath;
    private final String description;
    private final XQLConfigManager.Config config;

    public XqlFile(String alias,
                   String classPathFileName,
                   XQLFileManager.Resource resource,
                   XQLConfigManager.Config config) {
        this.alias = alias;
        this.classPathFileName = classPathFileName;
        this.absoluteFilePath = resource.getFilename();
        this.description = resource.getDescription();
        this.config = config;
    }

    @Override
    public @NotNull String toString() {
        return alias;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof XqlFile xqlFile)) return false;

        return alias.equals(xqlFile.alias) && classPathFileName.equals(xqlFile.classPathFileName) && getAbsoluteFilePath().equals(xqlFile.getAbsoluteFilePath()) && getDescription().equals(xqlFile.getDescription()) && config.equals(xqlFile.config);
    }

    @Override
    public int hashCode() {
        int result = alias.hashCode();
        result = 31 * result + classPathFileName.hashCode();
        result = 31 * result + getAbsoluteFilePath().hashCode();
        result = 31 * result + getDescription().hashCode();
        result = 31 * result + config.hashCode();
        return result;
    }

    public String alias() {
        return alias;
    }

    public String classPathFileName() {
        return classPathFileName;
    }

    public XQLConfigManager.Config config() {
        return config;
    }

    public String getAbsoluteFilePath() {
        return absoluteFilePath;
    }

    public String getDescription() {
        return description;
    }
}
