package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.github.chengyuxing.sql.XQLFileManager;
import org.intellij.lang.annotations.Language;

import java.nio.file.Path;

public final class Constants {
    @Language("RegExp")
    public static final String SQL_NAME_PATTERN = "^&[\\w\\-]+\\..+";
    public static final String SQL_NAME_ANNOTATION_PATTERN = XQLFileManager.NAME_PATTERN.pattern();
    public static final String CONFIG_NAME = XQLFileManager.YML;
    public static final Path RESOURCE_ROOT = Path.of("src", "main", "resources");
    public static final Path JAVA_SOURCE_ROOT = Path.of("src", "main", "java");
    public static final Path KT_SOURCE_ROOT = Path.of("src", "main", "kotlin");
    public static final Path CONFIG_PATH = RESOURCE_ROOT.resolve(CONFIG_NAME);
    @Language("Regexp")
    public static final String CONFIG_PATTERN = "xql-file-manager(-[a-zA-Z0-9_]+)?\\.yml";
    public static final String[] XQL_KEYWORDS = new String[]{"delimiter", "of", "open", "close", "blank", "null", "true", "false"};
}
