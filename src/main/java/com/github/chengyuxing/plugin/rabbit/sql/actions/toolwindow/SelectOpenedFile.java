package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow;

import com.github.chengyuxing.common.tuple.Triple;
import com.github.chengyuxing.plugin.rabbit.sql.ui.XqlFileManagerToolWindow;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNode;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.util.Objects;

import static com.github.chengyuxing.plugin.rabbit.sql.common.Constants.SQL_NAME_PATTERN;

public class SelectOpenedFile extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }

        var element = PsiUtil.getElementAtCaret(project);

        if (Objects.isNull(element)) {
            return;
        }

        String javaLiteral = PsiUtil.getJavaLiteral(element);

        String sqlRef;
        if (Objects.nonNull(javaLiteral) && javaLiteral.matches(SQL_NAME_PATTERN)) {
            sqlRef = javaLiteral.substring(1);
        } else {
            sqlRef = null;
        }

        XqlFileManagerToolWindow.getXqlFileManagerPanel(project, panel -> {
            var tree = panel.getTree();
            var root = tree.getModel().getRoot();

            XqlTreeNode node = null;

            if (Objects.nonNull(sqlRef)) {
                var sqlRefParts = StringUtil.extraSqlReference(sqlRef);
                var alias = sqlRefParts.getItem1();
                var name = sqlRefParts.getItem2();
                node = SwingUtil.findNode((XqlTreeNode) root, treeNode -> {
                    if (treeNode.getUserObject() instanceof XqlTreeNodeData nodeData) {
                        if (nodeData.type() == XqlTreeNodeData.Type.XQL_FRAGMENT) {
                            if (!nodeData.title().equals(name)) {
                                return false;
                            }
                            if (treeNode.getParent() instanceof XqlTreeNode parentTreeNode) {
                                if (parentTreeNode.getUserObject() instanceof XqlTreeNodeData parentNodeData) {
                                    if (parentNodeData.type() == XqlTreeNodeData.Type.XQL_FILE) {
                                        return parentNodeData.title().equals(alias);
                                    }
                                }
                            }
                        }
                    }
                    return false;
                });
            }

            if (Objects.isNull(node)) {
                var currentFile = PsiUtil.getActiveFile(project);
                if (Objects.isNull(currentFile)) {
                    return;
                }
                node = SwingUtil.findNode((XqlTreeNode) root, treeNode -> {
                    if (treeNode.getUserObject() instanceof XqlTreeNodeData nodeData) {
                        if (nodeData.type() == XqlTreeNodeData.Type.XQL_FILE) {
                            @SuppressWarnings("unchecked") var sqlMeta = (Triple<String, String, String>) nodeData.source();
                            var filepath = sqlMeta.getItem3();
                            return filepath.equals(currentFile.toNioPath().toUri().toString());
                        }
                    }
                    return false;
                });
            }
            if (Objects.nonNull(node)) {
                var treePath = new TreePath(node.getPath());
                tree.setSelectionPath(treePath);
                tree.scrollPathToVisible(treePath);
            }
        });
    }
}
