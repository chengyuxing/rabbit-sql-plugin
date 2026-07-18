package com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl;

import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.NodeData;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public record Folder(String title, Icon icon) implements NodeData {

    @Override
    public @NotNull String toString() {
        return title;
    }
}
