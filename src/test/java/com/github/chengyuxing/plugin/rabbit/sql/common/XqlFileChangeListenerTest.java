package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.testFramework.HeavyPlatformTestCase;
import com.intellij.testFramework.PlatformTestUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

public class XqlFileChangeListenerTest extends HeavyPlatformTestCase {
    public void testRegisteredSqlChangesReloadAndUnsavedSqlIsSavedByManualReload() throws Exception {
        Path modulePath = Path.of(getProject().getProjectFilePath()).getParent();
        var module = VfsUtil.createDirectories(modulePath.toString());
        assertNotNull(module);
        ModuleRootModificationUtil.addContentRoot(getModule(), modulePath.toString());
        Path resources = Files.createDirectories(modulePath.resolve("src/main/resources"));
        Path sql = Files.writeString(resources.resolve("home.sql"), "/* [find] */\nselect 1;\n");
        Files.setLastModifiedTime(sql, FileTime.fromMillis(1000));
        Path yaml = Files.writeString(resources.resolve("xql-file-manager.yml"), "files:\n  home: home.sql\n");
        var fileSystem = LocalFileSystem.getInstance();
        var yamlFile = fileSystem.refreshAndFindFileByNioFile(yaml);
        var sqlFile = fileSystem.refreshAndFindFileByNioFile(sql);
        assertNotNull(yamlFile);
        assertNotNull(sqlFile);
        var manager = XQLConfigManager.getInstance(getProject());
        var config = manager.newConfig(module);
        config.setConfigVfs(yamlFile);
        config.initXqlFileManager();
        manager.add(modulePath, config);
        assertEquals("select 1", loadedSql(manager, modulePath));
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                VfsUtil.saveText(sqlFile, "/* [find] */\nselect 2;\n");
            } catch (java.io.IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
        });
        PlatformTestUtil.waitWithEventsDispatching("Registered .sql did not reload",
                () -> "select 2".equals(loadedSql(manager, modulePath)), 10);
        var documents = FileDocumentManager.getInstance();
        var document = documents.getDocument(sqlFile);
        assertNotNull(document);
        WriteCommandAction.runWriteCommandAction(getProject(), () -> document.setText("/* [find] */\nselect 3;\n"));
        assertTrue(documents.isDocumentUnsaved(document));
        PsiUtil.saveUnsavedXqlAndConfig(getProject());
        assertFalse(documents.isDocumentUnsaved(document));
        PlatformTestUtil.waitWithEventsDispatching("Saved .sql did not reload",
                () -> "select 3".equals(loadedSql(manager, modulePath)), 10);
    }

    private String loadedSql(XQLConfigManager manager, Path module) {
        var config = manager.getActiveConfig(module);
        if (config == null) {
            return null;
        }
        var resource = config.getXqlFileManager().getResource("home");
        if (resource == null || !resource.getEntry().containsKey("find")) {
            return null;
        }
        return config.getXqlFileManager().get("home.find");
    }
}
