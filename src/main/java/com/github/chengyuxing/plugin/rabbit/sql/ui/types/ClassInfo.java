package com.github.chengyuxing.plugin.rabbit.sql.ui.types;

public class ClassInfo {
    private final String packageName;
    private final String className;

    public ClassInfo(String packageName, String className) {
        this.packageName = packageName;
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }
}
