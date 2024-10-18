package com.github.chengyuxing.plugin.rabbit.sql.ui.renderer;

import com.github.chengyuxing.common.tuple.Quintuple;
import com.github.chengyuxing.common.tuple.Triple;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNode;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.file.XqlIcons;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.icons.AllIcons;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;
import java.util.function.Supplier;

public class TreeNodeRenderer extends ColoredTreeCellRenderer {
    private final Supplier<Boolean> xqlFileTreeView;

    public TreeNodeRenderer(Supplier<Boolean> xqlFileTreeView) {
        this.xqlFileTreeView = xqlFileTreeView;
    }

    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (value instanceof XqlTreeNode) {
            var node = (XqlTreeNode) value;
            if (node.getUserObject() instanceof XqlTreeNodeData) {
                var nodeSource = (XqlTreeNodeData) node.getUserObject();
                var type = nodeSource.getType();
                setToolTipText(null);
                switch (type) {
                    case MODULE:
                        setIcon(AllIcons.Nodes.Module);
                        append(nodeSource.toString());
                        break;
                    case XQL_CONFIG:
                        var config = (XQLConfigManager.Config) nodeSource.getSource();
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
                        break;
                    case XQL_FILE:
                        @SuppressWarnings("unchecked")
                        var xqlMeta = (Quintuple<String, String, String, XQLConfigManager.Config, String>) nodeSource.getSource();
                        if (ProjectFileUtil.isLocalFileUri(xqlMeta.getItem3())) {
                            setIcon(XqlIcons.XQL_FILE);
                        } else {
                            setIcon(XqlIcons.XQL_FILE_REMOTE);
                        }
                        String secondaryText;
                        append(xqlMeta.getItem1() + " ");
                        if (Objects.nonNull(xqlMeta.getItem5()) && !Objects.equals(xqlMeta.getItem5().trim(), "")) {
                            secondaryText = "(" + xqlMeta.getItem5() + ")";
                            setToolTipText(xqlMeta.getItem2());
                        } else {
                            if (xqlFileTreeView.get()) {
                                secondaryText = "";
                            } else {
                                secondaryText = "(" + xqlMeta.getItem2() + ")";
                            }
                            setToolTipText(null);
                        }
                        append(secondaryText, SimpleTextAttributes.GRAY_ATTRIBUTES);
                        break;
                    case XQL_FILE_FOLDER:
                        var title = nodeSource.getTitle();
                        if (ProjectFileUtil.isURI(title)) {
                            if (!ProjectFileUtil.isLocalFileUri(title)) {
                                setIcon(AllIcons.Nodes.PpWeb);
                            } else {
                                setIcon(AllIcons.Nodes.Folder);
                            }
                        } else {
                            setIcon(AllIcons.Nodes.Folder);
                        }
                        append(nodeSource.toString());
                        break;
                    case XQL_FRAGMENT:
                        @SuppressWarnings("unchecked")
                        var sqlMeta = (Triple<String, String, XQLFileManager.Sql>) nodeSource.getSource();
                        setIcon(AllIcons.FileTypes.Text);
                        append(sqlMeta.getItem2() + " -> ");
                        var info = getInfo(sqlMeta);
                        append(info, SimpleTextAttributes.GRAY_ATTRIBUTES);
                        if (info.isBlank()) {
                            setToolTipText(null);
                        } else {
                            setToolTipText(info);
                        }
                        break;
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
