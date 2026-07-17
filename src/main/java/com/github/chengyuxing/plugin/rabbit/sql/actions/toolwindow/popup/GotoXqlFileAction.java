package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.SqlFragment;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

public class GotoXqlFileAction extends AnAction {
    private final JTree tree;

    public GotoXqlFileAction(JTree tree) {
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
        if (nodeSource instanceof SqlFragment sqlFragment) {
            PsiUtil.navigate2xqlFile(sqlFragment.xqlAlias(), sqlFragment.sqlName(), sqlFragment.config());
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(false);
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (nodeSource instanceof SqlFragment sqlFragment) {
            var xqlFile = sqlFragment.config().getXqlFileManager().getResource(sqlFragment.xqlAlias()).getFilename();
            if (!ProjectFileUtil.isLocalFileUri(xqlFile)) {
                e.getPresentation().setEnabled(false);
                return;
            }
            e.getPresentation().setEnabled(true);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
