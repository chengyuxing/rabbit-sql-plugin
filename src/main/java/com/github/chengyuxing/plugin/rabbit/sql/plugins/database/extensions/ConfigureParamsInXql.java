package com.github.chengyuxing.plugin.rabbit.sql.plugins.database.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.extensions.support.SqlNameIntentionActionInXql;
import com.github.chengyuxing.plugin.rabbit.sql.ui.EntityGenerateDialog;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.github.chengyuxing.sql.XQLFileManager;
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

public class ConfigureParamsInXql extends SqlNameIntentionActionInXql implements Iconable {
    @Override
    public void invokeIfSuccess(Project project, PsiElement element, XQLConfigManager.Config config, String sqlName) {
        var sqlDefinition = config.getXqlFileManager().get(sqlName);
        var fieldMapping = StringUtil.getParamsMappingInfo(config.getSqlGenerator(), sqlDefinition, false);
        if (fieldMapping.isEmpty()) {
            return;
        }
        var sqlPart = XQLFileManager.decodeSqlReference(sqlName);
        ApplicationManager.getApplication().invokeLater(() -> new EntityGenerateDialog(project, sqlPart.getItem1(), sqlPart.getItem2(), config, fieldMapping).showAndGet());
    }

    @Override
    protected boolean isValidFileExtension(String extension) {
        return Objects.equals(extension, "xql");
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Configure params";
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return "Configure params";
    }

    @Override
    public Icon getIcon(int i) {
        return AllIcons.Actions.Compile;
    }
}
