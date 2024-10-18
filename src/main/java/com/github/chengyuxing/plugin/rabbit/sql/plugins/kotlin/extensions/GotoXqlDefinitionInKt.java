package com.github.chengyuxing.plugin.rabbit.sql.plugins.kotlin.extensions;

import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.plugin.rabbit.sql.extensions.GotoXqlDefinition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry;

public class GotoXqlDefinitionInKt extends GotoXqlDefinition {
    @Override
    protected Pair<String, PsiElement> handlerSqlRef(PsiElement sourceElement) {
        if (sourceElement instanceof LeafPsiElement && sourceElement.getParent() instanceof KtLiteralStringTemplateEntry) {
            var entry = (KtLiteralStringTemplateEntry) sourceElement.getParent();
            return Pair.of(entry.getText(), sourceElement);
        }
        return null;
    }
}
