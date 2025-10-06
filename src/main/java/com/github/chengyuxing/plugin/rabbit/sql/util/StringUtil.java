package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.common.script.Token;
import com.github.chengyuxing.common.script.TokenType;
import com.github.chengyuxing.common.script.lexer.RabbitScriptLexer;
import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.utils.SqlGenerator;
import com.github.chengyuxing.sql.utils.SqlUtil;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.chengyuxing.common.utils.StringUtil.NEW_LINE;
import static com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil.code;

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
        var namedParams = getParamsMappingInfo(config.getSqlGenerator(), sqlDefinition, true)
                .keySet()
                .stream()
                .map(key -> "\"" + key + "\", " + key)
                .toList();

        var templateParams = getTemplateParameters(sqlDefinition, "", "")
                .stream()
                .distinct()
                .map(key -> "\"" + key + "\", " + key)
                .toList();

        if (namedParams.isEmpty() && templateParams.isEmpty()) {
            return;
        }

        var paramsGroup = new ArrayList<String>();

        if (!templateParams.isEmpty()) {
            paramsGroup.add("// template parameters\n" + String.join(",\n", templateParams));
        }

        if (!namedParams.isEmpty()) {
            paramsGroup.add("// named parameters\n" + String.join(",\n", namedParams));
        }

        var result = String.join(",\n", paramsGroup);

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(result), null);
    }

    public static Set<String> getTemplateParameters(String str) {
        return getTemplateParameters(str, "${", "}");
    }

    public static Set<String> getTemplateParameters(String str, String keyPrefix, String keySuffix) {
        String[] lines = str.split(NEW_LINE);
        if (lines.length > 0) {
            var cleanedSql = Stream.of(lines).filter(line -> !line.trim().startsWith("--"))
                    .collect(Collectors.joining(NEW_LINE));
            var m = SqlUtil.FMT.getPattern().matcher(cleanedSql);
            var params = new LinkedHashSet<String>();
            while (m.find()) {
                var key = m.group("key");
                params.add(keyPrefix + key + keySuffix);
            }
            return params;
        }
        return Set.of();
    }

    public static Map<String, Set<String>> getParamsMappingInfo(SqlGenerator sqlGenerator, String sql) {
        return getParamsMappingInfo(sqlGenerator, sql, false);
    }

    public static Map<String, Set<String>> getParamsMappingInfo(SqlGenerator sqlGenerator, String sql, boolean excludeTemplateHolder) {
        Map<String, Set<String>> paramsMap = new LinkedHashMap<>();
        RabbitScriptLexer lexer = new RabbitScriptLexer(sql) {
            @Override
            protected String trimExpressionLine(String line) {
                String lt = line.trim();
                if (lt.startsWith("--")) {
                    return lt.substring(2);
                }
                return line;
            }
        };
        var tokens = lexer.tokenize();
        var sqlTokens = new ArrayList<Token>();
        var scriptTokens = new ArrayList<Token>();
        for (var token : tokens) {
            if (token.getType() == TokenType.PLAIN_TEXT) {
                sqlTokens.add(token);
            } else {
                scriptTokens.add(token);
            }
        }

        for (int i = 0; i < scriptTokens.size(); i++) {
            var token = scriptTokens.get(i);
            if (token.getType() == TokenType.IF ||
                    token.getType() == TokenType.SWITCH ||
                    token.getType() == TokenType.WHEN ||
                    token.getType() == TokenType.FOR ||
                    token.getType() == TokenType.CHECK ||
                    token.getType() == TokenType.GUARD ||
                    token.getType() == TokenType.DEFINE_VAR) {
                List<Token> scripts = new ArrayList<>();
                List<Token> vars = new ArrayList<>();
                while (scriptTokens.get(i).getType() != TokenType.NEWLINE) {
                    var itemToken = scriptTokens.get(i);
                    if (itemToken.getType() == TokenType.VARIABLE_NAME) {
                        vars.add(itemToken);
                    }
                    scripts.add(itemToken);
                    i++;
                }
                for (Token var : vars) {
                    String name = var.getValue().substring(1);
                    var kp = getKeyAndProp(name);
                    var varKey = kp.getItem1();
                    if (!paramsMap.containsKey(varKey)) {
                        paramsMap.put(varKey, new LinkedHashSet<>());
                    }
                    var ss = scripts.stream().map(t -> {
                        if (t.getType() == TokenType.VARIABLE_NAME) {
                            var tkp = getKeyAndProp(t.getValue().substring(1));
                            if (tkp.getItem1().equals(varKey)) {
                                if (tkp.getItem2().isEmpty()) {
                                    return "_";
                                }
                                return "_" + code(tkp.getItem2(), HtmlUtil.Color.ANNOTATION);
                            }
                        }
                        return code(HtmlUtil.safeEscape(t.getValue()), HtmlUtil.Color.ANNOTATION);
                    }).collect(Collectors.joining(" "));
                    paramsMap.get(name).add(ss);
                }
            }
        }

        var plainSql = sqlTokens.stream()
                .map(Token::getValue)
                .collect(Collectors.joining(NEW_LINE));
        sqlGenerator.generatePreparedSql(plainSql, Map.of()).getArgNameIndexMapping()
                .keySet()
                .forEach(k -> {
                    var kp = getKeyAndProp(k);
                    if (!paramsMap.containsKey(kp.getItem1())) {
                        paramsMap.put(kp.getItem1(), new LinkedHashSet<>());
                    }
                    var prop = kp.getItem2();
                    var holder = "_";
                    if (!prop.isEmpty()) {
                        holder = holder + code(prop, HtmlUtil.Color.ANNOTATION);
                    }
                    paramsMap.get(kp.getItem1()).add(holder);
                });
        if (excludeTemplateHolder) {
            return paramsMap;
        }
        var tempP = SqlUtil.FMT.getPattern();
        var tempM = tempP.matcher(plainSql);
        while (tempM.find()) {
            var key = tempM.group("key");
            if (key.startsWith("!")) {
                key = key.substring(1);
            }
            if (key.contains(".")) {
                key = key.substring(0, key.indexOf("."));
            }
            if (!paramsMap.containsKey(key)) {
                paramsMap.put(key, new LinkedHashSet<>());
            }
            var temp = tempM.group(0).replace(key, "*");
            var coloredTemp = code(temp.substring(0, temp.indexOf("*")), HtmlUtil.Color.ANNOTATION) + code("_", HtmlUtil.Color.LIGHT) + code(temp.substring(temp.indexOf("*") + 1), HtmlUtil.Color.ANNOTATION);
            paramsMap.get(key).add(coloredTemp);
        }
        return paramsMap;
    }

    public static Pair<String, String> getKeyAndProp(String name) {
        String key = name;
        String prop = "";
        int dotIdx = name.indexOf(".");
        if (dotIdx != -1) {
            key = name.substring(0, dotIdx);
            prop = name.substring(dotIdx);
        }
        return Pair.of(key, prop);
    }

    public static String camelizeAndClean(String content) {
        var result = content.replace("_", "-");
        result = com.github.chengyuxing.common.utils.StringUtil.camelize(result);
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
