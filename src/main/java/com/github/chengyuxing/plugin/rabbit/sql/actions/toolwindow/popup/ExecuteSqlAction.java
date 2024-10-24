package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.common.tuple.Quadruple;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.DynamicSqlCalcDialog;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.utils.SqlUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

public class ExecuteSqlAction extends AnAction {
    private final JTree tree;

    public ExecuteSqlAction(JTree tree) {
        super(() -> {
            var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
            if (Objects.nonNull(nodeSource)) {
                if (nodeSource.type() == XqlTreeNodeData.Type.XQL_FRAGMENT) {
                    @SuppressWarnings("unchecked")
                    var data = (Quadruple<String, String, XQLFileManager.Sql, XQLConfigManager.Config>) nodeSource.source();
                    var name = data.getItem2();
                    return "Execute " + SqlUtil.safeQuote(name);
                }
            }
            return "Execute";
        }, () -> "Execute dynamic sql.", AllIcons.Actions.Execute);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (Objects.nonNull(nodeSource) && nodeSource.type() == XqlTreeNodeData.Type.XQL_FRAGMENT) {
            @SuppressWarnings("unchecked")
            var sqlMeta = (Quadruple<String, String, XQLFileManager.Sql, XQLConfigManager.Config>) nodeSource.source();
            var alias = sqlMeta.getItem1();
            var name = sqlMeta.getItem2();
            var config = sqlMeta.getItem4();
            ApplicationManager.getApplication().invokeLater(() -> {
                var dialog = new DynamicSqlCalcDialog(XQLFileManager.encodeSqlReference(alias, name), config, project);
                dialog.showAndGet();
            });
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(false);
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (Objects.nonNull(nodeSource) && nodeSource.type() == XqlTreeNodeData.Type.XQL_FRAGMENT) {
            e.getPresentation().setEnabled(true);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
