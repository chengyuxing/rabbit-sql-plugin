package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.common.tuple.Quadruple;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementVisitor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

public class GotoXqlFileAction extends AnAction {
    private final JTree tree;

    public GotoXqlFileAction(JTree tree) {
        super("Go To Definition");
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (Objects.nonNull(nodeSource) && nodeSource.type() == XqlTreeNodeData.Type.XQL_FRAGMENT) {
            @SuppressWarnings("unchecked")
            var sqlMeta = (Quadruple<String, String, String, XQLConfigManager.Config>) nodeSource.source();
            var alias = sqlMeta.getItem1();
            var name = sqlMeta.getItem2();
            var config = sqlMeta.getItem4();
            var xqlVf = ProjectFileUtil.findXqlByAlias(alias, config);
            if (Objects.nonNull(xqlVf) && xqlVf.exists()) {
                var psi = PsiManager.getInstance(project).findFile(xqlVf);
                if (Objects.nonNull(psi)) {
                    ProgressManager.checkCanceled();
                    psi.acceptChildren(new PsiRecursiveElementVisitor() {
                        @Override
                        public void visitElement(@NotNull PsiElement element) {
                            if (element instanceof PsiComment comment) {
                                if (comment.getText().matches("/\\*\\s*\\[\\s*" + name + "\\s*]\\s*\\*/")) {
                                    var nav = comment.getNavigationElement();
                                    NavigationUtil.activateFileWithPsiElement(nav);
                                    return;
                                }
                            }
                            if (element instanceof GeneratedParserUtilBase.DummyBlock) {
                                super.visitElement(element);
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(false);
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (Objects.nonNull(nodeSource) && nodeSource.type() == XqlTreeNodeData.Type.XQL_FRAGMENT) {
            e.getPresentation().setEnabled(true);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
