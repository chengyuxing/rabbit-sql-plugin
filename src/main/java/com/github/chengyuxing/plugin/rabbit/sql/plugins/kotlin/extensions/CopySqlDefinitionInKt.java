package com.github.chengyuxing.plugin.rabbit.sql.plugins.kotlin.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.java.extensions.CopySqlDefinitionInJava;
import com.intellij.codeInspection.util.IntentionFamilyName;
import org.jetbrains.annotations.NotNull;

public class CopySqlDefinitionInKt extends CopySqlDefinitionInJava {
    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return MessageBundle.message("intention.action.copySql.familyName", "kotlin");
    }
}
