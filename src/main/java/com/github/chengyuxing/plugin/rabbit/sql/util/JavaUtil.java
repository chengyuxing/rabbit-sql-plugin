package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.common.utils.StringUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.annotation.CountQuery;
import com.github.chengyuxing.sql.annotation.XQL;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl;
import com.intellij.psi.search.FilenameIndex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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

    public static List<PsiElement> collectSqlRefElements(Project project, Module module, String... sqlRefs) {
        return FilenameIndex.getAllFilesByExt(project, "java", module.getModuleProductionSourceScope())
                .stream()
                .filter(vf -> vf != null && vf.isValid())
                .map(vf -> PsiManager.getInstance(project).findFile(vf))
                .filter(Objects::nonNull)
                .map(psi -> {
                    final List<PsiElement> psiElements = new ArrayList<>();
                    var psiClasses = ((PsiJavaFile) psi).getClasses();
                    if (psiClasses.length > 0) {
                        var psiClass = psiClasses[0];
                        var psiAlias = PsiUtil.getXQLMapperAlias(psiClass);
                        if (Objects.nonNull(psiAlias)) {
                            var psiMethods = psiClass.getMethods();
                            for (var psiMethod : psiMethods) {
                                if (PsiUtil.isXQLMapperMethod(psiMethod)) {
                                    for (var sqlRef : sqlRefs) {
                                        var annoAttr = PsiUtil.getMethodAnnoValue(psiMethod, CountQuery.class.getName(), "value");
                                        if (Objects.nonNull(annoAttr)) {
                                            var cQAttrValue = PsiUtil.getAnnoTextValue(annoAttr);
                                            if (!Objects.equals("", cQAttrValue)) {
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
                        } else {
                            psi.accept(new JavaRecursiveElementWalkingVisitor() {
                                @Override
                                public void visitLiteralExpression(@NotNull PsiLiteralExpression expression) {
                                    String v = expression.getValue() instanceof String ? (String) expression.getValue() : null;
                                    if (Objects.nonNull(v) && StringUtil.equalsAny(v, sqlRefs)) {
                                        psiElements.add(expression);
                                    }
                                    // unnecessary to do that anymore.
                                    // super.visitElement(expression);
                                }
                            });
                        }
                    }
                    return psiElements;
                }).flatMap(Collection::stream)
                .toList();
    }
}
