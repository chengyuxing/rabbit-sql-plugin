package com.github.chengyuxing.plugin.rabbit.sql.plugins.kotlin;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.UsageSearchContext;
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry;
import org.jetbrains.kotlin.psi.KtStringTemplateExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class KotlinUtil {
    public static String getStringLiteral(PsiElement element) {
        if (element instanceof KtLiteralStringTemplateEntry stringTemplateEntry) {
            return stringTemplateEntry.getText();
        }
        if (element instanceof LeafPsiElement && element.getParent() instanceof KtLiteralStringTemplateEntry stringTemplateEntry) {
            return stringTemplateEntry.getText();
        }
        if (element instanceof KtStringTemplateExpression expression) {
            var text = expression.getText();
            return text.substring(1, text.length() - 1);
        }
        return null;
    }

    public static List<PsiElement> collectSqlRefElements(Project project, Module module, String... sqlRefs) {
        PsiSearchHelper helper = PsiSearchHelper.getInstance(project);
        var result = new ArrayList<PsiElement>();
        for (var sqlRef : sqlRefs) {
            helper.processElementsWithWord((elem, offset) -> {
                if (elem instanceof KtLiteralStringTemplateEntry entry) {
                    if(Objects.equals(entry.getText(), sqlRef)) {
                        result.add(elem);
                    }
                }
                return true;
            }, module.getModuleProductionSourceScope(), sqlRef, UsageSearchContext.IN_STRINGS, true);
        }
        return result;
    }
}
