package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.common.MostDateTime;
import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.sql.Args;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Optional;

public class ProjectFileUtil {
    public static Document getDocument(Project project, VirtualFile virtualFile) {
        if (Objects.isNull(virtualFile)) {
            return null;
        }
        var psi = PsiManager.getInstance(project).findFile(virtualFile);
        if (Objects.isNull(psi)) {
            return null;
        }
        var doc = PsiDocumentManager.getInstance(project).getDocument(psi);
        if (Objects.isNull(doc)) {
            return null;
        }
        return doc;
    }

    public static void openFile(Project project, Path file, boolean refresh) {
        var newVf = refresh ?
                LocalFileSystem.getInstance().refreshAndFindFileByNioFile(file) :
                VirtualFileManager.getInstance().findFileByNioPath(file);
        if (Objects.isNull(newVf)) {
            return;
        }
        var psi = PsiManager.getInstance(project).findFile(newVf);
        if (Objects.isNull(psi)) {
            return;
        }
        NavigationUtil.activateFileWithPsiElement(psi);
    }


    public static boolean isXqlFileManagerConfig(String name) {
        return name.matches(Constants.CONFIG_PATTERN);
    }

    public static boolean isProjectModule(VirtualFile module) {
        var mPath = module.toNioPath();
        var resourcesPath = mPath.resolve(Constants.RESOURCE_ROOT);
        return Files.exists(resourcesPath);
    }

    public static void createXqlConfigByTemplate(Project project, Path absFilename, Runnable then) {
        try {
            var xqlConfig = FileTemplateManager.getInstance(project).getTemplate("XQL File Manager.yml");
            var now = MostDateTime.now();
            var args = Args.of(
                    "USER", System.getProperty("user.name"),
                    "DATE", now.toString("yyyy/MM/dd"),
                    "TIME", now.toString("HH:mm:ss")
            );
            var path = absFilename.getParent();
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            var template = xqlConfig.getText(args);
            Files.writeString(absFilename, template, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            then.run();
        } catch (IOException ex) {
            NotificationUtil.showMessage(project, "Error", ex.getMessage(), NotificationType.ERROR);
        }
    }

    public static VirtualFile findXqlByAlias(String alias, XQLConfigManager.Config config) {
        var resource = config.getXqlFileManager().getResource(alias);
        if (Objects.isNull(resource)) {
            return null;
        }
        var filename = resource.getFilename();
        return VirtualFileManager.getInstance()
                .findFileByNioPath(Path.of(URI.create(filename)));
    }

    public static VirtualFile getValidVirtualFile(VirtualFile file) {
        if (Objects.isNull(file)) return null;
        if (file.isValid()) return file;
        return getValidVirtualFile(file.getParent());
    }

    public static VirtualFile findModule(Project project, VirtualFile fileBelongs) {
        if (Objects.isNull(fileBelongs)) {
            return null;
        }
        var module = ModuleUtil.findModuleForFile(fileBelongs, project);
        if (Objects.isNull(module)) {
            return null;
        }
        return ProjectUtil.guessModuleDir(module);
    }

    public static VirtualFile findModule(Project project, PsiElement element) {
        var file = element.getContainingFile();
        if (!file.isPhysical()) {
            file = file.getOriginalFile();
        }
        var vf = file.getVirtualFile();
        return findModule(project, vf);
    }

    public static Path getModulePath(Project project, PsiElement element) {
        return Optional.ofNullable(findModule(project, element))
                .map(VirtualFile::toNioPath)
                .orElse(null);
    }

    public static long lineNumber(Path path) {
        try (var fr = new FileReader(path.toFile());
             var lr = new LineNumberReader(fr)) {
            //noinspection ResultOfMethodCallIgnored
            lr.skip(Long.MAX_VALUE);
            return lr.getLineNumber();
        } catch (IOException e) {
            return 0;
        }
    }
}
