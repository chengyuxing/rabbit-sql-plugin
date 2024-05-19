package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.common.tuple.Tuples;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNode;
import com.github.chengyuxing.sql.XQLFileManager;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
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

    public static void path2tree(Path path, Map<String, Object> container) {
        var first = path.getName(0).toString();
        if (!container.containsKey(first)) {
            container.put(first, new LinkedHashMap<>());
        }
        if (path.getNameCount() == 1) {
            return;
        }
        var children = path.subpath(1, path.getNameCount());
        //noinspection unchecked
        path2tree(children, (Map<String, Object>) container.get(first));
    }

    public static void buildXQLTree(Map<String, Object> treeNodes, XQLConfigManager.Config config, XqlTreeNode rootNode) {
        for (var e : treeNodes.entrySet()) {
            var item = e.getKey();
            @SuppressWarnings("unchecked") var children = (Map<String, Object>) e.getValue();
            XqlTreeNode pNode;
            if (children.isEmpty()) {
                var resource = config.getXqlFileManager().getResource(item);
                if (Objects.isNull(resource)) {
                    continue;
                }
                var filename = config.getXqlFileManagerConfig().getFiles().get(item);
                pNode = new XqlTreeNode(new XqlTreeNodeData(XqlTreeNodeData.Type.XQL_FILE, item, Tuples.of(item, filename, resource.getFilename(), config, resource.getDescription())));
                buildXQLNodes(config, item, pNode, resource);
            } else {
                pNode = new XqlTreeNode(new XqlTreeNodeData(XqlTreeNodeData.Type.XQL_FILE_FOLDER, item, config));
            }
            rootNode.add(pNode);
            buildXQLTree(children, config, pNode);
        }
    }

    public static void buildXQLNodes(XQLConfigManager.Config config, String item, XqlTreeNode XQLNode, XQLFileManager.Resource resource) {
        resource.getEntry().forEach((name, sql) -> {
            if (!name.startsWith("${") && !name.endsWith("}")) {
                var sqlNode = new XqlTreeNode(new XqlTreeNodeData(XqlTreeNodeData.Type.XQL_FRAGMENT,
                        name, Tuples.of(item, name, sql, config)));
                XQLNode.add(sqlNode);
            }
        });
    }
}
