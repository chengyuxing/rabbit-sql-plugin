package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.SqlFragment;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.XqlFile;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
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
            if (nodeSource instanceof SqlFragment sqlFragment) {
                var name = sqlFragment.sqlName();
                switch (copyType) {
                    case SQL_NAME -> {
                        return MessageBundle.message("action.copySql.type.xql.name", name);
                    }
                    case SQL_PATH -> {
                        return MessageBundle.message("action.copySql.type.xql.path", name);
                    }
                    case SQL_DEFINITION -> {
                        return MessageBundle.message("action.copySql.type.xql.def", name);
                    }
                }
            } else if (nodeSource instanceof XqlFile xqlFile) {
                var alias = xqlFile.alias();
                switch (copyType) {
                    case ALIAS -> {
                        return MessageBundle.message("action.copySql.type.file.alias", alias);
                    }
                    case ABSOLUTE_PATH -> {
                        return MessageBundle.message("action.copySql.type.file.abPath", alias);
                    }
                    case PATH_FROM_CLASSPATH -> {
                        return MessageBundle.message("action.copySql.type.file.classpath", alias);
                    }
                    case YML_ARRAY_PATH_FROM_CLASSPATH -> {
                        return MessageBundle.message("action.copySql.type.file.classArrayPath", alias);
                    }
                }
            }
            return MessageBundle.message("action.copySql.text.default");
        }, () -> MessageBundle.message("action.copySql.description.default"), AllIcons.Actions.Copy);
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
        if (nodeSource instanceof SqlFragment sqlMeta) {
            var alias = sqlMeta.xqlAlias();
            var name = sqlMeta.sqlName();
            var sql = sqlMeta.sql();
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            switch (copyType) {
                case SQL_NAME -> clipboard.setContents(new StringSelection(name), null);
                case SQL_PATH ->
                        clipboard.setContents(new StringSelection("&" + XQLFileManager.encodeSqlReference(alias, name)), null);
                case SQL_DEFINITION -> clipboard.setContents(new StringSelection(sql.getSource()), null);
            }
            return;
        }
        if (nodeSource instanceof XqlFile xqlFile) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            switch (copyType) {
                case ALIAS -> clipboard.setContents(new StringSelection(xqlFile.alias()), null);
                case ABSOLUTE_PATH -> clipboard.setContents(new StringSelection(xqlFile.getAbsoluteFilePath()), null);
                case PATH_FROM_CLASSPATH -> {
                    if (ProjectFileUtil.isURI(xqlFile.classPathFileName())) {
                        return;
                    }
                    clipboard.setContents(new StringSelection(xqlFile.classPathFileName()), null);
                }
                case YML_ARRAY_PATH_FROM_CLASSPATH -> {
                    if (ProjectFileUtil.isURI(xqlFile.classPathFileName())) {
                        return;
                    }
                    var classpathPath = xqlFile.classPathFileName().split("/");
                    var arrayPath = "[ " + String.join(", ", classpathPath) + " ]";
                    clipboard.setContents(new StringSelection(arrayPath), null);
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
            switch (copyType) {
                case PATH_FROM_CLASSPATH, YML_ARRAY_PATH_FROM_CLASSPATH -> {
                    if (ProjectFileUtil.isURI(xqlFile.classPathFileName())) {
                        e.getPresentation().setEnabled(false);
                    }
                }
            }
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
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
