package com.github.chengyuxing.plugin.rabbit.sql.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class XqlNameReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PsiLiteralExpression.class), new PsiReferenceProvider() {
            @Override
            public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                PsiLiteralExpression literalExpression = (PsiLiteralExpression) element;
                String value = literalExpression.getValue() instanceof String ?
                        (String) literalExpression.getValue() : null;
                if (value != null && value.startsWith("&")) {
                    var sqlPath = value.substring(1);
                    if (sqlPath.trim().equals("")) {
                        return PsiReference.EMPTY_ARRAY;
                    }
                    var property = new TextRange(2, value.length() + 1);
                    return new PsiReference[]{new XqlNameReference(element, property)};
                }
                return PsiReference.EMPTY_ARRAY;
            }
        });
    }
}
