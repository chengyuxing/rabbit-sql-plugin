package com.github.chengyuxing.plugin.tests;

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
        var p = Path.of("src");
        System.out.println(p.resolve(Path.of("main", "resource")));
    }
}
