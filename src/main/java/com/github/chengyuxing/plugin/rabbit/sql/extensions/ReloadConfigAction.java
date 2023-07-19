package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.common.ResourceCache;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ReloadConfigAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.nonNull(project)) {
            FileDocumentManager.getInstance().saveAllDocuments();
            ResourceCache.getInstance().foreach((p, r) -> r.fire(true));
        }
    }
}
