package com.github.chengyuxing.plugin.rabbit.sql.plugins.java.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.github.chengyuxing.sql.annotation.CountQuery;
import com.github.chengyuxing.sql.annotation.XQL;
import com.intellij.codeInsight.completion.CompletionConfidence;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl;
import com.intellij.util.ThreeState;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class XqlNameCompletionConfidence extends CompletionConfidence {
    @Override
    public @NotNull ThreeState shouldSkipAutopopup(@NotNull PsiElement contextElement, @NotNull PsiFile psiFile, int offset) {
        String sqlRef = handlerSqlRef(contextElement);
        if (Objects.isNull(sqlRef)) {
            return ThreeState.UNSURE;
        }

        if (sqlRef.startsWith("&")) {
            if (PsiUtil.isParentAXQLMapperInterface(contextElement)) {
                return ThreeState.UNSURE;
            }
            return ThreeState.NO;
        }

        if (PsiUtil.isParentAXQLMapperInterface(contextElement)) {
            if (PsiUtil.getIfElementIsAnnotationAttr(contextElement, XQL.class.getName(), "value") != null) {
                return ThreeState.NO;
            }
            if (PsiUtil.getIfElementIsAnnotationAttr(contextElement, CountQuery.class.getName(), "value") != null) {
                return ThreeState.NO;
            }
            return ThreeState.UNSURE;
        }

        return ThreeState.UNSURE;
    }

    protected String handlerSqlRef(PsiElement sourceElement) {
        if (!(sourceElement instanceof PsiJavaTokenImpl) || !(sourceElement.getParent() instanceof PsiLiteralExpression literalExpression)) {
            return null;
        }
        return literalExpression.getValue() instanceof String ? (String) literalExpression.getValue() : null;
    }
}
