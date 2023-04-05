package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.github.chengyuxing.sql.XQLFileManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public enum Store {
    INSTANCE;
    public final Set<Path> projectJavas = new HashSet<>();

    public void refreshJavaFile() {
        projectJavas.removeIf(java -> !Files.exists(java));
    }

    /**
     * xql file manager to resolve and cache sql.
     */
    public final XQLFileManager xqlFileManager = new XQLFileManager();
}
