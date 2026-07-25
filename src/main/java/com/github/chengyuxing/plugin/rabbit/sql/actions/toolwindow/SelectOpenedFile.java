package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow;

import com.github.chengyuxing.common.util.ValueUtils;
import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.XqlFileManagerToolWindow;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.XqlTreeNode;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.SqlFragment;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.XqlConfig;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.XqlFile;
import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.annotation.CountQuery;
import com.github.chengyuxing.sql.annotation.XQL;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiIdentifier;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.chengyuxing.plugin.rabbit.sql.common.Constants.SQL_NAME_PATTERN;
import static com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil.findParentObjectUntil;

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
        if (element instanceof PsiComment comment) {
            var commentText = comment.getText();
            var m = Constants.SQL_NAME_ANNOTATION_PATTERN.matcher(commentText);
            if (m.matches()) {
                sqlRef = ValueUtils.coalesce(m.group("sqlName"), m.group("partName"));
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
                        sqlRef = "&" + XQLFileManager.encodeSqlReference(mapperAlias, sqlName);
                    } else {
                        var annoXqlValue = PsiUtil.getIfElementIsAnnotationAttr(element, XQL.class.getName(), "value");
                        if (Objects.isNull(annoXqlValue)) {
                            annoXqlValue = PsiUtil.getIfElementIsAnnotationAttr(element, CountQuery.class.getName(), "value");
                        }
                        if (Objects.nonNull(annoXqlValue)) {
                            var sqlName = PsiUtil.getAnnoTextValue(annoXqlValue).trim();
                            if (!sqlName.isEmpty()) {
                                sqlRef = "&" + XQLFileManager.encodeSqlReference(mapperAlias, sqlName);
                            }
                        }
                    }
                }
            } else {
                String jvmLangLiteral = PsiUtil.getJvmLangLiteral(element);
                if (Objects.nonNull(jvmLangLiteral) && SQL_NAME_PATTERN.matcher(jvmLangLiteral).matches()) {
                    sqlRef = jvmLangLiteral;
                }
            }
        }
        final var finalSqlRef = sqlRef;
        XqlFileManagerToolWindow.getXqlFileManagerPanel(project, panel -> {
            var xqlConfigManager = XQLConfigManager.getInstance(project);
            var tree = panel.getTree();
            var root = tree.getModel().getRoot();

            // find xql name reference which in the string literal
            if (Objects.nonNull(finalSqlRef) && finalSqlRef.startsWith("&")) {
                var sqlRefParts = StringUtil.extraSqlReference(finalSqlRef.substring(1));
                var alias = sqlRefParts.getItem1();
                var name = sqlRefParts.getItem2();
                var node = SwingUtil.findNode((XqlTreeNode) root, treeNode -> {
                    var sqlFragment = findParentObjectUntil(treeNode, SqlFragment.class);
                    if (sqlFragment == null) {
                        return false;
                    }
                    if (!sqlFragment.sqlName().equals(name)) {
                        return false;
                    }
                    // find xql node
                    var xqlFile = findParentObjectUntil(treeNode, XqlFile.class);
                    var xqlConfig = findParentObjectUntil(treeNode, XqlConfig.class);
                    if (xqlFile == null || xqlConfig == null) {
                        return false;
                    }
                    var activeConfig = xqlConfigManager.getActiveConfig(element);
                    if (activeConfig != null && activeConfig.getConfigName().equals(xqlConfig.config().getConfigName())) {
                        return xqlFile.alias().equals(alias);
                    }
                    return false;
                });
                activeTreeNode(tree, node);
                return;
            }

            // find xql file
            AtomicReference<XqlTreeNode> sqlCommentNode = new AtomicReference<>();
            var currentFile = PsiUtil.getActiveFile(project);
            if (Objects.isNull(currentFile)) {
                return;
            }
            var node = SwingUtil.findNode((XqlTreeNode) root, treeNode -> {
                var xqlFile = findParentObjectUntil(treeNode, XqlFile.class);
                var xqlConfig = findParentObjectUntil(treeNode, XqlConfig.class);
                if (xqlConfig == null || xqlFile == null) {
                    return false;
                }
                var activeConfig = xqlConfigManager.getActiveConfig(element);
                if (Objects.isNull(activeConfig) || !activeConfig.getConfigName().equals(xqlConfig.config().getConfigName())) {
                    return false;
                }
                var filepath = xqlFile.getAbsoluteFilePath();
                var matchFile = filepath.equals(currentFile.toNioPath().toUri().toString());
                for (int i = 0, j = treeNode.getChildCount(); i < j; i++) {
                    var childNode = treeNode.getChildAt(i);
                    var sqlFragment = findParentObjectUntil(treeNode.getChildAt(i), SqlFragment.class);
                    if (sqlFragment == null) {
                        continue;
                    }
                    var matchSqlName = sqlFragment.sqlName().equals(finalSqlRef);
                    if (matchSqlName && matchFile) {
                        sqlCommentNode.set((XqlTreeNode) childNode);
                        return true;
                    }
                }
                return matchFile;
            });
            if (Objects.nonNull(sqlCommentNode.get())) {
                node = sqlCommentNode.get();
            }
            activeTreeNode(tree, node);
        });
    }

    private void activeTreeNode(Tree tree, XqlTreeNode node) {
        if (Objects.nonNull(node)) {
            var treePath = new TreePath(node.getPath());
            tree.setSelectionPath(treePath);
            tree.scrollPathToVisible(treePath);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
