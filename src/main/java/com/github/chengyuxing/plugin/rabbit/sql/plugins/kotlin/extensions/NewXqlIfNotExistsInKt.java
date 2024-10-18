package com.github.chengyuxing.plugin.rabbit.sql.plugins.kotlin.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.extensions.NewXqlIfNotExists;
import com.intellij.lang.Language;
import org.jetbrains.kotlin.idea.KotlinLanguage;

public class NewXqlIfNotExistsInKt extends NewXqlIfNotExists {
    @Override
    protected boolean isValidFileLanguage(Language language) {
        return language == KotlinLanguage.INSTANCE;
    }
}
