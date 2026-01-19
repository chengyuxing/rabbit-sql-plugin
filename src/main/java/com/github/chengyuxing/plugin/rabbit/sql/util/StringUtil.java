package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.common.script.Token;
import com.github.chengyuxing.common.script.TokenType;
import com.github.chengyuxing.common.script.lexer.RabbitScriptLexer;
import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.common.util.NamingUtils;
import com.github.chengyuxing.common.util.StringUtils;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.util.SqlGenerator;
import com.github.chengyuxing.sql.util.SqlUtils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.chengyuxing.common.util.StringUtils.NEW_LINE;
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
        Map<String, Set<String>> paramsMap = new LinkedHashMap<>();
        RabbitScriptLexer lexer = new RabbitScriptLexer(sql) {
            @Override
            protected String normalizeDirectiveLine(String line) {
                int idx = SqlUtils.indexOfWholeLineComment(line);
                if (idx != -1) {
                    return line.substring(idx + 2);
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

        var localParams = new HashSet<String>();
        for (int i = 0, j = scriptTokens.size(); i < j; i++) {
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
                    } else if (itemToken.getType() == TokenType.FOR) {
                        // #for item,idx of :list
                        if (i + 1 < j && scriptTokens.get(i + 1).getType() == TokenType.IDENTIFIER) {
                            localParams.add(scriptTokens.get(i + 1).getValue());
                        }
                        if (i + 3 < j && scriptTokens.get(i + 3).getType() == TokenType.IDENTIFIER) {
                            localParams.add(scriptTokens.get(i + 3).getValue());
                        }
                    } else if (itemToken.getType() == TokenType.DEFINE_VAR) {
                        // #var id =
                        if (i + 1 < j && scriptTokens.get(i + 1).getType() == TokenType.IDENTIFIER) {
                            localParams.add(scriptTokens.get(i + 1).getValue());
                        }
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
                    paramsMap.get(varKey).add(ss);
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

        var tempP = StringUtils.FMT.getPattern();
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
        paramsMap.entrySet().removeIf(entry -> localParams.contains(entry.getKey()));
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
