package com.github.chengyuxing.plugin.rabbit.sql.language.psi;

import com.github.chengyuxing.plugin.rabbit.sql.language.RabbitScriptLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class RabbitScriptTokenType extends IElementType {
    public RabbitScriptTokenType(@NonNls @NotNull String debugName) {
        super(debugName, RabbitScriptLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return RabbitScriptTokenType.class.getSimpleName() + "." + super.toString();
    }
}
