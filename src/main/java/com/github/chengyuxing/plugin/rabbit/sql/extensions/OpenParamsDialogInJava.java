package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.common.DatasourceCache;
import com.github.chengyuxing.plugin.rabbit.sql.common.ResourceCache;
import com.github.chengyuxing.plugin.rabbit.sql.ui.DynamicSqlCalcDialog;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public class OpenParamsDialogInJava extends CopySqlDefinition {
    private static final Logger log = Logger.getInstance(OpenParamsDialogInJava.class);

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        try {
            @SuppressWarnings("DataFlowIssue") var sqlName = ((PsiLiteralExpression) element.getParent()).getValue().toString().substring(1);
            var resource = ResourceCache.getInstance().getResource(element);
            var dsResource = DatasourceCache.getInstance().getResource(project);
            ApplicationManager.getApplication().invokeLater(() -> new DynamicSqlCalcDialog(sqlName, resource, dsResource).showAndGet());
        } catch (Exception e) {
            log.warn(e);
        }
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Test dynamic sql";
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return "Test dynamic sql";
    }
}
