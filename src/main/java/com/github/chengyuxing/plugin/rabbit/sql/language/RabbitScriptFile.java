package com.github.chengyuxing.plugin.rabbit.sql.language;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class RabbitScriptFile extends PsiFileBase {
    protected RabbitScriptFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, RabbitScriptLanguage.INSTANCE);
    }

    @Override
    public @NotNull FileType getFileType() {
        return RabbitScriptFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Rabbit Script File";
    }
}
