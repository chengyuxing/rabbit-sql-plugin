package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;

import java.nio.file.Path;
import java.util.Objects;
import java.util.StringJoiner;

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

    public static boolean projectContains(Project project, VirtualFile virtualFile) {
        return ProjectRootManager.getInstance(project).getFileIndex().isInContent(virtualFile);
    }

    public static String getClassName(PsiElement childElement) {
        var clazz = com.intellij.psi.util.PsiUtil.getTopLevelClass(childElement);
        if (clazz != null) {
            return clazz.getQualifiedName();
        }
        return null;
    }

    public static String findMethod(PsiElement inCodeBody) {
        if (inCodeBody == null) {
            return "";
        }
        var codeBlock = com.intellij.psi.util.PsiUtil.getTopLevelEnclosingCodeBlock(inCodeBody, null);
        var clazz = com.intellij.psi.util.PsiUtil.getTopLevelClass(inCodeBody);
        if (clazz != null) {
            var methods = clazz.getAllMethods();
            for (var m : methods) {
                var id = m.getNameIdentifier();
                if (id == null) {
                    return "";
                }
                if (Objects.equals(m.getBody(), codeBlock)) {
                    var params = m.getParameterList();
                    var joiner = new StringJoiner(", ", "(", ")");
                    for (var i = 0; i < params.getParametersCount(); i++) {
                        var p = params.getParameter(i);
                        if (p != null) {
                            var t = p.getType().getPresentableText();
                            joiner.add(t);
                        }
                    }
                    return id.getText() + joiner;
                }
            }
        }
        return "";
    }
}
