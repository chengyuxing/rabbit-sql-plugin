package com.github.chengyuxing.plugin.rabbit.sql.ui.renderer;

import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.common.tuple.Triple;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNode;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.file.XqlIcons;
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
                switch (type) {
                    case MODULE -> {
                        setIcon(AllIcons.Nodes.Module);
                        append(nodeSource.toString());
                    }
                    case XQL_CONFIG -> {
                        var config = (XQLConfigManager.Config) nodeSource.source();
                        setIcon(AllIcons.General.GearPlain);
                        append(nodeSource.toString());
                        if (config.isPrimary()) {
                            append(" [primary]", SimpleTextAttributes.GRAY_ATTRIBUTES);
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
                        appendHTML("(" + sqlMeta.getItem2() + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
                    }
                    case XQL_FRAGMENT -> {
                        @SuppressWarnings("unchecked")
                        var sqlMeta = (Triple<String, String, String>) nodeSource.source();
                        setIcon(AllIcons.FileTypes.Text);
                        append(sqlMeta.getItem2() + " -> ");
                        appendHTML(sqlMeta.getItem3(), SimpleTextAttributes.GRAY_ATTRIBUTES);
                    }
                }
            }
        }
    }
}
