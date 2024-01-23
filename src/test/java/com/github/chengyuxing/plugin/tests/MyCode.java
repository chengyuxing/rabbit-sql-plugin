package com.github.chengyuxing.plugin.tests;

import com.fasterxml.jackson.jr.ob.JSON;
import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.script.IPipe;
import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.common.utils.ReflectUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.ClassFileLoader;
import com.github.chengyuxing.plugin.rabbit.sql.util.SimpleJavaCompiler;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.utils.SqlGenerator;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyCode {
    @Test
    public void test1() {
        Path p = Paths.get(URI.create("file:/Users/chengyuxing/IdeaProjects/rabbit-sql-plugin/src/main/resources/plugin.xml"));

        System.out.println(p.endsWith(Path.of("src", "main", "resources", "plugin.xml")));
    }

    @Test
    public void test2() throws IOException {
        String sql = Files.readString(Path.of("/Users/chengyuxing/IdeaProjects/rabbit-sql-plugin/src/test/resources/data.sql"));
        StringUtil.getTemplateParameters(sql)
                .forEach(System.out::println);
    }

    @Test
    public void test3() {
        Path path = Path.of("/Users/chengyuxing/IdeaProjects/rabbit-sql-plugin/src/main/resources/plugin.xml");
        System.out.println(path.toUri());
    }

    @Test
    public void test5() throws IOException {
        String sql = Files.readString(Path.of("/Users/chengyuxing/IdeaProjects/rabbit-sql-plugin/src/test/resources/data.sql"));
        var keyMapping = StringUtil.getParamsMappingInfo(new SqlGenerator(':'), sql);
        keyMapping.forEach((k, v) -> {
            System.out.println(k + ":" + v);
        });
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
//        System.out.println(StringUtil.isVarKeyInForExpression(sql, "user.name"));
    }

    @Test
    public void test38() throws IOException {
        var a = JSON.std.anyFrom("[{\"name\":\"cyx\"},{\"name\":\"jackson\"},{\"name\":\"ikun\"}]");
        var b = JSON.std.anyFrom("{\"id\":1}");
        System.out.println(a);
        System.out.println(b);
    }

    @Test
    public void testFile() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        var path = Path.of("/Users/chengyuxing/IdeaProjects/sbp-test1")
                .resolve(Path.of("target", "classes"));
        Class<?> clazz = ClassFileLoader.of(ClassLoader.getSystemClassLoader(), path).findClass("org.example.pipes.Big");
        System.out.println(ReflectUtil.getInstance(clazz));
    }

    @Test
    public void testx() throws MalformedURLException, ClassNotFoundException {
//        URLClassLoader classLoader = new URLClassLoader(new URL[]{new URL("file:/Users/chengyuxing/IdeaProjects/sbp-test1/target/classes/org/example/pipes")});
//        Class<?> clazz = classLoader.loadClass("org.example.pipes.Big");
//        System.out.println(clazz);
    }

    @Test
    public void test23() {
        var user1 = new User("cyx", 23);
        var user2 = new User("cyx", 23);
        System.out.println(user1.hashCode());
        System.out.println(user2.hashCode());
    }

    @Test
    public void testXQL() {
        var xql = new XQLFileManager();
        System.out.println(xql);
    }

    @Test
    public void testSbtApp() throws IOException {
        var p1 = Pair.of("a", "b");
        var p2 = Pair.of("a", "b");
        var a1 = "Hello world!";
        var a2 = "Hello world!";
        System.out.println(p1.equals(p2));
        System.out.println(p1.hashCode() + ":" + p2.hashCode());

        System.out.println(a1 == a2);
        System.out.println(a1.hashCode() + ":" + a2.hashCode());
    }

    @Test
    public void testYield() {
        System.out.println(ok(10));
    }

    public boolean ok(int i) {
        return switch (i) {
            case 5:
                yield true;
            default:
                yield false;
        };
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
//        Class<?> clazz = SimpleJavaCompiler.getInstance().compile("org.example.pipes.Big", Path.of("/Users/chengyuxing/IdeaProjects/sbp-test1/src/main/java/org/example/pipes/Big.java"));
//        IPipe<?> pipe = (IPipe<?>) clazz.getConstructor().newInstance();
//        System.out.println(pipe.transform(175));
    }

    public record User(String name, int age) {

    }
}
