package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.common.tuple.Quadruple;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.MapperGenerateDialog;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
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
import java.util.Objects;

public class GenerateXqlMapperAction extends AnAction {
    private final JTree tree;

    public GenerateXqlMapperAction(JTree tree) {
        super("Generate Code...", "Generate mapper file of this xql file.", AllIcons.Actions.Compile);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (Objects.nonNull(nodeSource) && nodeSource.getType() == XqlTreeNodeData.Type.XQL_FILE) {
            @SuppressWarnings("unchecked") var data = (Quadruple<String, String, String, XQLConfigManager.Config>) nodeSource.getSource();
            var alias = data.getItem1();
            var filename = data.getItem3();
            var config = data.getItem4();
            if (!ProjectFileUtil.isLocalFileUri(filename)) {
                NotificationUtil.showMessage(project, "remote file not support yet", NotificationType.WARNING);
                return;
            }
            ApplicationManager.getApplication().invokeLater(() -> new MapperGenerateDialog(project, alias, config).showAndGet());
        }
    }
}
