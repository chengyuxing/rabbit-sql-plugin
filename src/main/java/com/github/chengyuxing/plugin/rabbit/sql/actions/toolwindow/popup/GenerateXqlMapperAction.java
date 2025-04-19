package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.common.tuple.Quadruple;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.MapperGenerateDialog;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

public class GenerateXqlMapperAction extends AnAction {
    private final JTree tree;

    public GenerateXqlMapperAction(JTree tree) {
        super("Generate Mapper...", "Generate mapper file of this xql file.", AllIcons.Actions.Compile);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (Objects.nonNull(nodeSource) && nodeSource.type() == XqlTreeNodeData.Type.XQL_FILE) {
            @SuppressWarnings("unchecked") var data = (Quadruple<String, String, String, XQLConfigManager.Config>) nodeSource.source();
            var alias = data.getItem1();
            var config = data.getItem4();
            ApplicationManager.getApplication().invokeLater(() -> new MapperGenerateDialog(project, alias, config).showAndGet());
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
        if (Objects.nonNull(nodeSource) && nodeSource.type() == XqlTreeNodeData.Type.XQL_FILE) {
            @SuppressWarnings("unchecked") var data = (Quadruple<String, String, String, XQLConfigManager.Config>) nodeSource.source();
            var config = data.getItem4();
            var module = ModuleUtil.findModuleForFile(config.getConfigVfs(), project);
            if (Objects.nonNull(module)) {
                var moduleType = ModuleType.get(module).getId();
                if (Objects.equals(moduleType, "JAVA_MODULE")) {
                    e.getPresentation().setEnabled(true);
                }
            }
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
