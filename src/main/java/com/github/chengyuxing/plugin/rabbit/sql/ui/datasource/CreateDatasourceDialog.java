package com.github.chengyuxing.plugin.rabbit.sql.ui.datasource;

import com.github.chengyuxing.plugin.rabbit.sql.ui.datasource.components.DatasourceConfigPanel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CreateDatasourceDialog extends DialogWrapper {
    private final Project myProject;
    private final DatasourceConfigPanel datasourceConfigPanel;

    public CreateDatasourceDialog(@Nullable Project project) {
        super(project, true);
        myProject = project;
        datasourceConfigPanel = new DatasourceConfigPanel();
        setTitle("Configure Datasource");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return datasourceConfigPanel;
    }
}
