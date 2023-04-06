package com.github.chengyuxing.plugin.rabbit.sql.language.grammer;

import com.intellij.psi.tree.IElementType;
import com.intellij.sql.psi.SqlLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class XqlElementType extends IElementType {
    public XqlElementType(@NonNls @NotNull String debugName) {
        super(debugName, SqlLanguage.INSTANCE);
    }
}
