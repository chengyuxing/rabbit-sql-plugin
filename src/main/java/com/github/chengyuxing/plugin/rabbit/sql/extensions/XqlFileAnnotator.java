package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.github.chengyuxing.common.script.expression.Patterns;
import com.github.chengyuxing.common.script.lexer.FlowControlLexer;
import com.github.chengyuxing.common.utils.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
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
        if (value.matches(Constants.SQL_NAME_ANNOTATION_PATTERN) || value.matches(XQLFileManager.PART_PATTERN.pattern())) {
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
        for (String k : Constants.XQL_KEYWORDS) {
            highlightWord(holder, element, whiteSpaceLen, value, tag, k);
        }
        highlightVarName(holder, element, whiteSpaceLen, value);
    }

    void highlightWord(AnnotationHolder holder, PsiElement element, int whiteSpaceLength, String content, String xqlTag, String keyword) {
        if (StringUtil.equalsAnyIgnoreCase(xqlTag, FlowControlLexer.KEYWORDS)) {
            Pattern p = Pattern.compile("\\s(?<keyword>" + keyword + ")(\\s|$)");
            Matcher m = p.matcher(content);
            if (m.find()) {
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

    void highlightVarName(AnnotationHolder holder, PsiElement element, int whiteSpaceLength, String content) {
        Pattern p = Pattern.compile("(?<var>:" + Patterns.VAR_KEY_PATTERN + ")(\\s|\\W|$)");
        Matcher m = p.matcher(content);
        while (m.find()) {
            String var = m.group("var");
            int offset = m.start("var");
            if (offset != -1) {
                TextRange range = TextRange.from(element.getTextRange().getStartOffset() - whiteSpaceLength + offset, var.length());
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(range)
                        .textAttributes(DefaultLanguageHighlighterColors.LOCAL_VARIABLE)
                        .create();
            }
        }
    }

    String getTag(String prefix) {
        for (String keyword : FlowControlLexer.KEYWORDS) {
            if (StringUtil.startsWithIgnoreCase(prefix, keyword)) {
                return keyword;
            }
        }
        return "";
    }
}
