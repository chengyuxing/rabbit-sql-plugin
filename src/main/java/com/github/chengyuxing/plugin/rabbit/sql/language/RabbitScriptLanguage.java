package com.github.chengyuxing.plugin.rabbit.sql.language;

import com.intellij.lang.Language;

public class RabbitScriptLanguage extends Language {
    public static final RabbitScriptLanguage INSTANCE = new RabbitScriptLanguage();

    protected RabbitScriptLanguage() {
        super("RabbitScript");
    }
}
