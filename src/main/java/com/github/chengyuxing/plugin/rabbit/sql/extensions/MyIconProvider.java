package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.file.XqlIcons;
import com.intellij.ide.IconProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MyIconProvider extends IconProvider {
    @Override
    public @Nullable Icon getIcon(@NotNull PsiElement element, int flags) {
        var psiFile = element.getContainingFile();
        if (psiFile != null) {
            var name = psiFile.getName();
            if (name.endsWith(".xql")) {
                return XqlIcons.XQL_FILE;
            }
            if (name.equals(Constants.CONFIG_NAME)) {
                return XqlIcons.XQL_FILE_MANAGER;
            }
            if (name.matches(Constants.CONFIG_PATTERN)) {
                return XqlIcons.XQL_FILE_MANAGER_SECONDARY;
            }
        }
        return null;
    }
}
