package com.github.chengyuxing.plugin.rabbit.sql.plugins.kotlin;

import com.github.chengyuxing.common.util.StringUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.search.FilenameIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.structuralsearch.visitor.KotlinRecursiveElementWalkingVisitor;
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry;
import org.jetbrains.kotlin.psi.KtStringTemplateExpression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class KotlinUtil {
    public static String getStringLiteral(PsiElement element) {
        if (element instanceof KtLiteralStringTemplateEntry stringTemplateEntry) {
            return stringTemplateEntry.getText();
        }
        if (element instanceof LeafPsiElement && element.getParent() instanceof KtLiteralStringTemplateEntry stringTemplateEntry) {
            return stringTemplateEntry.getText();
        }
        if (element instanceof KtStringTemplateExpression expression) {
            var text = expression.getText();
            return text.substring(1, text.length() - 1);
        }
        return null;
    }

    public static List<PsiElement> collectSqlRefElements(Project project, Module module, String... sqlRefs) {
        return FilenameIndex.getAllFilesByExt(project, "kt", module.getModuleProductionSourceScope())
                .stream()
                .filter(vf -> vf != null && vf.isValid())
                .map(vf -> PsiManager.getInstance(project).findFile(vf))
                .filter(Objects::nonNull)
                .map(psi -> {
                    final List<PsiElement> psiElements = new ArrayList<>();
                    psi.accept(new KotlinRecursiveElementWalkingVisitor() {
                        @Override
                        public void visitLiteralStringTemplateEntry(@NotNull KtLiteralStringTemplateEntry entry) {
                            var v = entry.getText();
                            if (Objects.nonNull(v) && StringUtils.equalsAny(v, sqlRefs)) {
                                psiElements.add(entry);
                            }
                        }
                    });
                    return psiElements;
                }).flatMap(Collection::stream)
                .toList();
    }
}
