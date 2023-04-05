package com.github.chengyuxing.plugin.rabbit.sql;

import com.github.chengyuxing.common.utils.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.common.Store;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
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

    @Override
    public void runActivity(@NotNull Project project) {
        if (project.getBasePath() != null) {
            Path basePath = Path.of(project.getBasePath());
            Path src = basePath.resolve("src");
            findJava(src);
            findXql(src);
            Store.INSTANCE.xqlFileManager.init();
            Notifications.Bus.notify(new Notification("Rabbit-SQL Notification Group", "XQL file Manager initialized!", NotificationType.INFORMATION));
        }
    }

    private void findXql(Path xqlRootPath) {
        if (Files.exists(xqlRootPath)) {
            try (Stream<Path> pathStream = Files.find(xqlRootPath, 15, (p, a) -> a.isRegularFile() && p.toString().endsWith(".xql"))) {
                pathStream.forEach(p -> Store.INSTANCE.xqlFileManager.add(p.toUri().toString()));
            } catch (IOException e) {
                log.error("find xql error.", e);
            }
        }
    }

    private void findJava(Path rootPath) {
        if (Files.exists(rootPath)) {
            try (Stream<Path> pathStream = Files.find(rootPath, 15, (p, a) -> a.isRegularFile() && StringUtil.endsWiths(p.toString(), ".java", ".scala", ".kt"))) {
                pathStream.forEach(Store.INSTANCE.projectJavas::add);
            } catch (IOException e) {
                log.error("find java error.", e);
            }
        }
    }
}
