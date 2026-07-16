package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.XqlConfig;
import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

public class ReloadSelectedAction extends AnAction {
    private final JTree tree;

    public ReloadSelectedAction(JTree tree) {
        super(MessageBundle.message("action.reloadSelected.text"), MessageBundle.message("action.reloadSelected.description"), AllIcons.Actions.ForceRefresh);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (nodeSource instanceof XqlConfig xqlConfig) {
            PsiUtil.saveUnsavedXqlAndConfig(project);
            xqlConfig.config().fire();
            PsiUtil.reHighlightActiveEditor(project);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
