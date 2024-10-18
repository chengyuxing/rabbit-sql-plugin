package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.ui.NewXQLFileManagerDialog;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class NewXqlFileManagerAction extends AnAction {
    private final JTree tree;

    public NewXqlFileManagerAction(JTree tree) {
        super("New", "Create a new XQL file manager config.", AllIcons.Actions.AddMulticaret);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (Objects.nonNull(nodeSource) && nodeSource.getType() == XqlTreeNodeData.Type.MODULE) {
            var module = (Path) nodeSource.getSource();

            var primaryAbsFilename = module.resolve(Constants.CONFIG_PATH);
            if (Files.exists(primaryAbsFilename)) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    var d = new NewXQLFileManagerDialog(project, module);
                    d.showAndGet();
                });
                return;
            }
            ProjectFileUtil.createXqlConfigByTemplate(project, primaryAbsFilename, () ->
                    ApplicationManager.getApplication().runWriteAction(() ->
                            ProjectFileUtil.openFile(project, primaryAbsFilename, true)));
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
