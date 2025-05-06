package com.github.chengyuxing.plugin.rabbit.sql.language;

import com.intellij.lexer.FlexAdapter;

public class RabbitScriptLexerAdapter extends FlexAdapter {
    public RabbitScriptLexerAdapter() {
        super(new _RabbitScriptLexer(null));
    }
}
