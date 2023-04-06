package com.github.chengyuxing.plugin.rabbit.sql.language.grammer;

import com.intellij.psi.tree.IElementType;
import com.intellij.sql.psi.SqlLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class XqlTokenType extends IElementType {
    public XqlTokenType(@NonNls @NotNull String debugName) {
        super(debugName, SqlLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "XqlTokenType." + super.toString();
    }
}
