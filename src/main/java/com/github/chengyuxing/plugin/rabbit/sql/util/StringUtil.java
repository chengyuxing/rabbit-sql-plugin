package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.utils.SqlGenerator;
import com.github.chengyuxing.sql.utils.SqlUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.chengyuxing.common.script.SimpleScriptParser.*;
import static com.github.chengyuxing.common.utils.StringUtil.NEW_LINE;
import static com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil.code;

public class StringUtil {

    public static Set<String> getTemplateParameters(String str) {
        var sql = SqlUtil.removeBlockAnnotation(str);
        String[] lines = sql.split(NEW_LINE);
        if (lines.length > 0) {
            var cleanedSql = Stream.of(lines).filter(line -> !line.trim().startsWith("--"))
                    .collect(Collectors.joining(NEW_LINE));
            var m = SqlUtil.FMT.getPattern().matcher(cleanedSql);
            var params = new LinkedHashSet<String>();
            while (m.find()) {
                var key = m.group("key");
                params.add("${" + key + "}");
            }
            return params;
        }
        return Set.of();
    }

    public static Map<String, Set<String>> getParamsMappingInfo(SqlGenerator sqlGenerator, String sql) {
        var p = sqlGenerator.getNamedParamPattern();
        var substrMapPair = SqlUtil.replaceSqlSubstr(sql);
        var nonSubstringSql = substrMapPair.getItem1();
        var substrMap = substrMapPair.getItem2();

        String[] lines = nonSubstringSql.split(NEW_LINE);
        var keyMapping = new LinkedHashMap<String, Set<String>>();
        for (String line : lines) {
            var tl = line.trim();
            if (tl.startsWith("--")) {
                tl = tl.substring(2).trim();
            }
            var m = p.matcher(tl);
            if (com.github.chengyuxing.common.utils.StringUtil.startsWithsIgnoreCase(tl, IF, SWITCH, WHEN, CASE, FOR)) {
                while (m.find()) {
                    var name = m.group("name");
                    var kl = getKeyAndRestLength(name);
                    var key = kl.getItem1();
                    var endIdx = m.end("name") - kl.getItem2();
                    tl = tl.replace("<", "&lt;")
                            .replace(">", "&gt;");
                    var part = code(tl.substring(0, m.start("name") - 1), HtmlUtil.Color.ANNOTATION) + "_" + code(tl.substring(endIdx), HtmlUtil.Color.ANNOTATION);
                    if (!keyMapping.containsKey(key)) {
                        var parts = new LinkedHashSet<String>();
                        keyMapping.put(key, parts);
                    }
                    keyMapping.get(key).add(restoreSubStr(part, substrMap));
                }
                continue;
            }
            while (m.find()) {
                var name = m.group("name");
                // ignore for local variables
                if (name.startsWith(XQLFileManager.DynamicSqlParser.FOR_VARS_KEY + ".")) {
                    continue;
                }
                var kl = getKeyAndRestLength(name);
                var key = kl.getItem1();
                var holder = "_" + code(name.substring(key.length()), HtmlUtil.Color.ANNOTATION);
                if (!keyMapping.containsKey(key)) {
                    var set = new LinkedHashSet<String>();
                    set.add(restoreSubStr(holder, substrMap));
                    keyMapping.put(key, set);
                } else {
                    keyMapping.get(key).add(restoreSubStr(holder, substrMap));
                }
            }
            var tempP = SqlUtil.FMT.getPattern();
            var tempM = tempP.matcher(line);
            while (tempM.find()) {
                var key = tempM.group("key");
                if (key.contains(".")) {
                    key = key.substring(0, key.indexOf("."));
                }
                if (!keyMapping.containsKey(key)) {
                    var temp = tempM.group(0).replace(key, "*");
                    var coloredTemp = code(temp.substring(0, temp.indexOf("*")), HtmlUtil.Color.ANNOTATION) + code("_", HtmlUtil.Color.LIGHT) + code(temp.substring(temp.indexOf("*") + 1), HtmlUtil.Color.ANNOTATION);
                    var set = new LinkedHashSet<String>();
                    set.add(coloredTemp);
                    keyMapping.put(key, set);
                }
            }
        }
        return keyMapping;
    }

    private static String restoreSubStr(String content, Map<String, String> substrMap) {
        for (Map.Entry<String, String> e : substrMap.entrySet()) {
            content = content.replace(e.getKey(), e.getValue());
        }
        return content;
    }

    public static Pair<String, Integer> getKeyAndRestLength(String name) {
        String key = name;
        int restLength = key.length();
        int dotIdx = name.indexOf(".");
        if (dotIdx != -1) {
            key = name.substring(0, dotIdx);
            restLength -= dotIdx;
        } else {
            restLength = 0;
        }
        return Pair.of(key, restLength);
    }

    public static boolean isForLocalVariable(String sql, String key, int start, int end) {
        if (!sql.contains(key)) {
            return false;
        }
        int forStart = com.github.chengyuxing.common.utils.StringUtil.indexOfIgnoreCase(sql, FOR);
        if (forStart > 0) {
            var forEnd = com.github.chengyuxing.common.utils.StringUtil.indexOfIgnoreCase(sql.substring(start + 4), DONE);
            if (end > 0) {
                end = forStart + forEnd + 8;
                //
                // -- #for item of :users
                //      ${item} or :item
                // -- #done
                //
                if (forStart < start && forEnd > end) {
                    var forLoop = sql.substring(start, end);
                    var forVars = sql.substring(start + 4, sql.indexOf(" of :")).trim().split(",");
                    var itemName = forVars[0].trim();
                    var itemIdx = "---";
                    if (forVars.length > 1) {
                        itemIdx = forVars[1].trim();
                    }
                    if (forLoop.contains(key) && (key.contains(itemName) || key.contains(itemIdx))) {
                        return true;
                    }
                    return isForLocalVariable(sql.substring(end), key, start, end);
                }
            }
        }
        return false;
    }
}
