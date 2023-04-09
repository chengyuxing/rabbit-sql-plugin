package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.openapi.diagnostic.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

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
    // TODO: 2023/4/9 add a XQL File Manager Control Panel for custom settings.
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
