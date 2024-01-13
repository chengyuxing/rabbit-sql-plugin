package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.sql.utils.SqlHighlighter;

public class HtmlUtil {
    public static String toHighlightSqlHtml(String sql) {
        return "<pre>" + highlight(sql) + "</pre>";
    }

    public static String toHtml(String s, Color color) {
        return "<pre style='color:" + color.getCode() + "'>" + s + "</pre>";
    }

    public static String highlight(String sqlString) {
        var sql = sqlString.replace(">", "&gt;")
                .replace("<", "&lt;");
        return SqlHighlighter.highlight(sql, (tag, content) -> switch (tag) {
            case FUNCTION -> colorful(content, Color.FUNCTION);
            case KEYWORD -> colorful(content, Color.KEYWORD);
            case NUMBER -> colorful(content, Color.NUMBER);
            case POSTGRESQL_FUNCTION_BODY_SYMBOL, SINGLE_QUOTE_STRING -> colorful(content, Color.STRING);
            case ASTERISK -> colorful(content, Color.HIGHLIGHT);
            case LINE_ANNOTATION, BLOCK_ANNOTATION -> colorful(content, Color.ANNOTATION);
        });
    }

    public static String colorful(String word, Color color) {
        return "<code style='color:" + color.getCode() + "'>" + word + "</code>";
    }

    public enum Color {
        KEYWORD("#CC7832"),
        NUMBER("#48A0A2"),
        FUNCTION("#54ADF9"),
        STRING("#79A978"),
        ANNOTATION("#7B7E84"),
        DANGER("#E56068"),
        LIGHT("#B4BBC3"),
        HIGHLIGHT("#BBB529");
        private final String code;

        Color(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
}
