package com.github.chengyuxing.plugin.rabbit.sql.plugins.database.extensions;

import com.github.chengyuxing.common.script.ast.impl.KeyExpressionParser;
import com.github.chengyuxing.common.script.lang.Directives;
import com.github.chengyuxing.common.script.lexer.RabbitScriptLexer;
import com.github.chengyuxing.common.util.StringUtils;
import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XqlFileAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (!(element instanceof PsiComment)) {
            return;
        }
        String value = element.getText();
        if (value == null) {
            return;
        }
        // sql name highlight
        if (value.matches(Constants.SQL_NAME_ANNOTATION_PATTERN)) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(element.getTextRange())
                    .textAttributes(DefaultLanguageHighlighterColors.METADATA)
                    .create();
            return;
        }

        PsiWhiteSpace whiteSpace = null;
        if (element.getPrevSibling() instanceof PsiWhiteSpace w) {
            whiteSpace = w;
        }

        if (whiteSpace != null) {
            value = whiteSpace.getText() + value;
        }

        String clearValue = value.trim();

        if (!clearValue.startsWith("--")) {
            return;
        }
        final String prefix = clearValue.substring(2);
        final String clearPrefix = prefix.trim();
        final String tag = getTag(clearPrefix);

        if (tag.isEmpty()) {
            return;
        }

        // global expression highlight
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(element.getTextRange())
                .textAttributes(DefaultLanguageHighlighterColors.TEMPLATE_LANGUAGE_COLOR)
                .create();

        TextRange prefixRange = TextRange.from(element.getTextRange().getStartOffset(), tag.length() + clearValue.indexOf(tag));
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(prefixRange)
                .textAttributes(DefaultLanguageHighlighterColors.METADATA)
                .create();
        int whiteSpaceLen = whiteSpace == null ? 0 : whiteSpace.getTextLength();
        for (String k : Constants.XQL_DIRECTIVE_KEYWORDS) {
            highlightWord(holder, element, whiteSpaceLen, value, tag, k);
        }
        for (String k : Constants.XQL_VALUE_KEYWORDS) {
            highlightWord(holder, element, whiteSpaceLen, value, tag, k);
        }
        highlightForAsWord(holder, element, whiteSpaceLen, value, tag);
        highlightIdentifier(holder, element, whiteSpaceLen, value);
    }

    private static void highlightWord(AnnotationHolder holder, PsiElement element, int whiteSpaceLength, String content, String xqlTag, String keyword) {
        if (StringUtils.equalsAnyIgnoreCase(xqlTag, RabbitScriptLexer.DIRECTIVES)) {
            Pattern p = Pattern.compile("\\s(?<keyword>" + keyword + ")(\\s|$)");
            Matcher m = p.matcher(content);
            while (m.find()) {
                int offset = m.start("keyword");
                if (offset != -1) {
                    TextRange range = TextRange.from(element.getTextRange().getStartOffset() - whiteSpaceLength + offset, keyword.length());
                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                            .range(range)
                            .textAttributes(DefaultLanguageHighlighterColors.KEYWORD)
                            .create();
                }
            }
        }
    }

    private static void highlightForAsWord(AnnotationHolder holder, PsiElement element, int whiteSpaceLength, String content, String xqlTag) {
        if (StringUtils.equalsAnyIgnoreCase(xqlTag, Directives.FOR)) {
            Pattern p = Pattern.compile("\\s*;\\s*(?<key>" + Constants.FOR_PROPERTIES_REGEXP + ")\\s+as\\s+\\w+(\\s*|$)");
            Matcher m = p.matcher(content);
            while (m.find()) {
                int offset = m.start("key");
                if (offset != -1) {
                    TextRange range = TextRange.from(element.getTextRange().getStartOffset() - whiteSpaceLength + offset, m.group("key").length());
                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                            .range(range)
                            .textAttributes(DefaultLanguageHighlighterColors.LOCAL_VARIABLE)
                            .create();
                }
            }
        }
    }

    private static void highlightIdentifier(AnnotationHolder holder, PsiElement element, int whiteSpaceLength, String content) {
        Pattern varP = Pattern.compile("(?<var>:" + KeyExpressionParser.EXPRESSION_PATTERN.pattern() + "|" + StringUtils.NUMBER_PATTERN.pattern() + "|'(''|[^'])*'|\"(\"\"|[^\"])*\")(\\s|\\W|$)");
        Matcher varM = varP.matcher(content);
        while (varM.find()) {
            String var = varM.group("var");
            int offset = varM.start("var");
            if (offset != -1) {
                TextAttributesKey key;
                if (var.startsWith(":")) {
                    key = DefaultLanguageHighlighterColors.LOCAL_VARIABLE;
                } else if (var.startsWith("'") || var.startsWith("\"")) {
                    key = DefaultLanguageHighlighterColors.STRING;
                } else if (StringUtils.isNumber(var)) {
                    key = DefaultLanguageHighlighterColors.NUMBER;
                } else {
                    key = DefaultLanguageHighlighterColors.LINE_COMMENT;
                }
                TextRange range = TextRange.from(element.getTextRange().getStartOffset() - whiteSpaceLength + offset, var.length());
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(range)
                        .textAttributes(key)
                        .create();
            }
        }
    }

    private static String getTag(String prefix) {
        for (String keyword : RabbitScriptLexer.DIRECTIVES) {
            if (StringUtils.startsWithIgnoreCase(prefix, keyword)) {
                return keyword;
            }
        }
        return "";
    }
}
