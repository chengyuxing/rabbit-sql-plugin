package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.plugin.rabbit.sql.file.XqlIcons;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SqlNameReference extends XqlNameReference {
    private final String alias;

    public SqlNameReference(@NotNull PsiElement element, TextRange rangeInElement, String key) {
        super(element, rangeInElement, key);
        var refPart = StringUtil.extraSqlReference(key);
        this.alias = refPart.getItem1();
    }

    @Override
    public Object @NotNull [] getVariants() {
        if (Objects.isNull(config)) {
            return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
        }
        var resource = config.getXqlFileManager().getResource(alias);
        if (Objects.isNull(resource)) {
            return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
        }
        var filename = FileResource.getFileName(resource.getFilename(), true);
        var icon = ProjectFileUtil.isLocalFileUri(resource.getFilename())
                ? XqlIcons.XQL_FILE_ITEM
                : XqlIcons.XQL_FILE_ITEM_REMOTE;
        return resource.getEntry()
                .keySet()
                .stream()
                .map(sqlName -> LookupElementBuilder.create(sqlName)
                        .withIcon(icon)
                        .withTypeText(filename)
                        .withTailText(" " + resource.getDescription())
                        .withCaseSensitivity(true)
                ).toArray();
    }
}
