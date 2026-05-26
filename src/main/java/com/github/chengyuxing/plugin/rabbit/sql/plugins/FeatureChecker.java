package com.github.chengyuxing.plugin.rabbit.sql.plugins;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;

public class FeatureChecker {
    //public static final String YML_PLUGIN_ID = "org.jetbrains.plugins.yaml";
    public static final String DATABASE_PLUGIN_ID = "com.intellij.database";
    public static final String KOTLIN_PLUGIN_ID = "org.jetbrains.kotlin";
    public static final String JAVA_PLUGIN_ID = "com.intellij.java";

    public static boolean isPluginEnabled(String pluginId) {
        var id = PluginId.getId(pluginId);
        var plugin = PluginManager.getInstance().findEnabledPlugin(id);
        return plugin != null;
    }
}
