package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.ui.NewSQLDialog;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.XqlFile;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

public class NewSQLAction extends AnAction {
    private final JTree tree;

    public NewSQLAction(JTree tree) {
        super(MessageBundle.message("action.newSql.text"), MessageBundle.message("action.newSql.description"), AllIcons.Actions.AddMulticaret);
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
            ApplicationManager.getApplication().invokeLater(() -> {
                var d = new NewSQLDialog(project, xqlFile.alias(), xqlFile.config());
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
        if (nodeSource instanceof XqlFile xqlFile) {
            var filename = xqlFile.getAbsoluteFilePath();
            if (!ProjectFileUtil.isLocalFileUri(filename)) {
                e.getPresentation().setEnabled(false);
                return;
            }
            // is error file, do not allow 'new' action
            if (xqlFile.config().getXqlFileManager().getErrorAlias().containsKey(xqlFile.alias())) {
                e.getPresentation().setEnabled(false);
            }
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
