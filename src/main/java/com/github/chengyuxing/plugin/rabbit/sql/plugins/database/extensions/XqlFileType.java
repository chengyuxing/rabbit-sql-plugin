package com.github.chengyuxing.plugin.rabbit.sql.plugins.database.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.file.XqlIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.sql.psi.SqlLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class XqlFileType extends LanguageFileType {
    public static final XqlFileType INSTANCE = new XqlFileType();

    private XqlFileType() {
        super(SqlLanguage.INSTANCE);
    }

    @Override
    public @NonNls @NotNull String getName() {
        return "XQL";
    }

    @Override
    public @NotNull String getDescription() {
        return "An enhanced version of sql file for XQL File Manager.";
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return "xql";
    }

    @Override
    public Icon getIcon() {
        return XqlIcons.XQL_FILE;
    }
}
