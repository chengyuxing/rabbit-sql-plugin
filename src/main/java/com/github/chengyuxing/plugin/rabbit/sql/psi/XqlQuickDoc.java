package com.github.chengyuxing.plugin.rabbit.sql.psi;

import com.github.chengyuxing.plugin.rabbit.sql.common.ResourceCache;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.stream.Collectors;

import static com.github.chengyuxing.plugin.rabbit.sql.common.Constants.SQL_NAME_PATTERN;
import static com.intellij.lang.documentation.DocumentationMarkup.*;

public class XqlQuickDoc extends AbstractDocumentationProvider {
    @Override
    public @Nullable @Nls String generateHoverDoc(@NotNull PsiElement element, @Nullable PsiElement originalElement) {
        return generateDoc(element, originalElement);
    }

    @Override
    public @Nullable @Nls String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        if (!(originalElement instanceof PsiJavaTokenImpl) || !(originalElement.getParent() instanceof PsiLiteralExpression)) {
            return null;
        }
        if (!(element instanceof PsiComment)) {
            return null;
        }
        PsiLiteralExpression literalExpression = (PsiLiteralExpression) originalElement.getParent();
        String sqlRef = literalExpression.getValue() instanceof String ? (String) literalExpression.getValue() : null;
        if (sqlRef == null) {
            return null;
        }
        if (sqlRef.matches(SQL_NAME_PATTERN)) {
            String sqlName = sqlRef.substring(1);
            var resource = ResourceCache.getInstance().getResource(originalElement);
            if (resource != null && resource.getXqlFileManager().contains(sqlName)) {
                var xqlFileManager = resource.getXqlFileManager();
                String sqlDefinition = xqlFileManager.get(sqlName);
                String sqlContent = HtmlUtil.toHtml(sqlDefinition);
                String xqlFile = element.getContainingFile().getName();

                var params = xqlFileManager.getSqlTranslator().getPreparedSql(sqlDefinition, Collections.emptyMap())
                        .getItem2()
                        .stream()
                        .map(name -> xqlFileManager.getNamedParamPrefix() + name)
                        .distinct()
                        .collect(Collectors.joining(" "));

                return DEFINITION_START + element.getText() + DEFINITION_END +
                        CONTENT_START + sqlContent + CONTENT_END +
                        SECTIONS_START +
                        SECTION_HEADER_START + "Parameters: " + SECTION_SEPARATOR + "<p>" + params + SECTION_END +
                        SECTION_HEADER_START + "Defined in: " + SECTION_SEPARATOR + "<p>" + xqlFile + SECTION_END +
                        SECTIONS_END;
            }
        }
        return null;
    }

    @Override
    public @Nullable @Nls String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        if (!(originalElement instanceof PsiLiteralExpression)) {
            return null;
        }
        if (!(element instanceof PsiComment)) {
            return null;
        }
        PsiLiteralExpression literalExpression = (PsiLiteralExpression) originalElement;
        String sqlRef = literalExpression.getValue() instanceof String ? (String) literalExpression.getValue() : null;
        if (sqlRef == null) {
            return null;
        }
        if (sqlRef.matches(SQL_NAME_PATTERN)) {
            String sqlName = sqlRef.substring(1);
            var resource = ResourceCache.getInstance().getResource(originalElement);
            if (resource != null && resource.getXqlFileManager().contains(sqlName)) {
                String xqlFile = element.getContainingFile().getName();
                return xqlFile + "<br>" + element.getText() + " -> " + sqlRef;
            }
        }
        return null;
    }
}
