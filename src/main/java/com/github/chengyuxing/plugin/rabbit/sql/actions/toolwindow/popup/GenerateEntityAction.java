package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.ui.EntityGenerateDialog;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.SqlFragment;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

public class GenerateEntityAction extends AnAction {
    private final JTree tree;

    public GenerateEntityAction(JTree tree) {
        super(MessageBundle.message("action.generateEntity.text"), MessageBundle.message("action.generateEntity.description"), AllIcons.Actions.Compile);
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
            var sqlName = sqlFragment.sqlName();
            var sql = sqlFragment.sql().getSource();
            var config = sqlFragment.config();
            var fieldMapping = StringUtil.getParamsMappingInfo(config.getSqlGenerator(), sql);
            if (fieldMapping.isEmpty()) {
                return;
            }
            ApplicationManager.getApplication().invokeLater(() -> new EntityGenerateDialog(project, alias, sqlName, config, fieldMapping).showAndGet());
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(false);
        var project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (nodeSource instanceof SqlFragment sqlFragment) {
            var config = sqlFragment.config();
            var sql = sqlFragment.sql().getSource();
            var paramsCount = (long) StringUtil.getParamsMappingInfo(config.getSqlGenerator(), sql).size();
            if (paramsCount > 0) {
                e.getPresentation().setEnabled(true);
            }
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
