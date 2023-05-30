package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.github.chengyuxing.plugin.rabbit.sql.ui.components.ParametersForm;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class DynamicSqlCalcDialog extends DialogWrapper {
    private final String sql;
    private final XQLFileManager xqlFileManager;
    private final ParametersForm parametersForm;

    public DynamicSqlCalcDialog(String sql, XQLFileManager xqlFileManager, List<String> parameterNames) {
        super(true);
        this.sql = sql;
        this.xqlFileManager = xqlFileManager;
        this.parametersForm = new ParametersForm(parameterNames);
        setTitle("Parameters");
        init();
    }

    public String getSql() {
        return sql;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return parametersForm;
    }

    @Override
    protected void doOKAction() {
        var data = parametersForm.getData();
        var finalSql = xqlFileManager.dynamicCalc(sql, data, false);
        parametersForm.setSqlHtml(HtmlUtil.toHtml(finalSql));
    }
}
