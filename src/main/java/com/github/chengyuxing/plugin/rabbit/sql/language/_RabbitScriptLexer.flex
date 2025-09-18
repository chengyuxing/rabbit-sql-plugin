package com.github.chengyuxing.plugin.rabbit.sql.language;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.github.chengyuxing.plugin.rabbit.sql.language.psi.RabbitScriptTypes.*;

%%

%public
%class _RabbitScriptLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL=\R

IF=#if
ELSE=#else
FI=#fi
CHOOSE=#choose
WHEN=#when
SWITCH=#switch
CASE=#case
DEFAULT=#default
BREAK=#break
END=#end
FOR=#for
DONE=#done
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

<YYINITIAL> {IF} {yybegin(IF_EXPR_STATE); return IF;}
<YYINITIAL> {FI} {yybegin(YYINITIAL); return FI;}
<YYINITIAL> {NEWLINE} {yybegin(YYINITIAL); return WHITE_SPACE;}
<YYINITIAL> {PLAIN_TEXT} {return PLAIN_TEXT;}

<IF_EXPR_STATE> {
    {VARIABLE} {return VARIABLE;}
    {REL_OP} {return REL_OP;}
    {STRING} | {NUMBER} | {CONST} { return VALUE; }
    {WHITE_SPACE} { return WHITE_SPACE; }
    {NEWLINE} {yybegin(YYINITIAL);return NEWLINE;}
}

// 1. 逻辑似乎应该是，识别以#号开头的行，然后进入waiting表达式状态机，模仿官方文档
// 2. 再分别判断是否是#if，#fi等关键字
// 3. 分别在进入表达式判断状态机