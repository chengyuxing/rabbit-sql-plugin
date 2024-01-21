package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.StringJoiner;

public class PsiUtil {

    public static void saveUnsavedXqlAndConfig(@NotNull Project project) {
        var fileDocumentManager = FileDocumentManager.getInstance();
        var unsaved = fileDocumentManager.getUnsavedDocuments();
        for (Document doc : unsaved) {
            var psi = PsiDocumentManager.getInstance(project).getPsiFile(doc);
            if (Objects.nonNull(psi)) {
                var vf = psi.getVirtualFile();
                if (Objects.nonNull(vf) && vf.exists()) {
                    var filename = vf.getName();
                    var ext = vf.getExtension();
                    if (Objects.equals(ext, "xql")) {
                        fileDocumentManager.saveDocument(doc);
                        continue;
                    }
                    if (filename.matches(Constants.CONFIG_PATTERN)) {
                        fileDocumentManager.saveDocument(doc);
                    }
                }
            }
        }
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
