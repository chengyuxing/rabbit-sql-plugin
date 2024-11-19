package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.common.tuple.Quadruple;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.NewSQLDialog;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

public class NewSQLAction extends AnAction {
    private final JTree tree;

    public NewSQLAction(JTree tree) {
        super("New", "Create a new SQL fragment.", AllIcons.Actions.AddMulticaret);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (Objects.nonNull(nodeSource) && nodeSource.getType() == XqlTreeNodeData.Type.XQL_FILE) {
            @SuppressWarnings("unchecked") var data = (Quadruple<String, String, String, XQLConfigManager.Config>) nodeSource.getSource();
            var alias = data.getItem1();
            var config = data.getItem4();
            ApplicationManager.getApplication().invokeLater(() -> {
                var d = new NewSQLDialog(project, alias, config);
                d.showAndGet();
            });
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (Objects.nonNull(nodeSource) && nodeSource.getType() == XqlTreeNodeData.Type.XQL_FILE) {
            @SuppressWarnings("unchecked") var data = (Quadruple<String, String, String, XQLConfigManager.Config>) nodeSource.getSource();
            var filename = data.getItem3();
            if (!ProjectFileUtil.isLocalFileUri(filename)) {
                e.getPresentation().setEnabled(false);
            }
        }
    }
}
