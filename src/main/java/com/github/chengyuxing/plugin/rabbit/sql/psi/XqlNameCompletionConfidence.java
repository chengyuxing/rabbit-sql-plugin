package com.github.chengyuxing.plugin.rabbit.sql.psi;

import com.intellij.codeInsight.completion.CompletionConfidence;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl;
import com.intellij.util.ThreeState;
import org.jetbrains.annotations.NotNull;

public class XqlNameCompletionConfidence extends CompletionConfidence {
    @Override
    public @NotNull ThreeState shouldSkipAutopopup(@NotNull PsiElement contextElement, @NotNull PsiFile psiFile, int offset) {
        if (!(contextElement instanceof PsiJavaTokenImpl) || !(contextElement.getParent() instanceof PsiLiteralExpression literalExpression)) {
            return ThreeState.UNSURE;
        }
        String sqlRef = literalExpression.getValue() instanceof String ? (String) literalExpression.getValue() : null;
        if (sqlRef == null) {
            return ThreeState.UNSURE;
        }
        if (sqlRef.startsWith("&")) {
            return ThreeState.NO;
        }
        return ThreeState.UNSURE;
    }
}
