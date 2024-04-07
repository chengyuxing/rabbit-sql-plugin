package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.common.DatasourceManager;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.extensions.support.SqlNameIntentionActionInXql;
import com.github.chengyuxing.plugin.rabbit.sql.ui.DynamicSqlCalcDialog;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class OpenParamsDialogInXql extends SqlNameIntentionActionInXql implements Iconable {
    @Override
    public void invokeIfSuccess(Project project, PsiElement element, XQLConfigManager.Config config, String sqlName) {
        var dsResource = DatasourceManager.getInstance().getResource(project);
        ApplicationManager.getApplication().invokeLater(() -> new DynamicSqlCalcDialog(sqlName, config, dsResource).showAndGet());
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Execute dynamic sql In xql";
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return "Execute dynamic sql";
    }

    @Override
    public Icon getIcon(int flags) {
        return AllIcons.Actions.Execute;
    }
}
