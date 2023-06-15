package com.github.chengyuxing.plugin.rabbit.sql.language.psi;

import com.github.chengyuxing.plugin.rabbit.sql.language.XqlScriptLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class XqlScriptTokenType extends IElementType {
    public XqlScriptTokenType(@NonNls @NotNull String debugName) {
        super(debugName, XqlScriptLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "XqlScriptTokenType." + super.toString();
    }
}
