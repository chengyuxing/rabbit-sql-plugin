package com.github.chengyuxing.plugin.tests;

import com.fasterxml.jackson.jr.ob.JSON;
import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.utils.ReflectUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.PathUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.utils.SqlTranslator;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

    @Test
    public void test3() {
        Path path = Path.of("/Users/chengyuxing/IdeaProjects/rabbit-sql-plugin/src/main/resources/plugin.xml");
        System.out.println(path.toUri());
    }

    @Test
    public void test4() {
        var a = ReflectUtil.json2Obj("[{\"a\":\"cyx\"}]", List.class);
        System.out.println(a);
    }

    @Test
    public void test5() throws IOException {
        String sql = Files.readString(Path.of("/Users/chengyuxing/IdeaProjects/rabbit-sql-plugin/src/test/resources/data.sql"));
        var keyMapping = StringUtil.getParamsMappingInfo(new SqlTranslator(':'), sql);
        keyMapping.forEach((k, v) -> {
            System.out.println(k + ":" + v);
        });
    }

    @Test
    public void test33() {
        var path = Path.of("/Users/chengyuxing/IdeaProjects/rabbit-sql-plugin/src/test/resources/data.sql");
        System.out.println(Path.of("/abc/ddd.my.xql").toUri().toString());
        System.out.println(PathUtil.backward(path, 4).getFileName());
    }

    @Test
    public void test35() {
        System.out.println(Files.exists(Path.of("")));
    }

    @Test
    public void test36() {
        XQLFileManager xqlFileManager = new XQLFileManager("bbb.yml");
        System.out.println(xqlFileManager);
    }

    @Test
    public void test37() {
        var sql = new FileResource("data.sql").readString(StandardCharsets.UTF_8);
        System.out.println(StringUtil.isTemplateKeyInForExpression(sql, "user.name"));
    }

    @Test
    public void test38() throws IOException {
        var a = JSON.std.anyFrom("[{\"name\":\"cyx\"},{\"name\":\"jackson\"},{\"name\":\"ikun\"}]");
        var b = JSON.std.anyFrom("{\"id\":1}");
        System.out.println(a);
        System.out.println(b);
    }
}
