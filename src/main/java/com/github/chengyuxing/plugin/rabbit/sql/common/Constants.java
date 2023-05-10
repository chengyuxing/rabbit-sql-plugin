package com.github.chengyuxing.plugin.rabbit.sql.common;

import java.util.regex.Pattern;

public class Constants {
    public static final String SQL_NAME_PATTERN = "^&\\w+\\..+";
    public static final Pattern TEMP_PARAMETER_PATTERN = Pattern.compile("\\$\\{\\s*(?<key>:?[\\w._-]+)\\s*}");
    public static final String CONFIG_NAME = "xql-file-manager.properties";
}
