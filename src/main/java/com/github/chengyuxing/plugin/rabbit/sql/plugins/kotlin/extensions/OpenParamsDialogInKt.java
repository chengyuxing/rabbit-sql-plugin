package com.github.chengyuxing.plugin.rabbit.sql.plugins.kotlin.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.extensions.OpenParamsDialogInJava;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.lang.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.KotlinLanguage;

public class OpenParamsDialogInKt extends OpenParamsDialogInJava {
    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Execute dynamic sql in kotlin";
    }

    @Override
    public boolean isValidFileLanguage(Language language) {
        return language == KotlinLanguage.INSTANCE;
    }
}
