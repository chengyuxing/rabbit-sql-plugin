package com.github.chengyuxing.plugin.tests;

import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.github.chengyuxing.sql.utils.SqlTranslator;
import org.junit.Test;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyCode {
    @Test
    public void test1() {
        Path p = Paths.get(URI.create("file:/Users/chengyuxing/IdeaProjects/rabbit-sql-plugin/src/main/resources/plugin.xml"));

        System.out.println(p.endsWith(Path.of("src", "main", "resources", "plugin.xml")));
    }

    @Test
    public void test2() {
        StringUtil.getTemplateParameters(new SqlTranslator(':'), "select ${:name} from users where ${cnd.user}")
                .forEach(System.out::println);
    }
}
