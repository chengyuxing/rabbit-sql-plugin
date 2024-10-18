package com.github.chengyuxing.plugin.rabbit.sql.plugins.kotlin.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.extensions.CopySqlDefinitionInJava;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.lang.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.KotlinLanguage;

public class CopySqlDefinitionInKt extends CopySqlDefinitionInJava {
    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Copy sql definition in kotlin";
    }

    @Override
    public boolean isValidFileLanguage(Language language) {
        return language == KotlinLanguage.INSTANCE;
    }
}
