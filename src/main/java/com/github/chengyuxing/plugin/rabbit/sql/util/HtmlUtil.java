package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.common.script.lexer.FlowControlLexer;
import com.github.chengyuxing.common.utils.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.sql.utils.SqlHighlighter;

public class HtmlUtil {
    public static final String IDENTIFIER = "ij-rabbit-sql-hls";

    public static String highlightSql(String sqlString) {
        var sql = safeEscape(sqlString);
        var highlighted = SqlHighlighter.highlight(sql, (tag, content) -> switch (tag) {
            case FUNCTION -> span(content, Color.FUNCTION);
            case KEYWORD -> span(content, Color.KEYWORD);
            case NUMBER -> span(content, Color.NUMBER);
            case POSTGRESQL_FUNCTION_BODY_SYMBOL, SINGLE_QUOTE_STRING -> span(content, Color.STRING);
            case ASTERISK -> span(content, Color.HIGHLIGHT);
            case LINE_ANNOTATION -> {
                var nc = content;
                var isAnno = true;
                for (var k : FlowControlLexer.KEYWORDS) {
                    if (StringUtil.containsIgnoreCase(nc, ">" + k + "</")) {
                        isAnno = false;
                        break;
                    }
                }
                if (isAnno) {
                    nc = removeStyles(nc);
                }
                yield span(nc, Color.ANNOTATION);
            }
            case BLOCK_ANNOTATION -> span(removeStyles(content), Color.ANNOTATION);
            case NAMED_PARAMETER -> code(content, Color.LIGHT);
            case OTHER -> {
                if (StringUtil.equalsAny(content, Constants.XQL_KEYWORDS)) {
                    yield span(content, Color.KEYWORD);
                }
                var maybeKeyword = content;
                var pos = 0;
                if (content.startsWith("--")) {
                    maybeKeyword = content.substring(2);
                    pos = 2;
                }
                if (StringUtil.equalsAnyIgnoreCase(maybeKeyword, FlowControlLexer.KEYWORDS)) {
                    yield content.substring(0, pos) + span(maybeKeyword, Color.HIGHLIGHT);
                }
                yield content;
            }
        });
        return pre(highlighted, Color.EMPTY);
    }

    public static String removeStyles(String content) {
        return content.replaceAll("(<[a-z]+\\s+" + IDENTIFIER + "\\s+style=\")[^\"]+(\">)", "$1$2");
    }

    public static String pre(String s, Color color, String... styles) {
        return wrap("pre", s, color, styles);
    }

    public static String code(String word, Color color, String... styles) {
        return wrap("code", word, color, styles);
    }

    public static String span(String content, Color color, String... styles) {
        return wrap("span", content, color, styles);
    }

    public static String wrap(String tag, String content, Color color, String... styles) {
        var colorAttr = color.getCode().isEmpty() ? "" : "color:" + color.getCode();
        return String.format("<%1$s %5$s style=\"%2$s;%4$s\">%3$s</%1$s>", tag, colorAttr, content, String.join(";", styles), IDENTIFIER);
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
