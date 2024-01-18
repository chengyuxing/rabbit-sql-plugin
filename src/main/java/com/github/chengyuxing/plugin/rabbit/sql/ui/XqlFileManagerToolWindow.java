package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.github.chengyuxing.plugin.rabbit.sql.file.XqlIcons;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class XqlFileManagerToolWindow implements ToolWindowFactory, DumbAware {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.getComponent().add(new JButton("Hello world!"));
    }

    @Override
    public void init(@NotNull ToolWindow toolWindow) {
        toolWindow.setIcon(XqlIcons.XQL_FILE_MANAGER);
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return false;
    }
}
