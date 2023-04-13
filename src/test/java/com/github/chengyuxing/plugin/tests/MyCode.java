package com.github.chengyuxing.plugin.tests;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;

public class MyCode {
    @Test
    public void test1() {
        Path p = Paths.get("file:/Users/chengyuxing/IdeaProjects/rabbit-sql-plugin/src/main/resources/META-INF/plugin.xml");
        System.out.println(p.getFileName());
    }
}
