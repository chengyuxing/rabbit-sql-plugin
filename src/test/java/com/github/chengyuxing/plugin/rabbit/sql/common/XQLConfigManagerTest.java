package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.github.chengyuxing.common.script.pipe.IPipe;
import com.github.chengyuxing.plugin.rabbit.sql.ui.XqlFileManagerToolWindow;
import com.github.chengyuxing.plugin.rabbit.sql.util.ClassFileLoader;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.github.chengyuxing.sql.util.SqlGenerator;
import com.intellij.mock.MockProject;
import com.intellij.mock.MockVirtualFile;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class XQLConfigManagerTest {
    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void concurrentAddsKeepAllConfigsAndExposeStableSnapshots() throws Exception {
        var manager = new XQLConfigManager(null);
        var executor = Executors.newFixedThreadPool(8);
        try {
            Path module = temporary.newFolder("module").toPath();
            var start = new CountDownLatch(1);
            var tasks = new ArrayList<java.util.concurrent.Future<?>>();
            for (int i = 0; i < 32; i++) {
                var config = config(manager, module, module.resolve("config-" + i + ".yml"));
                tasks.add(executor.submit(() -> {
                    start.await();
                    manager.add(module, config);
                    return null;
                }));
            }
            start.countDown();
            for (var task : tasks) {
                task.get(10, TimeUnit.SECONDS);
            }
            var snapshot = manager.getConfigs(module);
            var mapSnapshot = manager.getConfigMap();
            assertEquals(32, snapshot.size());
            var replacement = config(manager, module, module.resolve("config-0.yml"));
            manager.add(module, replacement);
            assertEquals(32, manager.getConfigs(module).size());
            assertSame(replacement, manager.getConfigs(module).stream().filter(replacement::equals).findFirst().orElseThrow());
            assertNotSame(replacement, snapshot.stream().filter(replacement::equals).findFirst().orElseThrow());
            manager.toggleActive(replacement);
            assertSame(replacement, manager.getActiveConfig(module));
            assertEquals(1, manager.getConfigs(module).stream().filter(XQLConfigManager.Config::isActive).count());
            var iterator = snapshot.iterator();
            manager.add(module, config(manager, module, module.resolve("extra.yml")));
            while (iterator.hasNext()) {
                iterator.next();
            }
            assertEquals(32, mapSnapshot.get(module).size());
            assertEquals(33, manager.getConfigs(module).size());
            assertThrows(UnsupportedOperationException.class, snapshot::clear);
            assertThrows(UnsupportedOperationException.class, mapSnapshot::clear);
        } finally {
            executor.shutdownNow();
            manager.dispose();
        }
    }

    @Test
    public void reloadPreservesSelectionAndOnlyOneActiveConfig() throws Exception {
        var manager = new XQLConfigManager(null);
        Path module = temporary.newFolder("active").toPath();
        Path resources = Files.createDirectories(module.resolve("src/main/resources"));
        Path primary = Files.writeString(resources.resolve("xql-file-manager.yml"), "files: {}\n");
        Path local = Files.writeString(resources.resolve("xql-file-manager-local.yml"), "files: {}\n");
        try {
            var primaryConfig = config(manager, module, primary);
            var selected = config(manager, module, local);
            manager.add(module, selected);
            manager.add(module, primaryConfig);
            assertSame(primaryConfig, manager.getActiveConfig(module));
            manager.toggleActive(selected);
            for (Path file : new Path[]{primary, local, local, primary}) {
                manager.add(module, config(manager, module, file));
                assertEquals(local, manager.getActiveConfig(module).getConfigPath());
                assertEquals(1, manager.getConfigs(module).stream().filter(XQLConfigManager.Config::isActive).count());
            }
            // Tree nodes can still hold the replaced object until the UI refreshes.
            manager.toggleActive(primaryConfig);
            assertEquals(primary, manager.getActiveConfig(module).getConfigPath());
            manager.toggleActive(config(manager, module, resources.resolve("unregistered.yml")));
            assertEquals(primary, manager.getActiveConfig(module).getConfigPath());
        } finally {
            manager.dispose();
        }
    }

    @Test
    public void pipeReloadUsesRecompiledClassesAndChangedAliasesAcrossOutputRoots() throws Exception {
        var manager = new XQLConfigManager(null);
        Path module = temporary.newFolder("pipes").toPath();
        Path maven = module.resolve("target/classes");
        Path java = module.resolve("build/classes/java/main");
        Path kotlin = module.resolve("build/classes/kotlin/main");
        var xql = manager.new PluginXQLFileManager(module) {
            @Override
            protected String messagePrefix() {
                return "test: ";
            }
        };
        try {
            compile(maven, "pipes.Value", pipeSource("Value", "\"first\""));
            xql.setPipes(Map.of("value", "pipes.Value"));
            xql.init();
            var original = xql.getPipeInstances().get("value");
            assertEquals("first", original.transform(null));
            compile(maven, "pipes.Value", pipeSource("Value", "\"updated\""));
            xql.init();
            var updated = xql.getPipeInstances().get("value");
            assertEquals("updated", updated.transform(null));
            assertNotSame(original.getClass(), updated.getClass());
            compile(java, "pipes.Helper", "package pipes; public class Helper { public static String value() { return \"gradle\"; } }");
            compile(kotlin, "pipes.Other", pipeSource("Other", "Helper.value()"), java);
            xql.setPipes(Map.of("value", "pipes.Other"));
            xql.init();
            assertEquals("gradle", xql.getPipeInstances().get("value").transform(null));
        } finally {
            xql.close();
            manager.dispose();
        }
    }

    @Test
    public void classLoaderCachesClassesAndThrowsForMissingClass() throws Exception {
        Path output = temporary.newFolder("classes").toPath();
        compile(output, "pipes.Value", pipeSource("Value", "\"value\""));
        var loader = ClassFileLoader.of(getClass().getClassLoader(), output);
        assertSame(loader.loadClass("pipes.Value"), loader.loadClass("pipes.Value"));
        assertSame(IPipe.class, loader.loadClass(IPipe.class.getName()));
        assertThrows(ClassNotFoundException.class, () -> loader.loadClass("pipes.Missing"));
    }

    @Test
    public void emptyFilesReloadClearsResourcesAndOriginalPaths() throws Exception {
        var project = new MockProject(null, () -> {});
        var manager = new XQLConfigManager(project);
        Path module = temporary.newFolder("reload").toPath();
        Path resources = Files.createDirectories(module.resolve("src/main/resources"));
        Path yaml = resources.resolve("xql-file-manager.yml");
        Files.writeString(resources.resolve("home.xql"), "/* [find] */\nselect :id;\n");
        Files.writeString(yaml, "files:\n  home: home.xql\n");
        var config = config(manager, module, yaml);
        try {
            config.initXqlFileManager();
            assertEquals("select :id", config.getXqlFileManager().get("home.find"));
            assertEquals(1, config.getOriginalXqlFiles().size());
            var snapshot = config.getXqlFileManagerConfig();
            var modifiedSnapshot = config.getXqlFileManagerConfig();
            modifiedSnapshot.getFiles().clear();
            assertEquals(1, config.getXqlFileManagerConfig().getFiles().size());
            Files.writeString(yaml, "files: {}\n");
            config.initXqlFileManager();
            assertTrue(config.getXqlFileManager().getResources().isEmpty());
            assertTrue(config.getOriginalXqlFiles().isEmpty());
            assertEquals(1, snapshot.getFiles().size());
            Files.writeString(yaml, "files:\n  home: home.xql\n");
            config.initXqlFileManager();
            assertFalse(config.getXqlFileManager().getResources().isEmpty());
            Files.writeString(yaml, "# cleared\n");
            config.initXqlFileManager();
            assertTrue(config.getXqlFileManager().getResources().isEmpty());
            assertTrue(config.getOriginalXqlFiles().isEmpty());
        } finally {
            config.close();
            manager.dispose();
            project.dispose();
        }
    }

    @Test
    public void disposedProjectSkipsToolWindowCallback() {
        var project = new MockProject(null, () -> {});
        project.dispose();
        assertTrue(project.isDisposed());
        XqlFileManagerToolWindow.getXqlFileManagerPanel(project, panel -> fail("Disposed project callback ran"));
    }

    @Test
    public void bundledSqlGeneratorPreservesDollarQuotedText() {
        String sql = "select $$:text$$, $body$:other$body$, :id";
        var result = new SqlGenerator(':').generatePreparedSql(sql, Map.of("id", 1));
        assertEquals("select $$:text$$, $body$:other$body$, ?", result.getPrepareSql());
        assertEquals(Set.of("id"), result.getArgNameIndexMapping().keySet());
        assertEquals(Set.of("id"), StringUtil.getParamsMappingInfo(new SqlGenerator(':'), sql).keySet());
    }

    private XQLConfigManager.Config config(XQLConfigManager manager, Path module, Path yaml) {
        var config = manager.newConfig(virtualFile(module));
        config.setConfigVfs(virtualFile(yaml));
        return config;
    }

    private MockVirtualFile virtualFile(Path path) {
        return new MockVirtualFile(path.getFileName().toString()) {
            @Override
            public Path toNioPath() {
                return path;
            }
        };
    }

    private String pipeSource(String name, String expression) {
        return "package pipes; public class " + name + " implements " + IPipe.class.getName()
                + "<String> { public String transform(Object value, Object... params) { return " + expression + "; } }";
    }

    private void compile(Path output, String name, String source, Path... dependencies) throws Exception {
        Files.createDirectories(output);
        Path file = temporary.newFolder().toPath().resolve(name.substring(name.lastIndexOf('.') + 1) + ".java");
        Files.writeString(file, source);
        var classpath = new ArrayList<String>();
        Path api = temporary.newFolder("api-" + java.util.UUID.randomUUID()).toPath();
        Path pipeClass = api.resolve(IPipe.class.getName().replace('.', '/') + ".class");
        Files.createDirectories(pipeClass.getParent());
        try (var stream = IPipe.class.getResourceAsStream("IPipe.class")) {
            Files.copy(stream, pipeClass);
        }
        classpath.add(api.toString());
        for (Path dependency : dependencies) {
            classpath.add(dependency.toString());
        }
        var javac = Path.of(System.getProperty("java.home"), "bin", "javac").toString();
        assertEquals(0, new ProcessBuilder(javac,
                "-classpath", String.join(java.io.File.pathSeparator, classpath),
                "-d", output.toString(), file.toString()).inheritIO().start().waitFor());
    }
}
