package com.github.chengyuxing.plugin.rabbit.sql.plugins.java.extensions;

import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.annotation.CountQuery;
import com.github.chengyuxing.sql.annotation.XQL;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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
        if (PsiUtil.isParentAXQLMapperInterface(originalElement)) {
            if (sqlRef.matches(SQL_NAME_PATTERN)) {
                return null;
            }
            sqlRef = getSqlRefOnMapperMethod(originalElement);
            if (Objects.isNull(sqlRef)) {
                return null;
            }
        }
        if (sqlRef.matches(SQL_NAME_PATTERN)) {
            var sqlName = sqlRef.substring(1);
            var sqlRefParts = StringUtil.extraSqlReference(sqlName);
            var alias = sqlRefParts.getItem1();
            var config = xqlConfigManager.getActiveConfig(originalElement);
            if (Objects.nonNull(config) && config.getXqlFileManager().contains(sqlName)) {
                var xqlFileManager = config.getXqlFileManager();
                var resource = xqlFileManager.getResource(alias);
                if (Objects.isNull(resource)) {
                    return null;
                }
                var fileDescription = resource.getDescription();
                var sql = xqlFileManager.getSqlObject(sqlName);
                var sqlDefinition = sql.getContent();
                var sqlContent = HtmlUtil.highlightSql(sqlDefinition);
                var sqlDescription = sql.getDescription();
                var xqlFile = element instanceof PsiComment ? element.getContainingFile().getName() : FileResource.getFileName(resource.getFilename(), true);

                var doc = DEFINITION_START + HtmlUtil.wrap("span", element.getText(), HtmlUtil.Color.EMPTY);

                if (!sqlDescription.trim().isEmpty()) {
                    if (!fileDescription.trim().isEmpty()) {
                        sqlDescription = fileDescription + ": " + sqlDescription;
                    }
                    doc += HtmlUtil.pre(sqlDescription, HtmlUtil.Color.LIGHT);
                }

                doc += DEFINITION_END + CONTENT_START + sqlContent + CONTENT_END +
                        SECTIONS_START;

                var params = String.join("  ", StringUtil.getParamsMappingInfo(config.getSqlGenerator(), sqlDefinition)
                                .keySet()).trim();

                if (!params.isEmpty()) {
                    doc += SECTION_HEADER_START + "Parameters: " + SECTION_SEPARATOR + "<p>" + params + SECTION_END;
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
        if (!(element instanceof PsiComment)) {
            return null;
        }
        String sqlRef = PsiUtil.getJvmLangLiteral(originalElement);
        if (Objects.isNull(sqlRef)) {
            return null;
        }
        if (!sqlRef.matches(SQL_NAME_PATTERN)) {
            sqlRef = getSqlRefOnMapperMethod(originalElement);
            if (Objects.isNull(sqlRef)) {
                return null;
            }
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

    private static String getSqlRefOnMapperMethod(PsiElement originalElement) {
        var alias = PsiUtil.getXQLMapperAlias(originalElement);
        if (Objects.isNull(alias)) {
            return null;
        }
        var psiAttr = PsiUtil.getIfElementIsAnnotationAttr(originalElement, XQL.class.getName(), "value");
        if (Objects.isNull(psiAttr)) {
            psiAttr = PsiUtil.getIfElementIsAnnotationAttr(originalElement, CountQuery.class.getName(), "value");
        }
        if (Objects.isNull(psiAttr)) {
            return null;
        }
        var psiAttrValue = PsiUtil.getAnnoTextValue(psiAttr);
        return "&" + XQLFileManager.encodeSqlReference(alias, psiAttrValue);
    }
}
