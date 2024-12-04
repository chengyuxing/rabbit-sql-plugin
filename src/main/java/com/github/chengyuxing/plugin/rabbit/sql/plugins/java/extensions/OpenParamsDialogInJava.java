package com.github.chengyuxing.plugin.rabbit.sql.plugins.java.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.extensions.support.SqlNameIntentionActionInJvmLang;
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
import java.util.Objects;

public class OpenParamsDialogInJava extends SqlNameIntentionActionInJvmLang implements Iconable {
    @Override
    public void invokeIfSuccess(Project project, PsiElement element, XQLConfigManager.Config config, String sqlName) {
        ApplicationManager.getApplication().invokeLater(() -> new DynamicSqlCalcDialog(sqlName, config, project).showAndGet());
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Execute dynamic sql in java";
    }

    @Override
    public @IntentionName @NotNull String getText() {
        if (Objects.nonNull(intentionTarget)) {
            return "Execute '" + intentionTarget.substring(1) + "'";
        }
        return "Execute dynamic sql";
    }

    @Override
    public Icon getIcon(int flags) {
        return AllIcons.Actions.Execute;
    }
}
