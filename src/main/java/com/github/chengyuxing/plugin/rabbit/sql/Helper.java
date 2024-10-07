package com.github.chengyuxing.plugin.rabbit.sql;

import com.intellij.openapi.help.WebHelpProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Helper extends WebHelpProvider {
    public static final String XQL_FILE_MANAGER = "com.github.chengyuxing.rabbit-sql-plugin.xql-file-manager";
    public static final String XQL_FILE_MANAGER_BAKI_DAO = "com.github.chengyuxing.rabbit-sql-plugin.xql-file-manager.baki-dao";
    @Override
    public @Nullable String getHelpPageUrl(@NotNull String helpTopicId) {
        return switch (helpTopicId) {
            case XQL_FILE_MANAGER -> "https://github.com/chengyuxing/rabbit-sql?tab=readme-ov-file#xqlfilemanager";
            case XQL_FILE_MANAGER_BAKI_DAO -> "https://github.com/chengyuxing/rabbit-sql?tab=readme-ov-file#autoxfmconfig";
            default -> null;
        };
    }
}
