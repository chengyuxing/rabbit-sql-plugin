package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.github.chengyuxing.sql.XQLFileManager;
import org.intellij.lang.annotations.Language;

import java.nio.file.Path;

public class Constants {
    @Language("RegExp")
    public static final String SQL_NAME_PATTERN = "^&\\w+\\..+";
    public static final String SQL_NAME_ANNOTATION_PATTERN = XQLFileManager.NAME_PATTERN.pattern();
    public static final String CONFIG_NAME = XQLFileManager.YML;
    public static final Path RESOURCE_ROOT = Path.of("src", "main", "resources");
    public static final Path CONFIG_PATH = RESOURCE_ROOT.resolve(CONFIG_NAME);
    @Language("Regexp")
    public static final String CONFIG_PATTERN = "xql-file-manager(-[a-zA-Z0-9]+)?\\.yml";

    public static final String XQL_TEMPLATE = """
            /*
            * Created by IntelliJ IDEA.
            * User: ${USER}
            * Date: ${DATE}
            * Time: ${TIME}
            * Typing "xql" keyword to get suggestions,
            * e.g: "xql:new" will be create a sql fragment.
            */


            """;
}
