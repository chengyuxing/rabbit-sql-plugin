package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.github.chengyuxing.sql.XQLFileManager;
import org.intellij.lang.annotations.Language;

import java.nio.file.Path;

public class Constants {
    @Language("RegExp")
    public static final String SQL_NAME_PATTERN = "^&\\w+\\..+";
    @Language("RegExp")
    public static final String SQL_NAME_ANNOTATION_PATTERN = "/\\*\\s*\\[\\s*(?<name>\\S+)\\s*]\\s*\\*/";
    public static final String CONFIG_NAME = XQLFileManager.YML;
    public static final Path RESOURCE_ROOT = Path.of("src", "main", "resources");
    public static final Path CONFIG_PATH = RESOURCE_ROOT.resolve(CONFIG_NAME);
}
