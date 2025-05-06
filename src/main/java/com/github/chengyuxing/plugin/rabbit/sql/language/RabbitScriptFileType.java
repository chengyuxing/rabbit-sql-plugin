package com.github.chengyuxing.plugin.rabbit.sql.language;

import com.github.chengyuxing.plugin.rabbit.sql.file.XqlIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RabbitScriptFileType extends LanguageFileType {
    public static final RabbitScriptFileType INSTANCE = new RabbitScriptFileType();

    protected RabbitScriptFileType() {
        super(RabbitScriptLanguage.INSTANCE);
    }

    @Override
    public @NonNls @NotNull String getName() {
        return "RabbitScript File";
    }

    @Override
    public @NlsContexts.Label @NotNull String getDescription() {
        return "RabbitScript language file";
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return "rbs";
    }

    @Override
    public Icon getIcon() {
        return XqlIcons.RABBIT_SCRIPT_FILE;
    }
}
