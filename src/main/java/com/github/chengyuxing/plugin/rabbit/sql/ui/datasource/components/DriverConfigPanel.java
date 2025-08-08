package com.github.chengyuxing.plugin.rabbit.sql.ui.datasource.components;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.tabs.impl.JBEditorTabs;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class DriverConfigPanel extends JPanel {
    private final Project project;
    private final Disposable disposable;
    private JBEditorTabs tabs;

    public DriverConfigPanel(Project project, Disposable disposable) {
        this.project = project;
        this.disposable = disposable;
        initComponents();
    }

    private void initComponents() {
        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new MigLayout(
                "insets 0,hidemode 3",
                // columns
                "[grow 1,fill]",
                // rows
                "[grow 1,fill]"));
        tabs = new JBEditorTabs(project, IdeFocusManager.getInstance(project), disposable);

        add(tabs, "cell 0 0,grow");

    }
}
