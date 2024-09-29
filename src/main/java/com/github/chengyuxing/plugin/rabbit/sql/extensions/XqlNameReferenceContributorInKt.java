package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtStringTemplateExpression;

public class XqlNameReferenceContributorInKt extends XqlNameReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(KtStringTemplateExpression.class), new PsiReferenceProvider() {
            @Override
            public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                String value = element.getText();
                value = value.substring(1, value.length() - 1);
                return createPsiReferences(element, value);
            }
        });
    }
}
