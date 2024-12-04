package com.github.chengyuxing.plugin.rabbit.sql.plugins.java.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
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
        if (PsiUtil.isParentAXQLMapperInterface(element)) {
            return;
        }
        String sqlRef = getSqlRef(element);
        if (sqlRef == null) {
            return;
        }
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

    protected String getSqlRef(PsiElement element) {
        if (element instanceof PsiLiteralExpression literalExpression) {
            return literalExpression.getValue() instanceof String ? (String) literalExpression.getValue() : null;
        }
        return null;
    }
}
