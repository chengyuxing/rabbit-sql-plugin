package com.github.chengyuxing.plugin.rabbit.sql.plugins;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;

import java.util.Objects;

public class FeatureChecker {
    public static final String DATABASE_PLUGIN_ID = "com.intellij.database";
    public static final String KOTLIN_PLUGIN_ID = "org.jetbrains.kotlin";

    public static boolean isPluginEnabled(String pluginId) {
        var id = PluginId.getId(pluginId);
        var plugin = PluginManagerCore.getPlugin(id);
        return Objects.nonNull(plugin) && plugin.isEnabled();
    }
}
