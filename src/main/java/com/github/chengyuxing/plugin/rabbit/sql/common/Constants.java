package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.github.chengyuxing.common.script.lang.ForContextProperty;
import com.github.chengyuxing.sql.XQLFileManager;
import org.intellij.lang.annotations.Language;

import java.nio.file.Path;

public final class Constants {
    @Language("RegExp")
    public static final String SQL_NAME_PATTERN = "^&[\\w\\-]+\\..+";
    public static final String SQL_NAME_ANNOTATION_PATTERN = XQLFileManager.KEY_PATTERN.pattern();
    public static final String CONFIG_NAME = XQLFileManager.YML;
    public static final Path RESOURCE_ROOT = Path.of("src", "main", "resources");
    public static final Path JAVA_SOURCE_ROOT = Path.of("src", "main", "java");
    public static final Path KT_SOURCE_ROOT = Path.of("src", "main", "kotlin");
    public static final Path CONFIG_PATH = RESOURCE_ROOT.resolve(CONFIG_NAME);
    @Language("Regexp")
    public static final String CONFIG_PATTERN = "xql-file-manager(-[a-zA-Z0-9_]+)?\\.yml";
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
    public static final String PACKAGE_PATTERN = "[a-zA-Z]\\w*(\\.[a-zA-Z]\\w*)*";
    public static final String FULLY_CLASS_PATTERN = "[a-zA-Z]\\w*(\\.[a-zA-Z]\\w*)+";
}
