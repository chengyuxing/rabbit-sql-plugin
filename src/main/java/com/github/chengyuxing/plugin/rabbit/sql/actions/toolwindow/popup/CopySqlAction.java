package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.common.tuple.Quadruple;
import com.github.chengyuxing.plugin.rabbit.sql.common.TreeNodeSource;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.github.chengyuxing.sql.utils.SqlUtil;
import com.intellij.icons.AllIcons;
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
                if (nodeSource.type() == TreeNodeSource.NodeSourceType.XQL_FRAGMENT) {
                    @SuppressWarnings("unchecked")
                    var sqlMeta = (Quadruple<String, String, String, XQLConfigManager.Config>) nodeSource.source();
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
                }
            }
            return "Copy";
        }, () -> "Copy selection.", AllIcons.Actions.Copy);
        this.tree = tree;
        this.copyType = copyType;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (Objects.nonNull(nodeSource) && nodeSource.type() == TreeNodeSource.NodeSourceType.XQL_FRAGMENT) {
            @SuppressWarnings("unchecked")
            var sqlMeta = (Quadruple<String, String, String, XQLConfigManager.Config>) nodeSource.source();
            var alias = sqlMeta.getItem1();
            var name = sqlMeta.getItem2();
            var sql = sqlMeta.getItem3();
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            switch (copyType) {
                case SQL_NAME -> clipboard.setContents(new StringSelection(name), null);
                case SQL_PATH -> clipboard.setContents(new StringSelection("&" + alias + "." + name), null);
                case SQL_DEFINITION -> clipboard.setContents(new StringSelection(sql), null);
            }
        }
    }

    public enum CopyType {
        SQL_NAME,
        SQL_PATH,
        SQL_DEFINITION
    }
}
