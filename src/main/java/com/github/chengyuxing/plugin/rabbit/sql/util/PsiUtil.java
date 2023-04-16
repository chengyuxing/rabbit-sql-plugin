package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;

import java.nio.file.Path;

public class PsiUtil {
    public static Path getModuleDir(Project project, VirtualFile virtualFile) {
        if (virtualFile == null) {
            return null;
        }
        Module module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(virtualFile);
        if (module == null)
            return null;
        var vf = ProjectUtil.guessModuleDir(module);
        if (vf == null)
            return null;
        return vf.toNioPath();
    }

    public static Path getModuleDir(Project project, PsiElement element) {
        var file = element.getContainingFile();
        if (!file.isPhysical()) {
            file = file.getOriginalFile();
        }
        var vf = file.getVirtualFile();
        return getModuleDir(project, vf);
    }

    public static Path getModuleDir(PsiElement element) {
        return getModuleDir(element.getProject(), element);
    }
}
