package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.github.chengyuxing.common.script.ast.impl.KeyExpressionParser;
import com.github.chengyuxing.common.script.lang.ForContextProperty;
import com.github.chengyuxing.common.util.StringUtils;
import com.github.chengyuxing.sql.XQLFileManager;
import org.intellij.lang.annotations.Language;

import java.nio.file.Path;
import java.util.regex.Pattern;

public final class Constants {
    public static final Pattern SQL_NAME_PATTERN = Pattern.compile("^&[\\w\\-]+\\..+");
    public static final Pattern SQL_NAME_ANNOTATION_PATTERN = XQLFileManager.KEY_PATTERN;
    public static final String CONFIG_NAME = XQLFileManager.YML;
    public static final Path RESOURCE_ROOT = Path.of("src", "main", "resources");
    public static final Path JAVA_SOURCE_ROOT = Path.of("src", "main", "java");
    public static final Path KT_SOURCE_ROOT = Path.of("src", "main", "kotlin");
    public static final Path CONFIG_PATH = RESOURCE_ROOT.resolve(CONFIG_NAME);
    public static final Pattern CONFIG_PATTERN = Pattern.compile("xql-file-manager(-[a-zA-Z0-9_]+)?\\.yml");
    public static final String[] XQL_DIRECTIVE_KEYWORDS = new String[]{"of", "as", "throw"};
    public static final String[] FOR_PROPERTIES = new String[]{
            ForContextProperty.first.name(),
            ForContextProperty.index.name(),
            ForContextProperty.last.name(),
            ForContextProperty.odd.name(),
            ForContextProperty.even.name()
    };
    public static final String FOR_PROPERTIES_REGEXP = String.join("|", FOR_PROPERTIES);
    public static final String[] XQL_VALUE_KEYWORDS = new String[]{"blank", "null", "true", "false"};
    public static final Pattern PACKAGE_PATTERN = Pattern.compile("[a-zA-Z]\\w*(\\.[a-zA-Z]\\w*)*");
    public static final Pattern FULLY_CLASS_PATTERN = Pattern.compile("[a-zA-Z]\\w*(\\.[a-zA-Z]\\w*)+");
    public static final Pattern VAR_PATTERN = Pattern.compile("(?<var>:" + KeyExpressionParser.EXPRESSION_PATTERN.pattern() + "|" + StringUtils.NUMBER_PATTERN.pattern() + "|'(''|[^'])*'|\"(\"\"|[^\"])*\")(\\s|\\W|$)");
    public static final Pattern FOR_PROPS_AS_PATTERN = Pattern.compile("\\s*;\\s*(?<key>" + FOR_PROPERTIES_REGEXP + ")\\s+as\\s+\\w+(\\s*|$)");
    public static final Pattern URI_PATTERN = Pattern.compile("(?i)(file|http|https|ftp):/+.*");
}
