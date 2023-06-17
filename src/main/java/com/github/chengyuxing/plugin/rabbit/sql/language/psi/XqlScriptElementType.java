package com.github.chengyuxing.plugin.rabbit.sql.language.psi;

import com.github.chengyuxing.plugin.rabbit.sql.language.XqlScriptLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class XqlScriptElementType extends IElementType {
    public XqlScriptElementType(@NonNls @NotNull String debugName) {
        super(debugName, XqlScriptLanguage.INSTANCE);
    }
}
