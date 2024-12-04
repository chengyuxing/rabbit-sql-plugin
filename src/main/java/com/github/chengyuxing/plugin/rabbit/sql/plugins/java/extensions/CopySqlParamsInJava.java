package com.github.chengyuxing.plugin.rabbit.sql.plugins.java.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.extensions.support.SqlNameIntentionActionInJvmLang;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.icons.AllIcons;
import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CopySqlParamsInJava extends SqlNameIntentionActionInJvmLang implements Iconable {
    @Override
    public void invokeIfSuccess(Project project, PsiElement element, XQLConfigManager.Config config, String sqlName) {
        StringUtil.copySqlParams(config, sqlName);
    }

    @Override
    public boolean isValidFileLanguage(Language language) {
        return language == JavaLanguage.INSTANCE;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Copy sql params in java";
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return "Copy sql parameters to key-value pairs";
    }

    @Override
    public Icon getIcon(int i) {
        return AllIcons.Actions.Copy;
    }
}
