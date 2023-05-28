package com.github.chengyuxing.plugin.rabbit.sql.psi;

import com.github.chengyuxing.plugin.rabbit.sql.common.ResourceCache;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
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
        if (!(originalElement instanceof PsiJavaTokenImpl) || !(originalElement.getParent() instanceof PsiLiteralExpression literalExpression)) {
            return null;
        }
        if (!(element instanceof PsiComment)) {
            return null;
        }
        String sqlRef = literalExpression.getValue() instanceof String ? (String) literalExpression.getValue() : null;
        if (sqlRef == null) {
            return null;
        }
        if (sqlRef.matches(SQL_NAME_PATTERN)) {
            String sqlName = sqlRef.substring(1);
            var resource = ResourceCache.getInstance().getResource(originalElement);
            if (resource != null && resource.getXqlFileManager().contains(sqlName)) {
                var xqlFileManager = resource.getXqlFileManager();
                var sqlTranslator = xqlFileManager.getSqlTranslator();
                String sqlDefinition = xqlFileManager.get(sqlName);
                String sqlContent = HtmlUtil.toHtml(sqlDefinition);
                String xqlFile = element.getContainingFile().getName();

                String doc = DEFINITION_START + element.getText() + DEFINITION_END +
                        CONTENT_START + sqlContent + CONTENT_END +
                        SECTIONS_START;

                var prepareParams = sqlTranslator.getPreparedSql(sqlDefinition, Collections.emptyMap())
                        .getItem2()
                        .stream()
                        .map(name -> xqlFileManager.getNamedParamPrefix() + name)
                        .distinct()
                        .collect(Collectors.joining(" "))
                        .trim();

                if (!prepareParams.equals("")) {
                    doc += SECTION_HEADER_START + "Prepare parameters: " + SECTION_SEPARATOR + "<p>" + prepareParams + SECTION_END;
                }

                var tempParams = StringUtil.getTemplateParameters(sqlTranslator, sqlDefinition);
                if (!tempParams.isEmpty()) {
                    doc += SECTION_HEADER_START + "Template parameters: " + SECTION_SEPARATOR + "<p>" + String.join(" ", tempParams) + SECTION_END;
                }

                doc += SECTION_HEADER_START + "Defined in: " + SECTION_SEPARATOR + "<p>" + xqlFile + SECTION_END +
                        SECTIONS_END;

                return doc;
            }
        }
        return null;
    }

    @Override
    public @Nullable @Nls String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        if (!(originalElement instanceof PsiLiteralExpression literalExpression)) {
            return null;
        }
        if (!(element instanceof PsiComment)) {
            return null;
        }
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
