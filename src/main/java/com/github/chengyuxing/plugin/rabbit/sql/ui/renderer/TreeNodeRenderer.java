package com.github.chengyuxing.plugin.rabbit.sql.ui.renderer;

import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.common.tuple.Triple;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNode;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.file.XqlIcons;
import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.icons.AllIcons;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class TreeNodeRenderer extends ColoredTreeCellRenderer {
    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (value instanceof XqlTreeNode node) {
            if (node.getUserObject() instanceof XqlTreeNodeData nodeSource) {
                var type = nodeSource.type();
                setToolTipText(null);
                switch (type) {
                    case MODULE -> {
                        setIcon(AllIcons.Nodes.Module);
                        append(nodeSource.toString());
                    }
                    case XQL_CONFIG -> {
                        var config = (XQLConfigManager.Config) nodeSource.source();
                        append(nodeSource.toString());
                        if (config.isPrimary()) {
                            setIcon(XqlIcons.XQL_FILE_MANAGER);
                            append(" [primary]", SimpleTextAttributes.GRAY_ATTRIBUTES);
                        } else {
                            setIcon(XqlIcons.XQL_FILE_MANAGER_SECONDARY);
                        }
                        if (config.isActive()) {
                            append(" (active)", SimpleTextAttributes.GRAY_ATTRIBUTES);
                        }
                    }
                    case XQL_FILE -> {
                        @SuppressWarnings("unchecked")
                        var sqlMeta = (Pair<String, String>) nodeSource.source();
                        setIcon(XqlIcons.XQL_FILE);
                        append(sqlMeta.getItem1() + " ");
                        append("(" + sqlMeta.getItem2() + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
                    }
                    case XQL_FRAGMENT -> {
                        @SuppressWarnings("unchecked")
                        var sqlMeta = (Triple<String, String, XQLFileManager.Sql>) nodeSource.source();
                        setIcon(AllIcons.FileTypes.Text);
                        append(sqlMeta.getItem2() + " -> ");
                        var info = getInfo(sqlMeta);
                        append(info, SimpleTextAttributes.GRAY_ATTRIBUTES);
                        if (info.isBlank()) {
                            setToolTipText(null);
                        } else {
                            setToolTipText(info);
                        }
                    }
                }
            }
        }
    }

    @NotNull
    private static String getInfo(Triple<String, String, XQLFileManager.Sql> sqlMeta) {
        var sql = sqlMeta.getItem3();
        var sqlContent = sql.getContent();
        var description = sql.getDescription();
        var info = description;
        if (description.isBlank()) {
            if (sqlContent.length() > 100) {
                info = sqlContent.substring(0, 95) + "...";
            } else {
                info = sqlContent;
            }
        }
        return info;
    }
}
