package com.github.chengyuxing.plugin.rabbit.sql.ui.datasource.components;

import com.intellij.openapi.project.Project;

import javax.swing.*;

public class DataViewPanel extends JPanel {
    private final Project myProject;

    public DataViewPanel(Project myProject) {
        super(false);
        this.myProject = myProject;
    }
}
