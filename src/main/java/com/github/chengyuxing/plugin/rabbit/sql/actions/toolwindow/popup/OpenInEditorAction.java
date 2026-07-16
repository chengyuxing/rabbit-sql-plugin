package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.NodeData;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.XqlConfig;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.XqlFile;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
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
        super(MessageBundle.message("action.openInEditor.text"));
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        var filePath = detectedExistsFilePath(nodeSource);
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

    @Override
    public void update(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (nodeSource instanceof XqlFile xqlFile) {
            var filepath = xqlFile.getAbsoluteFilePath();
            if (!ProjectFileUtil.isLocalFileUri(filepath)) {
                e.getPresentation().setEnabled(false);
            }
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    private static Path detectedExistsFilePath(NodeData nodeSource) {
        if (nodeSource instanceof XqlConfig xqlConfig) {
            return xqlConfig.config().getConfigPath();
        }
        if (nodeSource instanceof XqlFile xqlFile) {
            var filepath = xqlFile.getAbsoluteFilePath();
            return Path.of(URI.create(filepath));
        }
        return null;
    }
}
