package com.github.chengyuxing.plugin.rabbit.sql.lang;

import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethodCallExpression;
import org.jetbrains.annotations.NotNull;

import static com.github.chengyuxing.plugin.rabbit.sql.XqlFileListenOnStartup.xqlFileManager;

public class XqlNameAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof PsiMethodCallExpression) {
            for (PsiExpression psiExpression : ((PsiMethodCallExpression) element).getArgumentList().getExpressions()) {
                if (psiExpression instanceof PsiLiteralExpression) {
                    Object argv = ((PsiLiteralExpression) psiExpression).getValue();
                    if (argv != null) {
                        String sql = argv.toString();
                        if (sql.matches("^&\\w+\\..+")) {
                            String sqlName = sql.substring(1);
                            if (xqlFileManager.contains(sqlName)) {
                                String sqlDefinition = xqlFileManager.get(sqlName);
                                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                                        .range(psiExpression)
                                        .textAttributes(DefaultLanguageHighlighterColors.METADATA)
                                        .tooltip(HtmlUtil.toHtml(sqlDefinition))
                                        .create();
                            }
                        }
                    }
                }
            }
        }
    }
}
