package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLMapperConfig;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.ReturnTypesForm;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.function.Consumer;

public class ReturnTypesDialog extends DialogWrapper {
    private final ReturnTypesForm returnTypesForm;
    private final Consumer<XQLMapperConfig.ReturnType> doOkAction;

    public ReturnTypesDialog(@Nullable Project project, String method, XQLMapperConfig.ReturnType selected, Consumer<XQLMapperConfig.ReturnType> doOkAction) {
        super(project, true);
        this.returnTypesForm = new ReturnTypesForm(selected, checked -> setOKActionEnabled(checked != 0));
        this.doOkAction = doOkAction;
        setTitle(MessageBundle.message("ui.dialog.returnType.title", method));
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
