package com.github.chengyuxing.plugin.rabbit.sql.lang;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.psi.*;
import com.intellij.sql.psi.SqlExpression;
import com.intellij.sql.psi.SqlVisitor;
import org.jetbrains.annotations.NotNull;

public class XqlNameAnnotator implements Annotator {
    boolean hasBaki;

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof PsiImportList) {
            var bakiImport = ((PsiImportList) element).findSingleClassImportStatement("com.github.chengyuxing.sql.Baki");
            var bakiDaoImport = ((PsiImportList) element).findSingleClassImportStatement("com.github.chengyuxing.sql.BakiDao");
            if (bakiImport != null || bakiDaoImport != null) {
                hasBaki = true;
            }
        }
        if (hasBaki) {
            if (element instanceof PsiMethodCallExpression) {
                for (PsiExpression psiExpression : ((PsiMethodCallExpression) element).getArgumentList().getExpressions()) {
                    if (psiExpression instanceof PsiLiteralExpression) {
                        Object argv = ((PsiLiteralExpression) psiExpression).getValue();
                        if (argv != null) {
                            String sql = argv.toString();
                            if (sql.startsWith("&")) {
                                String sqlName = sql.substring(1);
                                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                                        .range(psiExpression)
                                        .textAttributes(DefaultLanguageHighlighterColors.METADATA)
                                        .tooltip("<i>Quick</i> look sql definition.")
                                        .create();
                            } else {
                                new SqlVisitor().visitSqlExpression((SqlExpression) psiExpression);
                            }
                        }
                    }
                }
            }
        }
    }
}
