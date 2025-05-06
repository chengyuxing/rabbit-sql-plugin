package com.github.chengyuxing.plugin.rabbit.sql.language.psi;

import com.github.chengyuxing.plugin.rabbit.sql.language.RabbitScriptLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class RabbitScriptElementType extends IElementType {
    public RabbitScriptElementType(@NonNls @NotNull String debugName) {
        super(debugName, RabbitScriptLanguage.INSTANCE);
    }
}
