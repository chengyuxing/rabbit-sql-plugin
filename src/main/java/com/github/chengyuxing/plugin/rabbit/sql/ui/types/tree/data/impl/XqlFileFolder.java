package com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.NodeData;
import org.jetbrains.annotations.NotNull;

public record XqlFileFolder(XQLConfigManager.Config config, String title) implements NodeData {
    @Override
    public @NotNull String toString() {
        return title;
    }
}
