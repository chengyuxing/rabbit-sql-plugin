package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.FeatureChecker;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.yml.YmlUtil;
import com.github.chengyuxing.plugin.rabbit.sql.ui.NewXqlDialog;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNode;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
import com.github.chengyuxing.plugin.rabbit.sql.util.*;
import com.intellij.icons.AllIcons;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
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
        if (nodeSource.type() == XqlTreeNodeData.Type.XQL_CONFIG) {
            var config = (XQLConfigManager.Config) nodeSource.source();
            openNewXqlDialog(project, config, List.of());
            return;
        }
        if (nodeSource.type() == XqlTreeNodeData.Type.XQL_FILE_FOLDER) {
            var config = (XQLConfigManager.Config) nodeSource.source();
            var selected = tree.getSelectionPath();
            if (Objects.isNull(selected)) {
                return;
            }
            var folderClasspath = Stream.of(selected.getPath())
                    .filter(p -> p instanceof XqlTreeNode)
                    .map(p -> ((XqlTreeNode) p).getUserObject())
                    .filter(n -> n instanceof XqlTreeNodeData)
                    .map(n -> (XqlTreeNodeData) n)
                    .filter(n -> n.type() == XqlTreeNodeData.Type.XQL_FILE_FOLDER)
                    .map(XqlTreeNodeData::title)
                    .toList();
            if (folderClasspath.isEmpty()) {
                return;
            }
            var first = folderClasspath.get(0);
            if (ProjectFileUtil.isURI(first)) {
                NotificationUtil.showMessage(project, "only support classpath folder", NotificationType.WARNING);
                return;
            }
            openNewXqlDialog(project, config, folderClasspath);
        }
    }

    private void openNewXqlDialog(Project project, XQLConfigManager.Config config, List<String> pathPrefix) {
        var configPath = config.getConfigPath();
        var configVf = VirtualFileManager.getInstance().findFileByNioPath(configPath);
        var doc = ProjectFileUtil.getDocument(project, configVf);
        if (Objects.isNull(doc)) {
            return;
        }
        Map<String, String> anchors;
        if (FeatureChecker.isPluginEnabled(FeatureChecker.YML_PLUGIN_ID)) {
            anchors = YmlUtil.getYmlAnchors(project, configVf);
        } else {
            anchors = Map.of();
            NotificationUtil.showMessage(project, "YAML plugin is not enabled. YAML-anchor features are disabled.", NotificationType.WARNING);
        }
        ApplicationManager.getApplication().invokeLater(() -> {
            var d = new NewXqlDialog(project, config, doc, anchors);
            d.setPathPrefix(pathPrefix);
            d.initContent();
            d.showAndGet();
        });
    }
}
