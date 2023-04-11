package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.plugin.rabbit.sql.common.Store;
import com.intellij.psi.PsiElement;

import java.nio.file.Path;
import java.util.Objects;

public class PsiUtil {
    public static boolean xqlNotInFileManager(PsiElement element) {
        var currentFile = element.getContainingFile().getVirtualFile();
        if (Objects.equals(currentFile.getExtension(), "xql")) {
            String currentFilePath = element.getContainingFile().getVirtualFile().getPath();
            boolean in = Store.INSTANCE.xqlFileManager.allFiles().containsValue(Path.of(currentFilePath).toUri().toString());
            return !in;
        }
        return true;
    }
}
