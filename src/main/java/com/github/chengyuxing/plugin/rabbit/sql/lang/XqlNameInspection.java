package com.github.chengyuxing.plugin.rabbit.sql.lang;

import com.github.chengyuxing.common.utils.StringUtil;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

public class XqlNameInspection extends AbstractBaseJavaLocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            boolean hasBaki = false;
            String bakiField = "";
            String bakiVar = "";

            @Override
            public void visitImportList(PsiImportList list) {
                var bakiImport = list.findSingleClassImportStatement("com.github.chengyuxing.sql.Baki");
                var bakiDaoImport = list.findSingleClassImportStatement("com.github.chengyuxing.sql.BakiDao");
                if (bakiImport != null || bakiDaoImport != null) {
                    hasBaki = true;
                }
            }

            @Override
            public void visitLocalVariable(PsiLocalVariable variable) {
                bakiVar = variable.getName();
            }

            @Override
            public void visitField(PsiField field) {
                bakiField = field.getName();
            }

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                if (hasBaki) {
                    expression.getMethodExpression().getQualifierExpression();
                    String callName = expression.getMethodExpression().getQualifiedName();
                    if (StringUtil.startsWiths(callName,
                            bakiField + ".query",
                            bakiVar + ".query")) {
                        for (PsiExpression psiExpression : expression.getArgumentList().getExpressions()) {
                            if (psiExpression instanceof PsiLiteralExpression) {
                                Object argv = ((PsiLiteralExpression) psiExpression).getValue();
                                if (argv != null) {
                                    String sql = argv.toString();
                                    if (sql.startsWith("&")) {
                                        String sqlName = sql.substring(1);
                                        holder.registerProblem(expression,
                                                "Quick look sql definition.",
                                                ProblemHighlightType.INFORMATION);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }
}
