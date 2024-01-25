package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.NewXqlDialog;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

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
        if (Objects.nonNull(nodeSource) && nodeSource.type() == XqlTreeNodeData.Type.XQL_CONFIG) {
            var config = (XQLConfigManager.Config) nodeSource.source();
            var configPath = config.getConfigPath();
            var configVf = VirtualFileManager.getInstance().findFileByNioPath(configPath);
            var doc = ProjectFileUtil.getDocument(project, configVf);
            if (Objects.isNull(doc)) {
                return;
            }
            var anchors = PsiUtil.getYmlAnchors(project, configVf);
            ApplicationManager.getApplication().invokeLater(() -> {
                var d = new NewXqlDialog(project, config, doc, anchors);
                d.initContent();
                d.showAndGet();
            });
        }
    }
}
