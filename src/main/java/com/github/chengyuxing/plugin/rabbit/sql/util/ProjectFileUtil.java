package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public class ProjectFileUtil {
    public static boolean isXqlFileManagerConfig(String name) {
        return name.matches(Constants.SPRINGBOOT_CONFIG_PATTERN) || name.matches(Constants.CONFIG_PATTERN);
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
