package com.github.chengyuxing.plugin.rabbit.sql.psi;

import com.intellij.sql.psi.SqlReference;
import com.intellij.sql.psi.SqlScopeProcessor;
import com.intellij.sql.psi.impl.SqlResolveExtension;
import org.jetbrains.annotations.NotNull;

public class DynamicSqlProvider implements SqlResolveExtension {
    @Override
    public boolean process(@NotNull SqlReference reference, @NotNull SqlScopeProcessor processor) {
        System.out.println(123);
        return SqlResolveExtension.super.process(reference, processor);
    }
}
