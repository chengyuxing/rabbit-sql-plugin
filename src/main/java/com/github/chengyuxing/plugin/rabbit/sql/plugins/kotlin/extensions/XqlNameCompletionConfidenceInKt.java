package com.github.chengyuxing.plugin.rabbit.sql.plugins.kotlin.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.plugins.java.extensions.XqlNameCompletionConfidence;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry;

public class XqlNameCompletionConfidenceInKt extends XqlNameCompletionConfidence {
    @Override
    protected String handlerSqlRef(PsiElement sourceElement) {
        if (sourceElement instanceof LeafPsiElement && sourceElement.getParent() instanceof KtLiteralStringTemplateEntry) {
            var entry = (KtLiteralStringTemplateEntry) sourceElement.getParent();
            return entry.getText();
        }
        return null;
    }
}
