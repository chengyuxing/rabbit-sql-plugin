package com.github.chengyuxing.plugin.tests;

import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.script.expression.IPipe;
import com.github.chengyuxing.common.utils.ReflectUtil;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLMapperConfig;
import com.github.chengyuxing.plugin.rabbit.sql.util.ClassFileLoader;
import com.github.chengyuxing.plugin.rabbit.sql.util.SimpleJavaCompiler;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.github.chengyuxing.sql.Args;
import com.github.chengyuxing.sql.utils.SqlGenerator;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MyCode {
    //    @Test
    public void test1() {
        Path p = Paths.get(URI.create("file:/Users/chengyuxing/IdeaProjects/rabbit-sql-plugin/src/main/resources/plugin.xml"));

        System.out.println(p.endsWith(Path.of("src", "main", "resources", "plugin.xml")));
    }

    //    @Test
    public void test2() throws IOException {
        String sql = Files.readString(Path.of("/Users/chengyuxing/IdeaProjects/rabbit-sql-plugin/src/test/resources/data.sql"));
        StringUtil.getTemplateParameters(sql)
                .forEach(System.out::println);
    }

    //    @Test
    public void test3() {
        Path path = Path.of("/Users/chengyuxing/IdeaProjects/rabbit-sql-plugin/src/main/resources/plugin.xml");
        System.out.println(path.toUri());
    }

    //    @Test
    public void test5() throws IOException {
        String sql = Files.readString(Path.of("/Users/chengyuxing/IdeaProjects/rabbit-sql-plugin/src/test/resources/data.sql"));
        var keyMapping = StringUtil.getParamsMappingInfo(new SqlGenerator(':'), sql);
        keyMapping.forEach((k, v) -> {
            System.out.println(k + ":" + v);
        });
    }

    //    @Test
    public void test38() throws IOException {
//        var a = JSON.std.anyFrom("[{\"name\":\"cyx\"},{\"name\":\"jackson\"},{\"name\":\"ikun\"}]");
//        var b = JSON.std.anyFrom("{\"id\":1}");
//        System.out.println(a);
//        System.out.println(b);
    }

    //    @Test
    public void testFile() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        var path = Path.of("/Users/chengyuxing/IdeaProjects/sbp-test1")
                .resolve(Path.of("target", "classes"));
        Class<?> clazz = ClassFileLoader.of(ClassLoader.getSystemClassLoader(), path).findClass("org.example.pipes.Big");
        System.out.println(ReflectUtil.getInstance(clazz));
    }

    //    @Test
    public void testx() throws MalformedURLException, ClassNotFoundException {
        URLClassLoader classLoader = new URLClassLoader(new URL[]{new URL("file:/Users/chengyuxing/IdeaProjects/sbp-test1/target/classes/org/example/pipes")});
        Class<?> clazz = classLoader.loadClass("org.example.pipes.Big");
        System.out.println(clazz);
    }

    //    @Test
    public void test23() {
//        var user1 = new User("cyx", 23);
//        var user2 = new User("cyx", 23);
//        System.out.println(user1.hashCode());
//        System.out.println(user2.hashCode());
//        System.out.println(user1.equals(user2));
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> clazz = SimpleJavaCompiler.getInstance().compile("org.example.pipes.Big", Path.of("/Users/chengyuxing/IdeaProjects/sbp-test1/src/main/java/org/example/pipes/Big.java"));
        IPipe<?> pipe = (IPipe<?>) clazz.getConstructor().newInstance();
        System.out.println(pipe.transform(175));
    }

    private static final Map<String, Object> nodeMap = new LinkedHashMap<>();

    @Test
    public void testNestTree() {
        System.out.println(String.join("/", List.of()));
    }

    @Test
    public void testYml() {
        Map<String, Object> nodeMap = new LinkedHashMap<>();
        nodeMap.put("name", "cyx");
        nodeMap.put("age", 23);
        nodeMap.put("nodeMap", Args.of("a", "b", "c", 1));
        nodeMap.put("nodeList", List.of(1, 2, 3, 4));

        var yml = new Yaml();
        var res = yml.dump(nodeMap);
        System.out.println("---");
        System.out.println(res);

        var loaded = yml.load("# Rabbit-SQL-Plugin - XQL mapper generate configuration - DO NOT MODIFY\n" + res);
        System.out.println(loaded);
    }

    @Test
    public void testYml2() {
        var yml = new Yaml();
        var c = yml.loadAs(new FileResource("file:///Users/chengyuxing/IdeaProjects/rabbit-sql-quick-start/src/main/resources/xqls/home.xql.mappers").getInputStream(), XQLMapperConfig.class);
        System.out.println(c);
    }
}
