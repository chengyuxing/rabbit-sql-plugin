package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.common.script.lexer.RabbitScriptLexer;
import com.github.chengyuxing.common.util.StringUtils;
import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.sql.util.SqlHighlighter;
import com.intellij.openapi.util.text.StringUtil;
import org.intellij.lang.annotations.Language;

public class HtmlUtil {
    public static String highlightSql(String sqlString) {
        var sql = safeEscape(sqlString);
        var highlighted = SqlHighlighter.highlight(sql, c -> StringUtil.stripHtml(c, true),
                (tag, content) -> switch (tag) {
                    case FUNCTION -> span(content, Color.FUNCTION);
                    case KEYWORD -> span(content, Color.KEYWORD);
                    case NUMBER -> span(content, Color.NUMBER);
                    case POSTGRESQL_FUNCTION_BODY_SYMBOL -> span(content, Color.STRING);
                    case QUOTE_STRING -> {
                        if (content.startsWith("'")) {
                            yield span(content, Color.STRING);
                        }
                        yield content;
                    }
                    case ASTERISK -> span(content, Color.HIGHLIGHT);
                    case LINE_COMMENT,
                         BLOCK_COMMENT -> span(StringUtil.stripHtml(content, true), Color.ANNOTATION);
                    case RABBIT_SCRIPT_COMMENT,
                         INLINE_TEMPLATE_COMMENT,
                         METADATA_DEFINE_COMMENT -> span(content, Color.ANNOTATION);
                    case NAMED_PARAMETER -> code(content, Color.LIGHT);
                    case OTHER -> {
                        if (StringUtils.equalsAny(content, Constants.XQL_DIRECTIVE_KEYWORDS) || StringUtils.equalsAny(content, Constants.XQL_VALUE_KEYWORDS)) {
                            yield span(content, Color.KEYWORD);
                        }
                        if (SqlHighlighter.METADATA_NAME_PATTERN.matcher(content).matches()) {
                            yield "@" + span(content.substring(1), Color.LIGHT);
                        }
                        var maybeKeyword = content;
                        var pos = 0;
                        if (content.startsWith("--")) {
                            maybeKeyword = content.substring(2);
                            pos = 2;
                        }
                        if (StringUtils.equalsAnyIgnoreCase(maybeKeyword, RabbitScriptLexer.DIRECTIVES)) {
                            yield content.substring(0, pos) + span(maybeKeyword, Color.HIGHLIGHT);
                        }
                        yield content;
                    }
                });
        return pre(highlighted, Color.EMPTY);
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
        return String.format("<%1$s style=\"%2$s;%4$s\">%3$s</%1$s>", tag, colorAttr, content, String.join(";", styles));
    }

    public static String safeEscape(String s) {
        return s.replace(">", "&gt;")
                .replace("<", "&lt;");
    }

    public static @Language("HTML") String toHtml(@Language("HTML") String content) {
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
