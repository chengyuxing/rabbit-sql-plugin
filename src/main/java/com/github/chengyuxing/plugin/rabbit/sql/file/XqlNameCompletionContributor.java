package com.github.chengyuxing.plugin.rabbit.sql.file;

import com.github.chengyuxing.plugin.rabbit.sql.common.Store;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class XqlNameCompletionContributor extends CompletionContributor {
    public XqlNameCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(PsiLiteralExpression.class), new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                Store.INSTANCE.xqlFileManager.names().forEach(name -> {
                    result.addElement(LookupElementBuilder.create("&" + name));
                });
            }
        });
    }
}
