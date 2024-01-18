package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
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
            XQLConfigManager.getInstance().getConfigMap(project)
                    .forEach((module, configs) -> configs.forEach(config -> {
                        if (config.isValid() && config.isPrimary()) {
                            config.fire(true);
                        }
                    }));
        }
    }
}
