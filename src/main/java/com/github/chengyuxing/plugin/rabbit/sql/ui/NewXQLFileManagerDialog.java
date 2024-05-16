package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.plugin.rabbit.sql.Helper;
import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.NewXQLFileManagerForm;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class NewXQLFileManagerDialog extends DialogWrapper {
    private final Project project;
    private final Path module;
    private final NewXQLFileManagerForm newXQLFileManagerForm;

    public NewXQLFileManagerDialog(Project project, Path module) {
        super(true);
        this.project = project;
        this.module = module;
        this.newXQLFileManagerForm = new NewXQLFileManagerForm();
        this.newXQLFileManagerForm.setInputChanged(s -> setOKActionEnabled(!s.trim().isEmpty()));
        setOKActionEnabled(false);
        setSize(370, 55);
        setTitle("New XQL File Manager");
        init();
    }

    @Override
    protected @NonNls @Nullable String getHelpId() {
        return Helper.XQL_FILE_MANAGER_BAKI_DAO;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return newXQLFileManagerForm;
    }

    @Override
    protected void doOKAction() {
        var name = newXQLFileManagerForm.getSecondaryFileName();
        var configName = FileResource.getFileName(Constants.CONFIG_NAME, false);
        var secondaryFilename = configName + "-" + name + ".yml";
        var secondaryAbsFilename = module.resolve(Constants.RESOURCE_ROOT).resolve(secondaryFilename);
        if (Files.exists(secondaryAbsFilename)) {
            newXQLFileManagerForm.setMessage(HtmlUtil.toHtml(HtmlUtil.span("'" + secondaryFilename + "' already exists.", HtmlUtil.Color.WARNING)));
            return;
        }
        ProjectFileUtil.createXqlConfigByTemplate(project, secondaryAbsFilename, () -> {
            dispose();
            ApplicationManager.getApplication().runWriteAction(() ->
                    ProjectFileUtil.openFile(project, secondaryAbsFilename, true));
        });
    }
}
