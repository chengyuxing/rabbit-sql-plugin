package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.NewXqlDialog;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNode;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
import com.github.chengyuxing.plugin.rabbit.sql.util.*;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class NewXqlFileAction extends AnAction {
    private final JTree tree;

    public NewXqlFileAction(JTree tree) {
        super("New", "Create a new XQL file.", AllIcons.Actions.AddMulticaret);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (Objects.isNull(nodeSource)) {
            return;
        }
        if (nodeSource.getType() == XqlTreeNodeData.Type.XQL_CONFIG) {
            var config = (XQLConfigManager.Config) nodeSource.getSource();
            openNewXqlDialog(project, config, List.of());
            return;
        }
        if (nodeSource.getType() == XqlTreeNodeData.Type.XQL_FILE_FOLDER) {
            var config = (XQLConfigManager.Config) nodeSource.getSource();
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
        if (Objects.isNull(nodeSource)) {
            return;
        }
        if (nodeSource.getType() == XqlTreeNodeData.Type.XQL_FILE_FOLDER) {
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
                .filter(n -> n instanceof XqlTreeNodeData)
                .map(n -> (XqlTreeNodeData) n)
                .filter(n -> n.getType() == XqlTreeNodeData.Type.XQL_FILE_FOLDER)
                .map(XqlTreeNodeData::getTitle)
                .toList();
    }

    private static void openNewXqlDialog(Project project, XQLConfigManager.Config config, List<String> pathPrefix) {
        var configPath = config.getConfigPath();
        var configVf = VirtualFileManager.getInstance().findFileByNioPath(configPath);
        var doc = ProjectFileUtil.getDocument(project, configVf);
        if (Objects.isNull(doc)) {
            return;
        }
        ApplicationManager.getApplication().invokeLater(() -> {
            var d = new NewXqlDialog(project, config, doc, Map.of());
            d.setPathPrefix(pathPrefix);
            d.initContent();
            d.showAndGet();
        });
    }
}
