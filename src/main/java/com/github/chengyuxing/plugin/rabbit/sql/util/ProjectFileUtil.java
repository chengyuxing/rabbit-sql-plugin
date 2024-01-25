package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;

import java.net.URI;
import java.nio.file.Path;
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

    public static boolean isXqlFileManagerConfig(String name) {
        return name.matches(Constants.CONFIG_PATTERN);
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
}
