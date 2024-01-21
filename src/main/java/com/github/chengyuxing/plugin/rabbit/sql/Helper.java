package com.github.chengyuxing.plugin.rabbit.sql;

import com.intellij.openapi.help.WebHelpProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Helper extends WebHelpProvider {
    public static final String XQL_FILE_MANAGER = "com.github.chengyuxing.rabbit-sql-plugin.xql-file-manager";
    @Override
    public @Nullable String getHelpPageUrl(@NotNull String helpTopicId) {
        return switch (helpTopicId) {
            case XQL_FILE_MANAGER -> "https://github.com/chengyuxing/rabbit-sql/blob/master/README.md#XQLFileManager";
            default -> null;
        };
    }
}
