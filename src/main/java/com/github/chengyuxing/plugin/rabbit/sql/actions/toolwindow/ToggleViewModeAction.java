package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow;

import com.github.chengyuxing.plugin.rabbit.sql.ui.XqlFileManagerToolWindow;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ToggleViewModeAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (Objects.isNull(e.getProject())) {
            return;
        }
        XqlFileManagerToolWindow.getXqlFileManagerPanel(e.getProject(), p -> {
            var tree = p.getTree();
            if (Objects.nonNull(tree)) {
                p.setTreeViewNodes(!p.isTreeViewNodes());
                p.updateStates();
            }
        });
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        if (Objects.isNull(e.getProject())) {
            return;
        }
        XqlFileManagerToolWindow.getXqlFileManagerPanel(e.getProject(), p -> {
            var tree = p.getTree();
            if (Objects.nonNull(tree)) {
                var icon = AllIcons.Actions.ListFiles;
                var title = "Toggle 'Tree View' Mode";
                if (p.isTreeViewNodes()) {
                    icon = AllIcons.Actions.ShowAsTree;
                    title = "Toggle 'Flatten' Mode";
                }
                e.getPresentation().setIcon(icon);
                e.getPresentation().setText(title);
            }
        });
    }
}
