package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.github.chengyuxing.plugin.rabbit.sql.Helper;
import com.github.chengyuxing.plugin.rabbit.sql.file.XqlIcons;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.XqlFileManagerPanel;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

public class XqlFileManagerToolWindow implements ToolWindowFactory, DumbAware {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        var xqlFileManagerToolPanel = new XqlFileManagerPanel(true, project);
        var content = ContentFactory.getInstance().createContent(xqlFileManagerToolPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    @Override
    public void init(@NotNull ToolWindow toolWindow) {
        toolWindow.setTitle("XQL File Manager");
        toolWindow.setHelpId(Helper.XQL_FILE_MANAGER);
        toolWindow.setIcon(XqlIcons.XQL_FILE_MANAGER_TOOL_WINDOW);
    }

    public static void getXqlFileManagerPanel(Project project, Consumer<XqlFileManagerPanel> consumer) {
        var tlm = ToolWindowManager.getInstance(project).getToolWindow("XQL File Manager");
        if (Objects.isNull(tlm)) {
            return;
        }
        var content = tlm.getContentManager().getContent(0);
        if (Objects.isNull(content)) {
            return;
        }
        var xqlFileManagerPanel = (XqlFileManagerPanel) content.getComponent();
        consumer.accept(xqlFileManagerPanel);
    }
}
