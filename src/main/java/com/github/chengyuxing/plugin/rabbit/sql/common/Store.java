package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.exceptions.DuplicateException;
import com.intellij.openapi.diagnostic.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public enum Store {
    INSTANCE;
    private static final Logger log = Logger.getInstance(Store.class);
    public final Set<Path> projectJavas = new HashSet<>();

    public void refreshJavaFiles() {
        projectJavas.removeIf(java -> !Files.exists(java));
    }

    /**
     * xql file manager to resolve and cache sql.
     */
    public final XQLFileManager xqlFileManager = new XQLFileManager();

    public final void reloadXqlFiles(BiConsumer<Boolean, String> initialized) {
        try {
            xqlFileManager.init();
            initialized.accept(true, null);
        } catch (Exception e) {
            initialized.accept(false, e.getMessage());
            log.warn(e);
        }
    }
}
