package com.github.chengyuxing.plugin.rabbit.sql.psi;

import com.github.chengyuxing.plugin.rabbit.sql.common.Store;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import org.jetbrains.annotations.NotNull;

import static com.github.chengyuxing.plugin.rabbit.sql.common.Constants.SQL_NAME_PATTERN;

public class XqlNameAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof PsiLiteralExpression) {
            PsiLiteralExpression literalExpression = (PsiLiteralExpression) element;
            String sqlRef = literalExpression.getValue() instanceof String ? (String) literalExpression.getValue() : null;
            if (sqlRef == null) {
                return;
            }
            if (sqlRef.matches(SQL_NAME_PATTERN)) {
                String sqlName = sqlRef.substring(1);
                if (Store.INSTANCE.xqlFileManager.contains(sqlName)) {
                    String sqlDefinition = Store.INSTANCE.xqlFileManager.get(sqlName);
                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                            .range(element)
                            .textAttributes(DefaultLanguageHighlighterColors.METADATA)
                            .tooltip(HtmlUtil.toHtml(sqlDefinition))
                            .create();
                }
            }
        }
    }
}