package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.common.script.lexer.FlowControlLexer;
import com.github.chengyuxing.common.utils.StringUtil;
import com.github.chengyuxing.sql.utils.SqlHighlighter;

public class HtmlUtil {
    public static String highlightSql(String sqlString) {
        var sql = safeEscape(sqlString);
        var highlighted = SqlHighlighter.highlight(sql, (tag, content) -> switch (tag) {
            case FUNCTION -> span(content, Color.FUNCTION);
            case KEYWORD -> span(content, Color.KEYWORD);
            case NUMBER -> span(content, Color.NUMBER);
            case POSTGRESQL_FUNCTION_BODY_SYMBOL, SINGLE_QUOTE_STRING -> span(content, Color.STRING);
            case ASTERISK -> span(content, Color.HIGHLIGHT);
            case LINE_ANNOTATION, BLOCK_ANNOTATION -> span(content, Color.ANNOTATION);
            case NAMED_PARAMETER -> code(content, Color.HIGHLIGHT);
            case OTHER -> {
                if (StringUtil.equalsAnyIgnoreCase(content, FlowControlLexer.KEYWORDS)) {
                    yield span(content, Color.FUNCTION);
                }
                yield content;
            }
        });
        return pre(highlighted, Color.EMPTY);
    }

    public static String pre(String s, Color color, String... attrs) {
        return wrap("pre", s, color, attrs);
    }

    public static String code(String word, Color color, String... attrs) {
        return wrap("code", word, color, attrs);
    }

    public static String span(String content, Color color, String... attrs) {
        return wrap("span", content, color, attrs);
    }

    public static String wrap(String tag, String content, Color color, String... attrs) {
        var colorAttr = color.getCode().isEmpty() ? "" : "color:" + color.getCode();
        return "<" + tag + " style=\"" + colorAttr + ";" + String.join(";", attrs) + "\">" + content + "</" + tag + ">";
    }

    public static String safeEscape(String s) {
        return s.replace(">", "&gt;")
                .replace("<", "&lt;");
    }

    public static String toHtml(String content) {
        return "<html><body>" + content + "</body></html>";
    }

    public enum Color {
        EMPTY(""),
        KEYWORD("#CC7832"),
        NUMBER("#48A0A2"),
        FUNCTION("#54ADF9"),
        NAMED_PARAM("#499ee7"),
        STRING("#79A978"),
        ANNOTATION("#7B7E84"),
        DANGER("#E56068"),
        LIGHT("#B4BBC3"),
        HIGHLIGHT("#BBB529"),
        WARNING("orange"),
        ERROR("#F75464");

        private final String code;

        Color(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
}
