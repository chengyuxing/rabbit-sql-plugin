package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow;

import com.github.chengyuxing.common.tuple.Triple;
import com.github.chengyuxing.common.utils.ObjectUtil;
import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.ui.XqlFileManagerToolWindow;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNode;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.github.chengyuxing.sql.annotation.CountQuery;
import com.github.chengyuxing.sql.annotation.XQL;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiIdentifier;
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

        String sqlRef = null;
        if (element instanceof PsiComment) {
            var commentText = element.getText();
            var pattern = Pattern.compile(Constants.SQL_NAME_ANNOTATION_PATTERN);
            var m = pattern.matcher(commentText);
            if (m.matches()) {
                sqlRef = ObjectUtil.coalesce(m.group("sqlName"), m.group("partName"));
            }
        } else {
            if (PsiUtil.isParentAXQLMapperInterface(element)) {
                var mapperAlias = PsiUtil.getXQLMapperAlias(element);
                if (Objects.nonNull(mapperAlias)) {
                    if (PsiUtil.isXQLMapperMethodIdentifier(element)) {
                        var sqlNameMv = PsiUtil.getMethodAnnoValue((PsiIdentifier) element, XQL.class.getName(), "value");
                        String sqlName;
                        if (Objects.nonNull(sqlNameMv)) {
                            sqlName = PsiUtil.getAnnoTextValue(sqlNameMv).trim();
                            if (sqlName.isEmpty()) {
                                sqlName = element.getText();
                            }
                        } else {
                            sqlName = element.getText();
                        }
                        sqlRef = "&" + mapperAlias + "." + sqlName;
                    } else {
                        var annoXqlValue = PsiUtil.getIfElementIsAnnotationAttr(element, XQL.class.getName(), "value");
                        if (Objects.isNull(annoXqlValue)) {
                            annoXqlValue = PsiUtil.getIfElementIsAnnotationAttr(element, CountQuery.class.getName(), "value");
                        }
                        if (Objects.nonNull(annoXqlValue)) {
                            var sqlName = PsiUtil.getAnnoTextValue(annoXqlValue).trim();
                            if (!sqlName.isEmpty()) {
                                sqlRef = "&" + mapperAlias + "." + sqlName;
                            }
                        }
                    }
                }
            } else {
                String jvmLangLiteral = PsiUtil.getJvmLangLiteral(element);
                if (Objects.nonNull(jvmLangLiteral) && jvmLangLiteral.matches(SQL_NAME_PATTERN)) {
                    sqlRef = jvmLangLiteral;
                }
            }
        }
        final var finalSqlRef = sqlRef;
        XqlFileManagerToolWindow.getXqlFileManagerPanel(project, panel -> {
            var tree = panel.getTree();
            var root = tree.getModel().getRoot();

            XqlTreeNode node = null;
            if (Objects.nonNull(finalSqlRef) && finalSqlRef.startsWith("&")) {
                var sqlRefParts = StringUtil.extraSqlReference(finalSqlRef.substring(1));
                var alias = sqlRefParts.getItem1();
                var name = sqlRefParts.getItem2();
                node = SwingUtil.findNode((XqlTreeNode) root, treeNode -> {
                    if (treeNode.getUserObject() instanceof XqlTreeNodeData) {
                        var nodeData = (XqlTreeNodeData) treeNode.getUserObject();
                        if (nodeData.getType() == XqlTreeNodeData.Type.XQL_FRAGMENT) {
                            if (!nodeData.getTitle().equals(name)) {
                                return false;
                            }
                            if (treeNode.getParent() instanceof XqlTreeNode) {
                                var parentTreeNode = (XqlTreeNode) treeNode.getParent();
                                if (parentTreeNode.getUserObject() instanceof XqlTreeNodeData) {
                                    var parentNodeData = (XqlTreeNodeData) parentTreeNode.getUserObject();
                                    if (parentNodeData.getType() == XqlTreeNodeData.Type.XQL_FILE) {
                                        return parentNodeData.getTitle().equals(alias);
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
                    if (treeNode.getUserObject() instanceof XqlTreeNodeData) {
                        var nodeData = (XqlTreeNodeData) treeNode.getUserObject();
                        if (nodeData.getType() == XqlTreeNodeData.Type.XQL_FILE) {
                            @SuppressWarnings("unchecked") var sqlMeta = (Triple<String, String, String>) nodeData.getSource();
                            var filepath = sqlMeta.getItem3();
                            var matchFile = filepath.equals(currentFile.toNioPath().toUri().toString());

                            for (int i = 0, j = treeNode.getChildCount(); i < j; i++) {
                                if (treeNode.getChildAt(i) instanceof XqlTreeNode) {
                                    var childNode = (XqlTreeNode) treeNode.getChildAt(i);
                                    if (childNode.getUserObject() instanceof XqlTreeNodeData) {
                                        var childNodeData = (XqlTreeNodeData) childNode.getUserObject();
                                        var matchSqlName = childNodeData.getTitle().equals(finalSqlRef);
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
}
