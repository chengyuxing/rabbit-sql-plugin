package com.github.chengyuxing.plugin.rabbit.sql.language;

import com.intellij.lang.Language;

public class XqlScriptLanguage extends Language {
    public static final XqlScriptLanguage INSTANCE = new XqlScriptLanguage();

    private XqlScriptLanguage() {
        super("XQLScript");
    }
}
