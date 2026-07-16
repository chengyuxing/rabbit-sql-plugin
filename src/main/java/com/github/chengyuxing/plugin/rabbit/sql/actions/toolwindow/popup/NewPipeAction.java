package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.ui.NewPipeDialog;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.XqlConfig;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

public class NewPipeAction extends AnAction {
    private final JTree tree;

    public NewPipeAction(JTree tree) {
        super(MessageBundle.message("action.newPipe.text"), MessageBundle.message("action.newPipe.description"), AllIcons.Nodes.Function);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (nodeSource instanceof XqlConfig xqlConfig) {
            ApplicationManager.getApplication().invokeLater(() -> {
                var dialog = new NewPipeDialog(project, xqlConfig.config());
                dialog.showAndGet();
            });
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
