package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.common.util.NamingUtils;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.util.SqlGenerator;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.*;

public class StringUtil {
    /**
     * get alias and sqlName
     *
     * @param sqlName sql reference name
     * @return [alias, sqlName]
     */
    public static Pair<String, String> extraSqlReference(String sqlName) {
        return XQLFileManager.decodeSqlReference(sqlName);
    }

    public static void copySqlParams(XQLConfigManager.Config config, String sqlName) {
        var sqlDefinition = config.getXqlFileManager().get(sqlName);
        var params = getParamsMappingInfo(config.getSqlGenerator(), sqlDefinition)
                .keySet()
                .stream()
                .map(key -> "\"" + key + "\", " + key)
                .toList();

        if (params.isEmpty()) {
            return;
        }

        var result = "// parameters\n" + String.join(",\n", params);

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(result), null);
    }

    public static Map<String, Set<String>> getParamsMappingInfo(SqlGenerator sqlGenerator, String sql) {
        var parser = new RabbitScriptParamParser(sql, sqlGenerator);
        parser.parse();
        return parser.getParamsMap();
    }

    public static String camelizeAndClean(String content) {
        var result = content.replaceAll("_+", "-");
        result = NamingUtils.kebabToCamel(result);
        result = result.replaceAll("\\W", "");
        return result;
    }

    public static String generateInterfaceMapperName(String alias) {
        return camelizeAndClean(alias.substring(0, 1).toUpperCase() + alias.substring(1)) + "Mapper";
    }

    public static Pair<String, String> getTypeAndPackagePath(String fullyClassName) {
        var shortType = fullyClassName.substring(fullyClassName.lastIndexOf(".") + 1);
        var packagePath = fullyClassName;
        if (fullyClassName.contains("<") && fullyClassName.contains(">")) {
            packagePath = fullyClassName.substring(0, fullyClassName.indexOf("<"));
        }
        return Pair.of(shortType, packagePath);
    }

    public static boolean isQuote(String s) {
        return (s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"));
    }
}
