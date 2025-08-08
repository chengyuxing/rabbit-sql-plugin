package com.github.chengyuxing.plugin.rabbit.sql.plugins.kotlin.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.plugins.java.extensions.OpenParamsDialogInJava;
import com.intellij.codeInspection.util.IntentionFamilyName;
import org.jetbrains.annotations.NotNull;

public class OpenParamsDialogInKt extends OpenParamsDialogInJava {
    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Execute dynamic sql in kotlin";
    }
}
