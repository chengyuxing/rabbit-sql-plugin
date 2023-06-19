package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.sql.utils.SqlTranslator;
import com.github.chengyuxing.sql.utils.SqlUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.chengyuxing.common.script.SimpleScriptParser.*;
import static com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil.colorful;

public class StringUtil {
    public static Set<String> getTemplateParameters(SqlTranslator sqlTranslator, String str) {
        var sql = SqlUtil.removeAnnotationBlock(str);
        String[] lines = sql.split("\n");
        if (lines.length > 0) {
            var cleanedSql = Stream.of(lines).filter(line -> !line.trim().startsWith("--"))
                    .collect(Collectors.joining("\n"));
            var m = sqlTranslator.getSTR_TEMP_PATTERN().matcher(cleanedSql);
            var params = new HashSet<String>();
            while (m.find()) {
                var key = m.group("key");
                if (!isTemplateKeyInForExpression(sql, key)) {
                    params.add("${" + m.group("key") + "}");
                }
            }
            return params;
        }
        return Collections.emptySet();
    }

    public static Map<String, Set<String>> getParamsMappingInfo(SqlTranslator sqlTranslator, String sql) {
        var p = sqlTranslator.getPARAM_PATTERN();
        String[] lines = sql.split("\n");
        var keyMapping = new LinkedHashMap<String, Set<String>>();
        for (String line : lines) {
            var tl = line.trim();
            if (tl.startsWith("--")) {
                tl = tl.substring(2).trim();
            }
            var m = p.matcher(tl);
            if (com.github.chengyuxing.common.utils.StringUtil.startsWithsIgnoreCase(tl, IF, SWITCH, WHEN, CASE, FOR)) {
                while (m.find()) {
                    var key = m.group("name");
                    tl = tl.replace("<", "&lt;")
                            .replace(">", "&gt;");
                    var part = colorful(tl.substring(0, m.start("name") - 1), HtmlUtil.Color.ANNOTATION) + "_" + colorful(tl.substring(m.end("name")), HtmlUtil.Color.ANNOTATION);
                    if (!keyMapping.containsKey(key)) {
                        var parts = new HashSet<String>();
                        keyMapping.put(key, parts);
                    }
                    keyMapping.get(key).add(part);
                }
                continue;
            }
            while (m.find()) {
                var key = m.group("name");
                if (!keyMapping.containsKey(key)) {
                    var parts = new HashSet<String>();
                    keyMapping.put(key, parts);
                }
            }
            var tempP = sqlTranslator.getSTR_TEMP_PATTERN();
            var tempM = tempP.matcher(line);
            if (tempM.find()) {
                var key = tempM.group("key");
                if (!isTemplateKeyInForExpression(sql, key)) {
                    if (key.contains(".")) {
                        key = key.substring(0, key.indexOf("."));
                    }
                    if (!keyMapping.containsKey(key)) {
                        var temp = tempM.group(0).replace(key, "*");
                        var coloredTemp = colorful(temp.substring(0, temp.indexOf("*")), HtmlUtil.Color.ANNOTATION) + colorful("_", HtmlUtil.Color.LIGHT) + colorful(temp.substring(temp.indexOf("*") + 1), HtmlUtil.Color.ANNOTATION);
                        keyMapping.put(key, Collections.singleton(coloredTemp));
                    }
                }
            }
        }
        return keyMapping;
    }

    public static boolean isTemplateKeyInForExpression(String sql, String key) {
        if (!sql.contains(key)) {
            return false;
        }
        if (sql.contains("#for") && sql.contains("#end")) {
            var start = sql.indexOf("#for");
            if (start > 0) {
                var end = sql.substring(start + 4).indexOf("#end");
                if (end > 0) {
                    end = start + end + 8;
                    var forLoop = sql.substring(start, end);
                    if (forLoop.contains(key)) {
                        return true;
                    }
                    return isTemplateKeyInForExpression(sql.substring(end), key);
                }
            }
        }
        return false;
    }
}
