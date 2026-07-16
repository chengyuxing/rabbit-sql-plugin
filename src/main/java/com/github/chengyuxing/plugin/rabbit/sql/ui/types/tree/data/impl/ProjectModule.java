package com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl;

import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.NodeData;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public record ProjectModule(Path module) implements NodeData {
    @Override
    public @NotNull String toString() {
        return module.getFileName().toString();
    }
}
