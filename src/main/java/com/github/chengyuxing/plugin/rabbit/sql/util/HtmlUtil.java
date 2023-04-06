package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.common.utils.StringUtil;
import com.github.chengyuxing.sql.Keywords;
import com.github.chengyuxing.sql.utils.SqlUtil;
import com.intellij.openapi.diagnostic.Logger;

import java.util.List;
import java.util.Map;

import static com.github.chengyuxing.sql.utils.SqlUtil.getAnnotationBlock;

public class HtmlUtil {
    private static final Logger log = Logger.getInstance(HtmlUtil.class);

    public static String toHtml(String sql) {
        return "<html lang=\"en\"><body><pre style='font-size:1em'>" + highlight(sql) + "</pre></body></html>";
    }

    public static String highlight(String sql) {
        try {
            Pair<String, Map<String, String>> r = SqlUtil.replaceSqlSubstr(sql);
            String rSql = r.getItem1();
            Pair<List<String>, List<String>> x = StringUtil.regexSplit(rSql, "(?<sp>[\\s,\\[\\]()::;])", "sp");
            List<String> maybeKeywords = x.getItem1();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < maybeKeywords.size(); i++) {
                String key = maybeKeywords.get(i);
                if (!key.trim().equals("")) {
                    // keywords highlight
                    if (StringUtil.equalsAnyIgnoreCase(key, Keywords.STANDARD) || StringUtil.equalsAnyIgnoreCase(key, Keywords.POSTGRESQL)) {
                        maybeKeywords.set(i, colorful(key, Color.KEYWORD));
                        // functions highlight
                    } else if (StringUtil.containsAnyIgnoreCase(key, Keywords.FUNCTIONS)) {
                        if (rSql.contains(key + "(")) {
                            maybeKeywords.set(i, colorful(key, Color.FUNCTION));
                        }
                        // number highlight
                    } else if (StringUtil.isNumeric(key)) {
                        maybeKeywords.set(i, colorful(key, Color.NUMBER));
                        // PostgreSQL function body block highlight
                    } else if (key.equals("$$")) {
                        maybeKeywords.set(i, colorful(key, Color.STRING));
                        // symbol '*' highlight
                    } else if (key.contains("*") && !key.contains("/*") && !key.contains("*/")) {
                        maybeKeywords.set(i, key.replace("*", colorful(key, Color.KEYWORD)));
                    }
                }
                sb.append(maybeKeywords.get(i));
                if (i < maybeKeywords.size() - 1) {
                    sb.append(x.getItem2().get(i));
                }
            }
            String colorfulSql = sb.toString();
            // reinsert the sub string
            Map<String, String> subStr = r.getItem2();
            for (String key : subStr.keySet()) {
                colorfulSql = colorfulSql.replace(key, colorful(subStr.get(key), Color.STRING));
            }
            // resolve single annotation
            String[] sqlLine = colorfulSql.split("\n");
            for (int i = 0; i < sqlLine.length; i++) {
                String line = sqlLine[i];
                if (line.trim().startsWith("--")) {
                    sqlLine[i] = colorful(line, Color.ANNOTATION);
                } else if (line.contains("--")) {
                    int idx = line.indexOf("--");
                    sqlLine[i] = line.substring(0, idx) + colorful(line.substring(idx), Color.ANNOTATION);
                }
            }
            colorfulSql = String.join("\n", sqlLine);
            // resolve block annotation
            if (colorfulSql.contains("/*") && colorfulSql.contains("*/")) {
                List<String> annotations = getAnnotationBlock(colorfulSql);
                for (String annotation : annotations) {
                    colorfulSql = colorfulSql.replace(annotation, colorful(annotation, Color.ANNOTATION));
                }
            }
            return colorfulSql;
        } catch (Exception e) {
            log.error(e);
            return sql;
        }
    }

    static String colorful(String word, Color color) {
        return "<code style='color:" + color.getCode() + "'>" + word + "</code>";
    }

    public enum Color {
        KEYWORD("#CC7832"),
        NUMBER("#56A9B6"),
        FUNCTION("#B89E30"),
        STRING("#79A978"),
        ANNOTATION("#7B7E84");
        private final String code;

        Color(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
}
