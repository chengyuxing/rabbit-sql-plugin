package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.common.tuple.Tuples;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNode;
import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

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

    public static void path2tree(List<String> paths, Map<String, Object> container) {
        var first = paths.get(0);
        if (!container.containsKey(first)) {
            container.put(first, new LinkedHashMap<>());
        }
        if (paths.size() == 1) {
            return;
        }
        var children = paths.subList(1, paths.size());
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

    public static JBPopup showPreview(String content, int contentHeight, Component target, Point point) {
        var editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setText(HtmlUtil.toHtml(content));
        editorPane.setEditable(false);
        editorPane.setOpaque(false);
        editorPane.setFocusable(true);
        editorPane.setBackground(UIManager.getColor("Label.background"));
        editorPane.setCaretPosition(0);
        UIUtil.addInsets(editorPane, 2, 10, 2, 10);
        var scrollPane = new JBScrollPane(editorPane);
        scrollPane.setFocusable(true);
        int max = Math.min(800, contentHeight);
        max = Math.max(max, 330);
        scrollPane.setPreferredSize(new Dimension(680, max));
        var popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(scrollPane, editorPane)
                .setResizable(true)
                .setMovable(true)
                .setShowShadow(true)
                .setRequestFocus(true)
                .createPopup();
        popup.show(new RelativePoint(target, point));
        return popup;
    }

    public static XqlTreeNode findNode(XqlTreeNode root, Predicate<XqlTreeNode> predicate) {
        if (predicate.test(root)) {
            return root;
        }
        for (int i = 0; i < root.getChildCount(); i++) {
            var childNode = (XqlTreeNode) root.getChildAt(i);
            var result = findNode(childNode, predicate);
            if (Objects.nonNull(result)) {
                return result;
            }
        }
        return null;
    }
}
