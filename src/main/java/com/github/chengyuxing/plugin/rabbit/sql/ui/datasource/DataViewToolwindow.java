package com.github.chengyuxing.plugin.rabbit.sql.ui.datasource;

import com.github.chengyuxing.plugin.rabbit.sql.file.XqlIcons;
import com.github.chengyuxing.plugin.rabbit.sql.ui.datasource.components.DataViewToolwindowPanel;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DataViewToolwindow implements ToolWindowFactory, DumbAware {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        var dataViewToolwindowPanel = new DataViewToolwindowPanel(project);
        var content = ContentFactory.getInstance().createContent(dataViewToolwindowPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    @Override
    public void init(@NotNull ToolWindow toolWindow) {
        toolWindow.setTitle("XQL Data Viewer");
        toolWindow.setIcon(XqlIcons.XQL_FILE_MANAGER_TOOL_WINDOW);
        toolWindow.setTitleActions(List.of(
        ));
    }
}
