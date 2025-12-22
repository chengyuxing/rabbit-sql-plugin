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
                        var sqlMeta = (Quintuple<String, String, String, XQLConfigManager.Config, String>) nodeSource.source();
                        if (ProjectFileUtil.isLocalFileUri(sqlMeta.getItem3())) {
                            setIcon(XqlIcons.XQL_FILE);
                        } else {
                            setIcon(XqlIcons.XQL_FILE_REMOTE);
                        }
                        String secondaryText;
                        append(sqlMeta.getItem1() + " ");
                        if (Objects.nonNull(sqlMeta.getItem5()) && !Objects.equals(sqlMeta.getItem5().trim(), "")) {
                            secondaryText = "(" + sqlMeta.getItem5() + ")";
                            setToolTipText(sqlMeta.getItem2());
                        } else {
                            if (xqlFileTreeView.get()) {
                                secondaryText = "";
                            } else {
                                secondaryText = "(" + sqlMeta.getItem2() + ")";
                            }
                            setToolTipText(null);
                        }
                        append(secondaryText, SimpleTextAttributes.GRAY_ATTRIBUTES);
                    }
                    case XQL_FILE_FOLDER -> {
                        var title = nodeSource.title();
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
        var sqlContent = sql.getSource();
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
