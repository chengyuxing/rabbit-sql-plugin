package com.github.chengyuxing.plugin.rabbit.sql.psi;

import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.file.XqlIcons;
import com.intellij.ide.IconProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class XqlFileManagerIconProvider extends IconProvider {
    @Override
    public @Nullable Icon getIcon(@NotNull PsiElement element, int flags) {
        var psiFile = element.getContainingFile();
        if (psiFile != null) {
            if (psiFile.getName().equals(Constants.CONFIG_NAME)) {
                return XqlIcons.XQL_FILE_MANAGER;
            }
        }
        return null;
    }
}
