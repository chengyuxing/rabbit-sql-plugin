package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry;

import static com.github.chengyuxing.plugin.rabbit.sql.common.Constants.SQL_NAME_PATTERN;

public class XqlNameAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        // handle kotlin
        if (element instanceof KtLiteralStringTemplateEntry stringTemplateEntry) {
            String sqlRef = stringTemplateEntry.getText();
            if (sqlRef == null) {
                return;
            }
            highlight(sqlRef, element, holder);
        }
        // handle java
        if (element instanceof PsiLiteralExpression literalExpression) {
            String sqlRef = literalExpression.getValue() instanceof String ? (String) literalExpression.getValue() : null;
            if (sqlRef == null) {
                return;
            }
            highlight(sqlRef, element, holder);
        }
    }

    void highlight(String sqlRef, @NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (sqlRef.matches(SQL_NAME_PATTERN)) {
            String sqlName = sqlRef.substring(1);
            var config = XQLConfigManager.getInstance().getActiveConfig(element);
            if (config != null) {
                if (config.getXqlFileManager().contains(sqlName)) {
                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                            .range(element)
                            .textAttributes(DefaultLanguageHighlighterColors.METADATA)
                            .create();
                }
            }
        }
    }
}
