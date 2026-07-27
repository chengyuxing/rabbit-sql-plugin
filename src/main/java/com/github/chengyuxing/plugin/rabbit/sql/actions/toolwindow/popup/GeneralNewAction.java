package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.ui.NewConstantDialog;
import com.github.chengyuxing.plugin.rabbit.sql.ui.NewPipeDialog;
import com.github.chengyuxing.plugin.rabbit.sql.ui.NewXqlDialog;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.XqlTreeNode;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.NodeData;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.Folder;
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

public class GeneralNewAction extends AnAction {
    private final JTree tree;

    public GeneralNewAction(JTree tree) {
        super(MessageBundle.message("action.folder.new.text"), MessageBundle.message("action.folder.new.description"), AllIcons.Actions.AddMulticaret);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.isNull(project)) return;
        var selected = tree.getSelectionPath();
        if (Objects.isNull(selected)) return;
        if (selected.getLastPathComponent() instanceof XqlTreeNode node &&
                node.getUserObject() instanceof NodeData nodeData) {
            if (nodeData instanceof Folder folder) {
                var xqlConfig = SwingUtil.findParentObjectUntil(node, XqlConfig.class);
                if (xqlConfig == null) return;
                switch (folder.title()) {
                    case "files" -> {
                        ApplicationManager.getApplication().invokeLater(() -> {
                            var d = new NewXqlDialog(project, xqlConfig.config());
                            d.initContent();
                            d.showAndGet();
                        });
                    }
                    case "pipes" -> {
                        ApplicationManager.getApplication().invokeLater(() -> {
                            var dialog = new NewPipeDialog(project, xqlConfig.config());
                            dialog.showAndGet();
                        });
                    }
                    case "constants" -> {
                        ApplicationManager.getApplication().invokeLater(() -> {
                            var dialog = new NewConstantDialog(project, xqlConfig.config());
                            dialog.showAndGet();
                        });
                    }
                }
            }
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
