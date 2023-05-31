package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.github.chengyuxing.plugin.rabbit.sql.ui.components.ParametersForm;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;
import java.util.Set;

public class DynamicSqlCalcDialog extends DialogWrapper {
    private final String sql;
    private final XQLFileManager xqlFileManager;
    private final ParametersForm parametersForm;

    public DynamicSqlCalcDialog(String sql, XQLFileManager xqlFileManager, Map<String, Set<String>> paramsMapping) {
        super(true);
        this.sql = sql;
        this.xqlFileManager = xqlFileManager;
        this.parametersForm = new ParametersForm(paramsMapping);
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
        if (parametersForm.getErrors().isEmpty()) {
            var finalSql = xqlFileManager.dynamicCalc(sql, data, false);
            parametersForm.setSqlHtml(HtmlUtil.toHighlightSqlHtml(finalSql));
            return;
        }
        String msg = String.join("\n", parametersForm.getErrors());
        parametersForm.setSqlHtml(HtmlUtil.toHtml(msg, HtmlUtil.Color.DANGER));
    }
}
