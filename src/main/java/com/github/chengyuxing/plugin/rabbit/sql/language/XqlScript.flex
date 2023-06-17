package com.github.chengyuxing.plugin.rabbit.sql.language;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.github.chengyuxing.plugin.rabbit.sql.language.psi.XqlScriptTypes;
import com.intellij.psi.TokenType;

%%

%class XqlScriptParser
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{ return;
%eof}

KEYWORD=(for|of|end|if|fi)
VALUE_KEY=:\w+
SQL_STATEMENT=[\s\S]*

%state WAITING_VALUE

%%

<YYINITIAL> {KEYWORD}   {yybegin(YYINITIAL); return XqlScriptTypes.FOR;}
<YYINITIAL> {KEYWORD}   {yybegin(YYINITIAL); return XqlScriptTypes.END;}
<YYINITIAL> {KEYWORD}   {yybegin(YYINITIAL); return XqlScriptTypes.OF;}
<YYINITIAL> {KEYWORD}   {yybegin(YYINITIAL); return XqlScriptTypes.IF;}
<YYINITIAL> {KEYWORD}   {yybegin(YYINITIAL); return XqlScriptTypes.FI;}