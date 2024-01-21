package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.sql.utils.SqlHighlighter;

public class HtmlUtil {

    public static String highlightSql(String sqlString) {
        var sql = sqlString.replace(">", "&gt;")
                .replace("<", "&lt;")
                .replace("&","&amp;");
        var highlighted = SqlHighlighter.highlight(sql, (tag, content) -> switch (tag) {
            case FUNCTION -> code(content, Color.FUNCTION);
            case KEYWORD -> code(content, Color.KEYWORD);
            case NUMBER -> code(content, Color.NUMBER);
            case POSTGRESQL_FUNCTION_BODY_SYMBOL, SINGLE_QUOTE_STRING -> code(content, Color.STRING);
            case ASTERISK -> code(content, Color.HIGHLIGHT);
            case LINE_ANNOTATION, BLOCK_ANNOTATION -> code(content, Color.ANNOTATION);
        });
        return "<pre>" + highlighted + "</pre>";
    }

    public static String pre(String s, Color color) {
        return "<pre style=\"color:" + color.getCode() + "\">" + s + "</pre>";
    }

    public static String code(String word, Color color) {
        return "<code style=\"color:" + color.getCode() + "\">" + word + "</code>";
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
