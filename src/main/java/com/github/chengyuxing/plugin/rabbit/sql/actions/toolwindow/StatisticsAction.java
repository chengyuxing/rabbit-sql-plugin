package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow;

import com.github.chengyuxing.plugin.rabbit.sql.ui.StatisticsDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StatisticsAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (Objects.isNull(e.getProject())) {
            return;
        }
        ApplicationManager.getApplication().invokeLater(() -> {
            var d = new StatisticsDialog(e.getProject());
            d.showAndGet();
        });
    }
}
