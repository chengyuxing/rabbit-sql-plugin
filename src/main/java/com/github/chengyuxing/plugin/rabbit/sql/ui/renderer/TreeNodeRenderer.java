package com.github.chengyuxing.plugin.rabbit.sql.ui.renderer;

import com.github.chengyuxing.common.util.StringUtils;
import com.github.chengyuxing.common.util.ValueUtils;
import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.XqlTreeNode;
import com.github.chengyuxing.plugin.rabbit.sql.file.XqlIcons;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.NodeData;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.*;
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
        if (value instanceof XqlTreeNode node && node.getUserObject() instanceof NodeData source) {
            setToolTipText(null);
            if (source instanceof ProjectModule) {
                setIcon(AllIcons.Nodes.Module);
                append(source.toString());
            } else if (source instanceof XqlConfig xqlConfig) {
                var config = xqlConfig.config();
                append(source.toString());
                if (config.isPrimary()) {
                    setIcon(XqlIcons.XQL_FILE_MANAGER);
                    append(" " + MessageBundle.message("ui.xqlConfig.status.primary"), SimpleTextAttributes.GRAY_ATTRIBUTES);
                } else {
                    setIcon(XqlIcons.XQL_FILE_MANAGER_SECONDARY);
                }
                if (config.isActive()) {
                    append(" " + MessageBundle.message("ui.xqlConfig.status.active"), SimpleTextAttributes.GRAY_ATTRIBUTES);
                }
            } else if (source instanceof XqlFile xqlFile) {
                if (ProjectFileUtil.isLocalFileUri(xqlFile.getAbsoluteFilePath())) {
                    setIcon(XqlIcons.XQL_FILE);
                } else {
                    setIcon(XqlIcons.XQL_FILE_REMOTE);
                }
                String secondaryText;

                boolean isFileHasError = xqlFile.config().getXqlFileManager().getErrorAlias().containsKey(xqlFile.alias());
                if (isFileHasError) {
                    // highlight error file node
                    append(source + " ", SimpleTextAttributes.ERROR_ATTRIBUTES);
                } else {
                    append(source.toString());
                }

                if (!Objects.equals(xqlFile.getDescription().trim(), "")) {
                    secondaryText = xqlFile.getDescription();
                    setToolTipText(xqlFile.classPathFileName());
                } else {
                    if (xqlFileTreeView.get()) {
                        secondaryText = "";
                    } else {
                        secondaryText = xqlFile.classPathFileName();
                    }
                    setToolTipText(null);
                }
                append(" " + secondaryText, SimpleTextAttributes.GRAY_ATTRIBUTES);
            } else if (source instanceof XqlFileFolder xqlFileFolder) {
                var title = xqlFileFolder.title();
                if (ProjectFileUtil.isURI(title)) {
                    if (!ProjectFileUtil.isLocalFileUri(title)) {
                        setIcon(AllIcons.Nodes.PpWeb);
                    } else {
                        setIcon(AllIcons.Nodes.Folder);
                    }
                } else {
                    setIcon(AllIcons.Nodes.Folder);
                }
                append(source.toString());
            } else if (source instanceof SqlFragment sqlFragment) {
                setIcon(AllIcons.FileTypes.Text);
                append(source.toString());
                var info = getInfo(sqlFragment.sql());
                append(" " + info, SimpleTextAttributes.GRAY_ATTRIBUTES);
                if (info.isBlank()) {
                    setToolTipText(null);
                } else {
                    setToolTipText(info);
                }
            } else if (source instanceof Folder folder) {
                setIcon(folder.icon());
                append(source.toString());
            } else if (source instanceof PipeName pipeName) {
                if (pipeName.builtin()) {
                    setIcon(AllIcons.Ide.Readonly);
                } else {
                    setIcon(AllIcons.Nodes.Function);
                }
                append(source.toString());
                append(" " + shortPackageName(pipeName.className()), SimpleTextAttributes.GRAY_ATTRIBUTES);
            } else if (source instanceof Constant constant) {
                setIcon(AllIcons.Nodes.Constant);
                append(source.toString());
                append(" = " + ValueUtils.coalesceNonNull(constant.value(), "null"), SimpleTextAttributes.GRAY_ATTRIBUTES);
            } else if (source instanceof Property item) {
                setIcon(AllIcons.Nodes.Property);
                append(source.toString());
                append(" = " + item.value(), SimpleTextAttributes.GRAY_ATTRIBUTES);
            }
        }
    }

    private static String shortPackageName(String packageName) {
        int dotCount = StringUtils.countOccurrences(packageName, ".");
        if (dotCount >= 7) {
            String[] ps = packageName.split("\\.");
            ps[0] = String.valueOf(ps[0].charAt(0));
            ps[1] = String.valueOf(ps[1].charAt(0));
            ps[2] = String.valueOf(ps[2].charAt(0));
            return String.join(".", ps);
        }
        return packageName;
    }

    @NotNull
    private static String getInfo(XQLFileManager.Sql sql) {
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
