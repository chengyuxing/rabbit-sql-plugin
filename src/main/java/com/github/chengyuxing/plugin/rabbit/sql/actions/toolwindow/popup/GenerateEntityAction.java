package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.common.tuple.Quadruple;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.EntityGenerateDialog;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
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

public class GenerateEntityAction extends AnAction {
    private final JTree tree;

    public GenerateEntityAction(JTree tree) {
        super("Configure Params...", "Configure params and generate entity by sql params.", AllIcons.Actions.Compile);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (Objects.nonNull(nodeSource) && nodeSource.getType() == XqlTreeNodeData.Type.XQL_FRAGMENT) {
            @SuppressWarnings("unchecked") var sqlMeta = (Quadruple<String, String, XQLFileManager.Sql, XQLConfigManager.Config>) nodeSource.getSource();
            var alias = sqlMeta.getItem1();
            var sqlName = sqlMeta.getItem2();
            var sql = sqlMeta.getItem3().getContent();
            var config = sqlMeta.getItem4();
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
        if (Objects.nonNull(nodeSource) && nodeSource.getType() == XqlTreeNodeData.Type.XQL_FRAGMENT) {
            @SuppressWarnings("unchecked") var sqlMeta = (Quadruple<String, String, XQLFileManager.Sql, XQLConfigManager.Config>) nodeSource.getSource();
            var config = sqlMeta.getItem4();
            var sql = sqlMeta.getItem3().getContent();
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
