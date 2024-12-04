package com.github.chengyuxing.plugin.rabbit.sql.plugins.java.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.extensions.SqlNameReference;
import com.github.chengyuxing.plugin.rabbit.sql.extensions.XqlNameReference;
import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.github.chengyuxing.sql.annotation.CountQuery;
import com.github.chengyuxing.sql.annotation.XQL;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class XqlNameReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PsiLiteralExpression.class), new PsiReferenceProvider() {
            @Override
            public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                PsiLiteralExpression literalExpression = (PsiLiteralExpression) element;
                String value = literalExpression.getValue() instanceof String ?
                        (String) literalExpression.getValue() : null;
                return createPsiReferences(element, value);
            }
        });
    }

    protected PsiReference @NotNull [] createPsiReferences(@NotNull PsiElement element, String value) {
        if (value != null && value.startsWith("&")) {
            if (PsiUtil.isParentAXQLMapperInterface(element)) {
                return PsiReference.EMPTY_ARRAY;
            }
            var sqlPath = value.substring(1);
            if (sqlPath.trim().isEmpty()) {
                return PsiReference.EMPTY_ARRAY;
            }
            var property = new TextRange(2, value.length() + 1);
            return new PsiReference[]{new XqlNameReference(element, property, sqlPath)};
        }
        if (PsiUtil.isParentAXQLMapperInterface(element)) {
            var psiAttrValue = PsiUtil.getIfElementIsAnnotationAttr(element, XQL.class.getName(), "value");
            if (Objects.isNull(psiAttrValue)) {
                psiAttrValue = PsiUtil.getIfElementIsAnnotationAttr(element, CountQuery.class.getName(), "value");
            }
            if (Objects.isNull(psiAttrValue)) {
                return PsiReference.EMPTY_ARRAY;
            }
            var sqlName = PsiUtil.getAnnoTextValue(psiAttrValue);
            if (sqlName.trim().isEmpty()) {
                return PsiReference.EMPTY_ARRAY;
            }
            var alias = PsiUtil.getXQLMapperAlias(element);
            if (Objects.isNull(alias)) {
                return PsiReference.EMPTY_ARRAY;
            }
            var property = new TextRange(1, sqlName.length() + 1);
            return new PsiReference[]{new SqlNameReference(element, property, alias + "." + sqlName)};
        }
        return PsiReference.EMPTY_ARRAY;
    }
}
