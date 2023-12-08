package com.github.chengyuxing.plugin.rabbit.sql;

import com.intellij.codeInsight.template.TemplateActionContext;
import com.intellij.codeInsight.template.TemplateContextType;
import org.jetbrains.annotations.NotNull;

public class XqlContext extends TemplateContextType {
    protected XqlContext() {
        super("Rabbit SQL(.xql)");
    }

    @Override
    public boolean isInContext(@NotNull TemplateActionContext templateActionContext) {
        return templateActionContext.getFile().getName().endsWith(".xql");
    }
}
