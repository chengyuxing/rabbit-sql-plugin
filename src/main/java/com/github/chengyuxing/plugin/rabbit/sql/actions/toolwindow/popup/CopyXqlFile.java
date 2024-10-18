package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.tuple.Triple;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
import com.github.chengyuxing.plugin.rabbit.sql.util.FileTransferable;
import com.github.chengyuxing.plugin.rabbit.sql.util.NotificationUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.intellij.icons.AllIcons;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class CopyXqlFile extends AnAction {
    private final JTree tree;

    public CopyXqlFile(JTree tree) {
        super("Copy", "Copy XQL file to system clipboard.", AllIcons.Actions.Copy);
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
        if (nodeSource.getType() == XqlTreeNodeData.Type.XQL_FILE) {
            //noinspection unchecked
            var sqlMeta = (Triple<String, String, String>) nodeSource.getSource();
            var filepath = sqlMeta.getItem3();
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                try {
                    var file = createFileByUri(filepath);
                    var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(new FileTransferable(List.of(file)), null);
                } catch (IOException ex) {
                    NotificationUtil.showMessage(project, ex.getMessage(), NotificationType.WARNING);
                }
            });
        }
    }

    private static File createFileByUri(String path) throws IOException {
        if (ProjectFileUtil.isLocalFileUri(path)) {
            return Path.of(URI.create(path)).toFile();
        }
        var filename = FileResource.getFileName(path, false);
        var file = File.createTempFile(filename + "_", ".xql");
        file.deleteOnExit();
        try (var out = new FileOutputStream(file)) {
            var fr = new FileResource(path);
            fr.transferTo(out);
        }
        return file;
    }
}
