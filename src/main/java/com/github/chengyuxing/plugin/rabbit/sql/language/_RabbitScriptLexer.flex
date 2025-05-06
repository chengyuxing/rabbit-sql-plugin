package com.github.chengyuxing.plugin.rabbit.sql.language;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.github.chengyuxing.plugin.rabbit.sql.language.psi.RabbitScriptTypes.*;

%%

%{
  private int nesting = 0;

  public _RabbitScriptLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _RabbitScriptLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL=\R

REL_OP=(<=|>=|==|\!=|<>|>|<|\~|\!\~|\@|\!\@)
VARIABLE=:[a-zA-Z_][a-zA-Z0-9_]*
CONST=blank|true|false|null
IDENTIFIER=[a-zA-Z_][a-zA-Z0-9_]*
STRING='([^'\n])*'
NUMBER=[0-9]+(\.[0-9]+)?
WHITE_SPACE=[\ \t\f]+
NEWLINE=\n+
PLAIN_TEXT=[^#]+

%state IF_EXPR_STATE
%state TEXT_STATE

%%

<YYINITIAL> "#if" {yybegin(IF_EXPR_STATE);return IF;}
<YYINITIAL> "#fi" {return FI;}
<YYINITIAL> {NEWLINE} {return NEWLINE;}
<YYINITIAL> {PLAIN_TEXT} {return PLAIN_TEXT;}

<IF_EXPR_STATE> {
    {VARIABLE} {return VARIABLE;}
    {REL_OP} {return REL_OP;}
    {STRING} | {NUMBER} | {CONST} { return VALUE; }
    {WHITE_SPACE} { return WHITE_SPACE; }
    {NEWLINE} {yybegin(YYINITIAL);return NEWLINE;}
}
