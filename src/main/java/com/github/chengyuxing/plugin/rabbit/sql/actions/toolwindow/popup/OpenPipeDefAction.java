package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.PipeName;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.ModuleUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

public class OpenPipeDefAction extends AnAction {
    private final JTree tree;

    public OpenPipeDefAction(JTree tree) {
        super(MessageBundle.message("action.gotoDefinition.text"));
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (nodeSource instanceof PipeName pipeName) {
            var module = ModuleUtil.findModuleForFile(pipeName.config().getConfigVfs(), project);
            ProjectFileUtil.openJavaFile(project, module, pipeName.className());
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
