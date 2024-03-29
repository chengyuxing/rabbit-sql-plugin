package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNode;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Objects;

public class SwingUtil {
    public static XqlTreeNodeData getTreeSelectionNodeUserData(JTree tree) {
        var selected = tree.getSelectionPath();
        if (Objects.isNull(selected)) {
            return null;
        }
        var node = (XqlTreeNode) selected.getLastPathComponent();
        if (node.getUserObject() instanceof XqlTreeNodeData nodeSource) {
            return nodeSource;
        }
        return null;
    }

    public static void toggleTreeView(JTree tree, TreePath parent, boolean expand, boolean includeCurrentNode) {
        var root = (TreeNode) parent.getLastPathComponent();
        if (root.getChildCount() >= 0) {
            var nodes = root.children();
            while (nodes.hasMoreElements()) {
                var node = nodes.nextElement();
                var path = parent.pathByAddingChild(node);
                toggleTreeView(tree, path, expand, true);
            }
        }
        if (includeCurrentNode) {
            if (expand) {
                tree.expandPath(parent);
            } else {
                tree.collapsePath(parent);
            }
        }
    }
}
