package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow;

import com.github.chengyuxing.plugin.rabbit.sql.ui.XqlFileManagerToolWindow;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ShowAsTreeViewAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (Objects.isNull(e.getProject())) {
            return;
        }
        XqlFileManagerToolWindow.getXqlFileManagerPanel(e.getProject(), p -> {
            var tree = p.getTree();
            if (Objects.nonNull(tree)) {
                p.setTreeViewNodes(true);
                p.updateStates();
            }
        });
    }
}
