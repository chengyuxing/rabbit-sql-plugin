package com.github.chengyuxing.plugin.rabbit.sql.plugins;

import java.util.Map;

public class FeatureChecker {
    //public static final String YML_PLUGIN_ID = "org.jetbrains.plugins.yaml";
    public static final String DATABASE_PLUGIN_ID = "com.intellij.database";
    public static final String KOTLIN_PLUGIN_ID = "org.jetbrains.kotlin";
    public static final String JAVA_PLUGIN_ID = "com.intellij.java";

    static final Map<String, String> pluginClass = Map.of(
            DATABASE_PLUGIN_ID, "com.intellij.database.psi.DbElement",
            KOTLIN_PLUGIN_ID, "org.jetbrains.kotlin.psi.KtElement",
            JAVA_PLUGIN_ID, "com.intellij.psi.PsiJavaToken"
    );

    public static boolean isPluginEnabled(String pluginId) {
        try {
            Class.forName(pluginClass.get(pluginId));
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
