package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.StatisticsForm;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class StatisticsDialog extends DialogWrapper {
    private final StatisticsForm statisticsForm;

    public StatisticsDialog(@Nullable Project project) {
        super(true);
        XQLConfigManager xqlConfigManager = XQLConfigManager.getInstance();
        this.statisticsForm = new StatisticsForm(xqlConfigManager.getConfigMap(project));
        setTitle("Statistics");
        setSize(600, 300);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return statisticsForm;
    }
}
