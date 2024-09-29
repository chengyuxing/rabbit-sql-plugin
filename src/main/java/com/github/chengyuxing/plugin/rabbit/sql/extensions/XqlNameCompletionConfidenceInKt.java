package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry;

public class XqlNameCompletionConfidenceInKt extends XqlNameCompletionConfidence {
    @Override
    protected String handlerSqlRef(PsiElement sourceElement) {
        if (sourceElement instanceof LeafPsiElement && sourceElement.getParent() instanceof KtLiteralStringTemplateEntry entry) {
            return entry.getText();
        }
        return null;
    }
}
