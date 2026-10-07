package com.github.chengyuxing.plugin.rabbit.sql.common;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import static org.junit.Assert.*;

public class XQLMapperConfigTest {
    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void missingEmptyAndCommentOnlyFilesReturnUsableDefaults() throws Exception {
        var path = temporary.getRoot().toPath().resolve("home.xql.rbm");
        assertTrue(XQLMapperConfig.load(path).getMethods().isEmpty());
        Files.writeString(path, "");
        assertTrue(XQLMapperConfig.load(path).getMethods().isEmpty());
        Files.writeString(path, "# generated configuration\n");
        assertTrue(XQLMapperConfig.load(path).getMethods().isEmpty());
    }

    @Test
    public void mapperConfigurationRoundTrips() throws Exception {
        var path = temporary.getRoot().toPath().resolve("home.xql.rbm");
        var config = new XQLMapperConfig();
        config.setPackageName("example.mapper");
        config.setBaki("secondaryBaki");
        var method = new XQLMapperConfig.XQLMethod();
        method.setSqlType("query");
        config.setMethods(Map.of("find", method));
        config.saveTo(path);
        var loaded = XQLMapperConfig.load(path);
        assertEquals("example.mapper", loaded.getPackageName());
        assertEquals("secondaryBaki", loaded.getBaki());
        assertEquals("query", loaded.getMethods().get("find").getSqlType());
    }

    @Test
    public void failedSavePropagatesToGenerationTask() throws Exception {
        var directory = temporary.newFolder("not-a-file").toPath();
        assertThrows(IOException.class, () -> new XQLMapperConfig().saveTo(directory));
        assertTrue(Files.isDirectory(directory));
    }
}
