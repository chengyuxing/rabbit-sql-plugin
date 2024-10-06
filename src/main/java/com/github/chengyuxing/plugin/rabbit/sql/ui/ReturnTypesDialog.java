package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.github.chengyuxing.plugin.rabbit.sql.ui.components.ReturnTypesForm;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.function.Consumer;

public class ReturnTypesDialog extends DialogWrapper {
    private final ReturnTypesForm returnTypesForm;
    private final Consumer<String> doOkAction;

    public ReturnTypesDialog(@Nullable Project project, String method, String selected, Consumer<String> doOkAction) {
        super(project, true);
        this.returnTypesForm = new ReturnTypesForm(selected, checked -> setOKActionEnabled(checked != 0));
        this.doOkAction = doOkAction;
        setTitle("[ " + method + " ] return types");
        setSize(360, 130);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return returnTypesForm;
    }

    @Override
    protected void doOKAction() {
        if (doOkAction != null) {
            doOkAction.accept(returnTypesForm.getSelected());
        }
        dispose();
    }
}
