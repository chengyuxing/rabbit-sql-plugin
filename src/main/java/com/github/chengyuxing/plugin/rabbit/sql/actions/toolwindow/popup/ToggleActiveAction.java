package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.plugin.rabbit.sql.ui.types.TreeNodeSource;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.XqlFileManagerToolWindow;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.XqlFileManagerPanel;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

public class ToggleActiveAction extends AnAction {
    private final XQLConfigManager xqlConfigManager = XQLConfigManager.getInstance();
    private final JTree tree;

    public ToggleActiveAction(JTree tree) {
        super("Toggle to Active", "Set selected as active.", AllIcons.Actions.Lightning);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (Objects.nonNull(nodeSource) && nodeSource.type() == TreeNodeSource.NodeSourceType.XQL_CONFIG) {
            var config = (XQLConfigManager.Config) nodeSource.source();
            if (config.isActive()) {
                return;
            }
            xqlConfigManager.toggleActive(project, config);
            config.fire(true);
            XqlFileManagerToolWindow.getXqlFileManagerPanel(project, XqlFileManagerPanel::updateStates);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(false);
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (Objects.nonNull(nodeSource) && nodeSource.type() == TreeNodeSource.NodeSourceType.XQL_CONFIG) {
            e.getPresentation().setEnabled(true);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
