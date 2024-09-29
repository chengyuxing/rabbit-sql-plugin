package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.intellij.codeInspection.util.IntentionFamilyName;
import org.jetbrains.annotations.NotNull;

public class CopySqlDefinitionInKt extends CopySqlDefinitionInJava {
    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Copy sql definition in kotlin";
    }
}
