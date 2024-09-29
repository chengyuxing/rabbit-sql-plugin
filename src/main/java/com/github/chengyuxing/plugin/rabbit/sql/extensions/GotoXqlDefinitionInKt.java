package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.github.chengyuxing.common.tuple.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry;

public class GotoXqlDefinitionInKt extends GotoXqlDefinition {
    @Override
    protected Pair<String, PsiElement> handlerSqlRef(PsiElement sourceElement) {
        if (sourceElement instanceof LeafPsiElement && sourceElement.getParent() instanceof KtLiteralStringTemplateEntry entry) {
            return Pair.of(entry.getText(), sourceElement);
        }
        return null;
    }
}
