package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.file.XqlIcons;
import com.github.chengyuxing.plugin.rabbit.sql.ui.NewXqlDialog;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.XqlTreeNode;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.XqlConfig;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.XqlFileFolder;
import com.github.chengyuxing.plugin.rabbit.sql.util.*;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class NewXqlFileAction extends AnAction {
    private final JTree tree;

    public NewXqlFileAction(JTree tree) {
        super(() -> {
            var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
            if (nodeSource instanceof XqlFileFolder) {
                return MessageBundle.message("new.text");
            }
            return MessageBundle.message("action.newXql.text");
        }, () -> MessageBundle.message("action.newXql.description"), XqlIcons.XQL_FILE);
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
            openNewXqlDialog(project, xqlConfig.config(), List.of());
            return;
        }
        if (nodeSource instanceof XqlFileFolder xqlFileFolder) {
            var config = xqlFileFolder.config();
            var selected = tree.getSelectionPath();
            if (Objects.isNull(selected)) {
                return;
            }
            var folderClasspath = getFolderClasspath(selected);
            if (folderClasspath.isEmpty()) {
                return;
            }
            openNewXqlDialog(project, config, folderClasspath);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (nodeSource instanceof XqlFileFolder) {
            var selected = tree.getSelectionPath();
            if (Objects.isNull(selected)) {
                return;
            }
            var folderClasspath = getFolderClasspath(selected);
            if (folderClasspath.isEmpty()) {
                return;
            }
            var first = folderClasspath.get(0);
            if (ProjectFileUtil.isURI(first)) {
                e.getPresentation().setEnabled(false);
            }
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    private static List<String> getFolderClasspath(TreePath selected) {
        return Stream.of(selected.getPath())
                .filter(p -> p instanceof XqlTreeNode)
                .map(p -> ((XqlTreeNode) p).getUserObject())
                .filter(n -> n instanceof XqlFileFolder)
                .map(n -> (XqlFileFolder) n)
                .map(XqlFileFolder::title)
                .toList();
    }

    private static void openNewXqlDialog(Project project, XQLConfigManager.Config config, List<String> pathPrefix) {
        ApplicationManager.getApplication().invokeLater(() -> {
            var d = new NewXqlDialog(project, config);
            d.setPathPrefix(pathPrefix);
            d.initContent();
            d.showAndGet();
        });
    }
}
