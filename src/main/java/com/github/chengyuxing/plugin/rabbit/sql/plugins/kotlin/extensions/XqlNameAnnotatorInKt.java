package com.github.chengyuxing.plugin.rabbit.sql.plugins.kotlin.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.plugins.java.extensions.XqlNameAnnotator;
import com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry;

public class XqlNameAnnotatorInKt extends XqlNameAnnotator {
    @Override
    protected String getSqlRef(PsiElement element) {
        if (element instanceof KtLiteralStringTemplateEntry stringTemplateEntry) {
            return stringTemplateEntry.getText();
        }
        return null;
    }
}
