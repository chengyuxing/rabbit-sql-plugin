package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.sql.utils.SqlUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.chengyuxing.plugin.rabbit.sql.common.Constants.TEMP_PARAMETER_PATTERN;

public class StringUtil {
    public static Set<String> getTemplateParameters(String str) {
        String[] lines = SqlUtil.removeAnnotationBlock(str).split("\n");
        if (lines.length > 0) {
            var cleanedSql = Stream.of(lines).filter(line -> !line.trim().startsWith("--"))
                    .collect(Collectors.joining(""));
            var m = TEMP_PARAMETER_PATTERN.matcher(cleanedSql);
            var params = new HashSet<String>();
            while (m.find()) {
                params.add("${" + m.group("key") + "}");
            }
            return params;
        }
        return Collections.emptySet();
    }
}
