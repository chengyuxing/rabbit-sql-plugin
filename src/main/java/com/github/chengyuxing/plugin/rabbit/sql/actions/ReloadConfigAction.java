package com.github.chengyuxing.plugin.rabbit.sql.actions;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.XqlFileManagerToolWindow;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.XqlFileManagerPanel;
import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ReloadConfigAction extends AnAction {
    private final XQLConfigManager xqlConfigManager = XQLConfigManager.getInstance();

    public ReloadConfigAction() {
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.nonNull(project)) {
            PsiUtil.saveUnsavedXqlAndConfig(project);
            xqlConfigManager.getConfigMap(project)
                    .forEach((module, configs) -> configs.forEach(config -> {
                        if (config.isValid()) {
                            config.fire();
                        }
                    }));
            xqlConfigManager.cleanup(project);
            XqlFileManagerToolWindow.getXqlFileManagerPanel(project, XqlFileManagerPanel::updateStates);
        }
    }
}
