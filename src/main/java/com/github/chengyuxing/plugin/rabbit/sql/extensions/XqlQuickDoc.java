package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.github.chengyuxing.common.script.lexer.FlowControlLexer;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.github.chengyuxing.sql.utils.SqlUtil;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.github.chengyuxing.plugin.rabbit.sql.common.Constants.SQL_NAME_PATTERN;
import static com.intellij.lang.documentation.DocumentationMarkup.*;

public class XqlQuickDoc extends AbstractDocumentationProvider {
    private final XQLConfigManager xqlConfigManager = XQLConfigManager.getInstance();

    @Override
    public @Nullable @Nls String generateHoverDoc(@NotNull PsiElement element, @Nullable PsiElement originalElement) {
        return generateDoc(element, originalElement);
    }

    @Override
    public @Nullable @Nls String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        String sqlRef = PsiUtil.getJvmLangLiteral(originalElement);
        if (Objects.isNull(sqlRef)) {
            return null;
        }
        String sqlRef = literalExpression.getValue() instanceof String ? (String) literalExpression.getValue() : null;
        if (sqlRef == null) {
            return null;
        }
        if (sqlRef.matches(SQL_NAME_PATTERN)) {
            var sqlName = sqlRef.substring(1);
            var sqlRefParts = StringUtil.extraSqlReference(sqlName);
            var alias = sqlRefParts.getItem1();
            var config = xqlConfigManager.getActiveConfig(originalElement);
            if (Objects.nonNull(config) && config.getXqlFileManager().contains(sqlName)) {
                var xqlFileManager = config.getXqlFileManager();
                var resource = xqlFileManager.getResource(alias);
                var fileDescription = resource.getDescription();
                var sql = xqlFileManager.getSqlObject(sqlName);
                var sqlDefinition = SqlUtil.trimEnd(sql.getContent());
                var sqlContent = HtmlUtil.highlightSql(sqlDefinition);
                var sqlDescription = sql.getDescription();
                var xqlFile = element instanceof PsiComment ? element.getContainingFile().getName() : resource.getFilename();

                var doc = DEFINITION_START + HtmlUtil.wrap("span", element.getText(), HtmlUtil.Color.EMPTY);

                if (!sqlDescription.trim().isEmpty()) {
                    if (Objects.nonNull(fileDescription) && !fileDescription.trim().isEmpty()) {
                        sqlDescription = fileDescription + ": " + sqlDescription;
                    }
                    doc += HtmlUtil.pre(sqlDescription, HtmlUtil.Color.LIGHT);
                }

                doc += DEFINITION_END + CONTENT_START + sqlContent + CONTENT_END +
                        SECTIONS_START;

                for (String keyword : FlowControlLexer.KEYWORDS) {
                    sqlDefinition = sqlDefinition.replaceAll("(?i)--\\s*" + keyword, keyword);
                }
                var prepareParams = config.getSqlGenerator().generatePreparedSql(sqlDefinition, Map.of())
                        .getItem2()
                        .keySet()
                        .stream()
                        .map(name -> xqlFileManager.getNamedParamPrefix() + name)
                        .distinct()
                        .collect(Collectors.joining("  "))
                        .trim();

                if (!prepareParams.isEmpty()) {
                    doc += SECTION_HEADER_START + "Named parameters: " + SECTION_SEPARATOR + "<p>" + prepareParams + SECTION_END;
                }

                var tempParams = StringUtil.getTemplateParameters(sqlDefinition);
                if (!tempParams.isEmpty()) {
                    doc += SECTION_HEADER_START + "Template parameters: " + SECTION_SEPARATOR + "<p>" + String.join("  ", tempParams) + SECTION_END;
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
            var config = xqlConfigManager.getActiveConfig(originalElement);
            if (config != null && config.getXqlFileManager().contains(sqlName)) {
                String xqlFile = element.getContainingFile().getName();
                return xqlFile + "<br>" + element.getText() + " -> " + sqlRef;
            }
        }
        return null;
    }
}
