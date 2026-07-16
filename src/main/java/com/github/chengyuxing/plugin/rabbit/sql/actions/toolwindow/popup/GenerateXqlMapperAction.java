package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.ui.MapperGenerateDialog;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.XqlFile;
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
        super(MessageBundle.message("action.generateXqlMapper.text"), MessageBundle.message("action.generateXqlMapper.description"), AllIcons.Actions.Compile);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (nodeSource instanceof XqlFile xqlFile) {
            ApplicationManager.getApplication().invokeLater(() ->
                    new MapperGenerateDialog(project, xqlFile.alias(), xqlFile.config()).showAndGet());
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
        if (nodeSource instanceof XqlFile xqlFile) {
            var config = xqlFile.config();
            var module = ModuleUtil.findModuleForFile(config.getConfigVfs(), project);
            if (Objects.nonNull(module)) {
                // is error file, do not allow 'generate mapper' action
                if (config.getXqlFileManager().getErrorAlias().containsKey(xqlFile.alias())) {
                    e.getPresentation().setEnabled(false);
                    return;
                }
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
