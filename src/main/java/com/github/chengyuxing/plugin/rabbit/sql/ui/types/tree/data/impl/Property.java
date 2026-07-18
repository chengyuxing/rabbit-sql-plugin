package com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.NodeData;
import org.jetbrains.annotations.NotNull;

public record Property(String key, String value, XQLConfigManager.Config config) implements NodeData {
    @Override
    public @NotNull String toString() {
        return key;
    }
}
