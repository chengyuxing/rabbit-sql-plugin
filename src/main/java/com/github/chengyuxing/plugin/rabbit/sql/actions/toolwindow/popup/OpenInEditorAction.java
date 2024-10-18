package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.common.tuple.Triple;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
import com.github.chengyuxing.plugin.rabbit.sql.util.NotificationUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;

public class OpenInEditorAction extends AnAction {
    private final JTree tree;

    public OpenInEditorAction(JTree tree) {
        super("Open In Editor");
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (Objects.nonNull(nodeSource)) {
            var filePath = detectedExistsFilePath(project, nodeSource);
            if (Objects.nonNull(filePath)) {
                var xqlVf = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(filePath);
                if (Objects.nonNull(xqlVf) && xqlVf.exists()) {
                    var psi = PsiManager.getInstance(project).findFile(xqlVf);
                    if (Objects.nonNull(psi)) {
                        NavigationUtil.activateFileWithPsiElement(psi);
                    }
                }
            }
        }
    }

    private static Path detectedExistsFilePath(Project project, XqlTreeNodeData nodeData) {
        switch (nodeData.getType()) {
            case XQL_CONFIG:
                var config = (XQLConfigManager.Config) nodeData.getSource();
                return config.getConfigPath();

            case XQL_FILE:
                @SuppressWarnings("unchecked")
                var sqlMeta = (Triple<String, String, String>) nodeData.getSource();
                var filepath = sqlMeta.getItem3();
                if (!ProjectFileUtil.isLocalFileUri(filepath)) {
                    NotificationUtil.showMessage(project, "only support local file", NotificationType.WARNING);
                    return null;
                }
                return Path.of(URI.create(filepath));
            default:
                return null;
        }
    }
}
