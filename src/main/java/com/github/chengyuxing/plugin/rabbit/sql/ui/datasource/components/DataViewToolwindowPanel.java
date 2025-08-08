package com.github.chengyuxing.plugin.rabbit.sql.ui.datasource.components;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;

public class DataViewToolwindowPanel extends SimpleToolWindowPanel {
    private final Project myProject;

    public DataViewToolwindowPanel(Project myProject) {
        super(false);
        this.myProject = myProject;
    }
}
