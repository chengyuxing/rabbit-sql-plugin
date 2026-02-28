package com.github.chengyuxing.plugin.rabbit.sql.plugins.java;

import com.github.chengyuxing.common.util.StringUtils;
import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.annotation.CountQuery;
import com.github.chengyuxing.sql.annotation.XQL;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.UsageSearchContext;

import java.util.*;

public class JavaUtil {
    public static String getStringLiteral(PsiElement element) {
        if (element instanceof PsiLiteralExpression literalExpression) {
            return literalExpression.getValue() instanceof String ? (String) literalExpression.getValue() : null;
        }
        if (element instanceof PsiJavaTokenImpl && element.getParent() instanceof PsiLiteralExpression literalExpression) {
            return literalExpression.getValue() instanceof String ? (String) literalExpression.getValue() : null;
        }
        return null;
    }

    public static Set<PsiElement> collectSqlRefElements(Project project, Module module, String... sqlRefs) {
        PsiSearchHelper helper = PsiSearchHelper.getInstance(project);
        var result = new HashSet<PsiElement>();
        for (var sqlRef : sqlRefs) {
            // @XQLMapper
            var name = XQLFileManager.decodeSqlReference(sqlRef).getItem2();
            helper.processElementsWithWord((elem, offset) -> {
                if (elem.getContainingFile() instanceof PsiJavaFile pjf) {
                    var psiClasses = pjf.getClasses();
                    if (psiClasses.length > 0) {
                        var psiClass = psiClasses[0];
                        var psiAlias = PsiUtil.getXQLMapperAlias(psiClass);
                        if (Objects.nonNull(psiAlias)) {
                            result.addAll(collectSqlRefElementInXqlMapper(pjf, sqlRef));
                        }
                    }
                }
                return true;
            }, module.getModuleProductionSourceScope(), name, UsageSearchContext.IN_CODE, true);
            // java string literal
            helper.processElementsWithWord((elem, offset) -> {
                if (elem instanceof PsiLiteralExpression literal && literal.getValue() instanceof String value) {
                    if (Objects.equals(value, sqlRef)) {
                        result.add(literal);
                    }
                }
                return true;
            }, module.getModuleProductionSourceScope(), sqlRef, UsageSearchContext.IN_STRINGS, true);
        }
        return result;
    }

    public static Set<PsiElement> collectSqlRefElementInXqlMapper(PsiJavaFile psi, String sqlRef) {
        var psiElements = new HashSet<PsiElement>();
        var psiClasses = psi.getClasses();
        if (psiClasses.length > 0) {
            var psiClass = psiClasses[0];
            var psiAlias = PsiUtil.getXQLMapperAlias(psiClass);
            if (Objects.nonNull(psiAlias)) {
                var psiMethods = psiClass.getMethods();
                for (var psiMethod : psiMethods) {
                    if (PsiUtil.isXQLMapperMethod(psiMethod)) {
                        var annoAttr = PsiUtil.getMethodAnnoValue(psiMethod, CountQuery.class.getName(), "value");
                        if (Objects.nonNull(annoAttr)) {
                            var cQAttrValue = PsiUtil.getAnnoTextValue(annoAttr);
                            if (!StringUtils.isEmpty(cQAttrValue)) {
                                if (Objects.equals(sqlRef, "&" + XQLFileManager.encodeSqlReference(psiAlias, cQAttrValue))) {
                                    psiElements.add(annoAttr);
                                }
                            }
                        }
                        var psiMethodAnnoAttr = PsiUtil.getMethodAnnoValue(psiMethod, XQL.class.getName(), "value");
                        if (Objects.nonNull(psiMethodAnnoAttr)) {
                            var attrValue = PsiUtil.getAnnoTextValue(psiMethodAnnoAttr);
                            // @XQL(type = Type.insert)
                            // int addGuest(DataRow dataRow);
                            if (Objects.equals("", attrValue)) {
                                if (Objects.equals(sqlRef, "&" + XQLFileManager.encodeSqlReference(psiAlias, psiMethod.getName()))) {
                                    psiElements.add(psiMethod);
                                }

                                // @XQL("queryGuests")
                                // Stream<Guest> queryGuests(Map<String, Object> args);
                            } else {
                                if (Objects.equals(sqlRef, "&" + XQLFileManager.encodeSqlReference(psiAlias, attrValue))) {
                                    psiElements.add(psiMethodAnnoAttr);
                                }
                            }

                            // List<DataRow> queryGuests(Map<String, Object> args);
                        } else {
                            if (Objects.equals(sqlRef, "&" + XQLFileManager.encodeSqlReference(psiAlias, psiMethod.getName()))) {
                                psiElements.add(psiMethod);
                            }
                        }
                    }
                }
            }
        }
        return psiElements;
    }
}
