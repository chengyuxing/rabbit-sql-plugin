package com.github.chengyuxing.plugin.rabbit.sql.lang;

import com.intellij.lang.Language;

public class XqlLanguage extends Language {
    public static final XqlLanguage INSTANCE = new XqlLanguage();

    protected XqlLanguage() {
        super("XQL");
    }
}
