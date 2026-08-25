package com.github.chengyuxing.plugin.rabbit.sql;

import com.intellij.openapi.help.WebHelpProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Helper extends WebHelpProvider {
    public static final String XQL_FILE_MANAGER = "com.github.chengyuxing.rabbit-sql-plugin.xql-file-manager";
    public static final String XQL_FILE_MANAGER_BAKI_DAO = "com.github.chengyuxing.rabbit-sql-plugin.xql-file-manager.baki-dao";
    public static final String XQL_FILE_MANAGER_PIPE = "com.github.chengyuxing.rabbit-sql-plugin.xql-file-manager.pipe";
    public static final String SPRING_INTERFACE_MAPPER_USAGE = "com.github.chengyuxing.rabbit-sql-plugin.xql-mapper-usage";
    public static final String DOMAIN = "domain";

    @Override
    public @Nullable String getHelpPageUrl(@NotNull String helpTopicId) {
        return switch (helpTopicId) {
            case XQL_FILE_MANAGER -> "https://github.com/chengyuxing/rabbit-sql#xqlfilemanager-1";
            case XQL_FILE_MANAGER_BAKI_DAO -> "https://github.com/chengyuxing/rabbit-sql#bakidao";
            case XQL_FILE_MANAGER_PIPE -> "https://github.com/chengyuxing/rabbit-sql#pipe";
            case SPRING_INTERFACE_MAPPER_USAGE ->
                    "https://github.com/chengyuxing/rabbit-sql-spring-boot-starter#simple-usage";
            case DOMAIN -> "https://rabbit-sql.com";
            default -> null;
        };
    }
}
