package com.github.chengyuxing.plugin.rabbit.sql;

import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class XqlFileListenOnStartup implements StartupActivity {
    private static final Logger log = Logger.getInstance(XqlFileListenOnStartup.class);
    /**
     * xql file manager to resolve and cache sql.
     */
    public static final XQLFileManager xqlFileManager = new XQLFileManager();

    @Override
    public void runActivity(@NotNull Project project) {
        if (project.getBasePath() != null) {
            Path basePath = Path.of(project.getBasePath());
            Path resources = basePath.resolve(Path.of("src", "main", "resources"));
            Path testResources = basePath.resolve(Path.of("src", "test", "resources"));
            findXql(resources);
            findXql(testResources);
            xqlFileManager.init();
        }
    }

    private void findXql(Path xqlRootPath) {
        if (Files.exists(xqlRootPath)) {
            try (Stream<Path> pathStream = Files.find(xqlRootPath, 5, (p, a) -> a.isRegularFile() && p.toString().endsWith(".xql"))) {
                pathStream.forEach(p -> xqlFileManager.add(p.toUri().toString()));
            } catch (IOException e) {
                log.error("find xql error.", e);
            }
        }
    }
}
