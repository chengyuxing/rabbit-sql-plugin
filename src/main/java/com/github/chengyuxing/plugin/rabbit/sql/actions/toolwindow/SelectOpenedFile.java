package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow;

import com.github.chengyuxing.common.tuple.Triple;
import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.ui.XqlFileManagerToolWindow;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNode;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiComment;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

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

        String jvmLangLiteral = PsiUtil.getJvmLangLiteral(element);

        String sqlRef;
        if (element instanceof PsiComment comment) {
            var commentText = comment.getText();
            var pattern = Pattern.compile(Constants.SQL_NAME_ANNOTATION_PATTERN);
            var m = pattern.matcher(commentText);
            if (m.matches()) {
                sqlRef = m.group("name");
            } else {
                sqlRef = null;
            }
        } else if (Objects.nonNull(jvmLangLiteral) && jvmLangLiteral.matches(SQL_NAME_PATTERN)) {
            sqlRef = jvmLangLiteral;
        } else {
            sqlRef = null;
        }

        XqlFileManagerToolWindow.getXqlFileManagerPanel(project, panel -> {
            var tree = panel.getTree();
            var root = tree.getModel().getRoot();

            XqlTreeNode node = null;

            if (Objects.nonNull(sqlRef) && sqlRef.startsWith("&")) {
                var sqlRefParts = StringUtil.extraSqlReference(sqlRef.substring(1));
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

            AtomicReference<XqlTreeNode> sqlCommentNode = new AtomicReference<>();
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
                            var matchFile = filepath.equals(currentFile.toNioPath().toUri().toString());

                            for (int i = 0, j = treeNode.getChildCount(); i < j; i++) {
                                if (treeNode.getChildAt(i) instanceof XqlTreeNode childNode) {
                                    if (childNode.getUserObject() instanceof XqlTreeNodeData childNodeData) {
                                        var matchSqlName = childNodeData.title().equals(sqlRef);
                                        if (matchSqlName) {
                                            sqlCommentNode.set(childNode);
                                            return true;
                                        }
                                    }
                                }
                            }
                            return matchFile;
                        }
                    }
                    return false;
                });
            }
            if (Objects.nonNull(sqlCommentNode.get())) {
                node = sqlCommentNode.get();
            }
            if (Objects.nonNull(node)) {
                var treePath = new TreePath(node.getPath());
                tree.setSelectionPath(treePath);
                tree.scrollPathToVisible(treePath);
            }
        });
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
