package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.common.tuple.Quadruple;
import com.github.chengyuxing.common.tuple.Quintuple;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.util.NotificationUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.utils.SqlUtil;
import com.intellij.icons.AllIcons;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Objects;

public class CopySqlAction extends AnAction {
    private final JTree tree;
    private final CopyType copyType;

    public CopySqlAction(JTree tree, CopyType copyType) {
        super(() -> {
            var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
            if (Objects.nonNull(nodeSource)) {
                if (nodeSource.type() == XqlTreeNodeData.Type.XQL_FRAGMENT) {
                    @SuppressWarnings("unchecked")
                    var sqlMeta = (Quadruple<String, String, XQLFileManager.Sql, XQLConfigManager.Config>) nodeSource.source();
                    var name = sqlMeta.getItem2();
                    switch (copyType) {
                        case SQL_NAME -> {
                            return "SQL Name " + SqlUtil.quote(name);
                        }
                        case SQL_PATH -> {
                            return "SQL Path From " + SqlUtil.quote(name);
                        }
                        case SQL_DEFINITION -> {
                            return "SQL Definition From " + SqlUtil.quote(name);
                        }
                    }
                } else if (nodeSource.type() == XqlTreeNodeData.Type.XQL_FILE) {
                    @SuppressWarnings("unchecked") var sqlMeta = (Quintuple<String, String, String, XQLConfigManager.Config, String>) nodeSource.source();
                    switch (copyType) {
                        case ALIAS -> {
                            return "Alias " + SqlUtil.quote(sqlMeta.getItem1());
                        }
                        case ABSOLUTE_PATH -> {
                            return "Absolute Path From " + SqlUtil.quote(sqlMeta.getItem1());
                        }
                        case PATH_FROM_CLASSPATH -> {
                            return "Classpath Path From " + SqlUtil.quote(sqlMeta.getItem1());
                        }
                        case YML_ARRAY_PATH_FROM_CLASSPATH -> {
                            return "Classpath YAML Array Path From " + SqlUtil.quote(sqlMeta.getItem1());
                        }
                    }
                }
            }
            return "Copy";
        }, () -> "Copy selection.", AllIcons.Actions.Copy);
        this.tree = tree;
        this.copyType = copyType;
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
        if (nodeSource.type() == XqlTreeNodeData.Type.XQL_FRAGMENT) {
            @SuppressWarnings("unchecked")
            var sqlMeta = (Quadruple<String, String, XQLFileManager.Sql, XQLConfigManager.Config>) nodeSource.source();
            var alias = sqlMeta.getItem1();
            var name = sqlMeta.getItem2();
            var sql = sqlMeta.getItem3();
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            switch (copyType) {
                case SQL_NAME -> clipboard.setContents(new StringSelection(name), null);
                case SQL_PATH -> clipboard.setContents(new StringSelection("&" + alias + "." + name), null);
                case SQL_DEFINITION -> clipboard.setContents(new StringSelection(sql.getContent()), null);
            }
            return;
        }
        if (nodeSource.type() == XqlTreeNodeData.Type.XQL_FILE) {
            @SuppressWarnings("unchecked") var sqlMeta = (Quintuple<String, String, String, XQLConfigManager.Config, String>) nodeSource.source();
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            switch (copyType) {
                case ALIAS -> clipboard.setContents(new StringSelection(sqlMeta.getItem1()), null);
                case ABSOLUTE_PATH -> clipboard.setContents(new StringSelection(sqlMeta.getItem3()), null);
                case PATH_FROM_CLASSPATH -> {
                    if (ProjectFileUtil.isURI(sqlMeta.getItem2())) {
                        NotificationUtil.showMessage(project, "Only support local file.", NotificationType.WARNING);
                        return;
                    }
                    clipboard.setContents(new StringSelection(sqlMeta.getItem2()), null);
                }
                case YML_ARRAY_PATH_FROM_CLASSPATH -> {
                    if (ProjectFileUtil.isURI(sqlMeta.getItem2())) {
                        NotificationUtil.showMessage(project, "Only support local file.", NotificationType.WARNING);
                        return;
                    }
                    var classpathPath = sqlMeta.getItem2().split("/");
                    var arrayPath = "[ " + String.join(", ", classpathPath) + " ]";
                    clipboard.setContents(new StringSelection(arrayPath), null);
                }
            }
        }
    }

    public enum CopyType {
        SQL_NAME,
        SQL_PATH,
        SQL_DEFINITION,

        ALIAS,
        ABSOLUTE_PATH,
        PATH_FROM_CLASSPATH,
        YML_ARRAY_PATH_FROM_CLASSPATH
    }
}
