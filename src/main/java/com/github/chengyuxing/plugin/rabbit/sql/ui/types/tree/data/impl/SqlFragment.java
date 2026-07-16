package com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.NodeData;
import com.github.chengyuxing.sql.XQLFileManager;
import org.jetbrains.annotations.NotNull;

public record SqlFragment(String xqlAlias,
                          String sqlName,
                          XQLFileManager.Sql sql,
                          XQLConfigManager.Config config) implements NodeData {
    @Override
    public @NotNull String toString() {
        return sqlName;
    }
}
