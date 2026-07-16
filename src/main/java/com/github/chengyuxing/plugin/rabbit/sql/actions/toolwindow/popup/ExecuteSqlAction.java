package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.ui.DynamicSqlCalcDialog;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.SqlFragment;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.github.chengyuxing.sql.XQLFileManager;
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
            if (nodeSource instanceof SqlFragment sqlFragment) {
                var name = sqlFragment.sqlName();
                return MessageBundle.message("action.executeSql.text", name);
            }
            return MessageBundle.message("action.executeSql.text.default");
        }, () -> MessageBundle.message("action.executeSql.description.default"), AllIcons.Actions.Execute);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (nodeSource instanceof SqlFragment sqlFragment) {
            var alias = sqlFragment.xqlAlias();
            var name = sqlFragment.sqlName();
            var config = sqlFragment.config();
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
        if (nodeSource instanceof SqlFragment) {
            e.getPresentation().setEnabled(true);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
