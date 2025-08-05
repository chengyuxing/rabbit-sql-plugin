package com.github.chengyuxing.plugin.rabbit.sql.plugins;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;

public class FeatureChecker {
    public static final String JAVA_PLUGIN_ID = "com.intellij.java";

    public static boolean isPluginEnabled(String pluginId) {
        var id = PluginId.getId(pluginId);
        return !PluginManagerCore.isDisabled(id);
    }
}
