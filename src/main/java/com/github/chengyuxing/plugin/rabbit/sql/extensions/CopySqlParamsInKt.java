package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.intellij.codeInspection.util.IntentionFamilyName;
import org.jetbrains.annotations.NotNull;

public class CopySqlParamsInKt extends CopySqlParamsInJava {
    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Copy sql params in kotlin";
    }
}
