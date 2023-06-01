package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.github.chengyuxing.common.script.Comparators;
import com.github.chengyuxing.common.utils.ReflectUtil;
import com.github.chengyuxing.common.utils.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.ParametersForm;
import com.github.chengyuxing.plugin.rabbit.sql.util.ExceptionUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DynamicSqlCalcDialog extends DialogWrapper {
    private final String sqlName;
    private final String sql;
    private final Map<String, Object> paramsHistory;
    private final XQLFileManager xqlFileManager;
    private final ParametersForm parametersForm;

    public DynamicSqlCalcDialog(String sqlName, Map<String, Object> paramsHistory, XQLFileManager xqlFileManager) {
        super(true);
        this.sqlName = sqlName;
        this.xqlFileManager = xqlFileManager;
        this.sql = this.xqlFileManager.get(sqlName);
        this.paramsHistory = paramsHistory;
        var paramsMapping = com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil.getParamsMappingInfo(this.xqlFileManager.getSqlTranslator(), sql);
        this.parametersForm = new ParametersForm(paramsMapping, paramsHistory);
        setTitle("Parameters");
        createDefaultActions();
        init();
        Optional.ofNullable(getButton(getOKAction())).ifPresent(a -> a.setText("Execute"));
        Optional.ofNullable(getButton(getCancelAction())).ifPresent(a -> a.setText("Close"));
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return parametersForm;
    }

    @Override
    protected void doHelpAction() {
        parametersForm.setSqlHtml(HtmlUtil.toHighlightSqlHtml(sql));
        autoHeight(sql);
    }

    @Override
    public void disposeIfNeeded() {
        var data = parametersForm.getData().getItem1();
        var cache = new HashMap<String, Object>();
        data.forEach((k, v) -> {
            if (v == Comparators.ValueType.BLANK) {
                cache.put(k, "");
            } else if (v instanceof Collection || v instanceof Map) {
                cache.put(k, ReflectUtil.obj2Json(v));
            } else {
                cache.put(k, v.toString());
            }
        });
        paramsHistory.putAll(cache);
    }

    @Override
    protected @NonNls @Nullable String getHelpId() {
        return "help";
    }

    @Override
    protected void setHelpTooltip(@NotNull JButton helpButton) {
        helpButton.setToolTipText("Show raw sql.");
    }

    @Override
    protected void doOKAction() {
        var data = parametersForm.getData();
        if (data.getItem2().isEmpty()) {
            try {
                var finalSql = xqlFileManager.get(sqlName, data.getItem1(), false);
                parametersForm.setSqlHtml(HtmlUtil.toHighlightSqlHtml(finalSql));
                autoHeight(finalSql);
            } catch (Exception e) {
                var errors = ExceptionUtil.getCauseMessages(e);
                var msg = String.join("\n", errors);
                parametersForm.setSqlHtml(HtmlUtil.toHtml(msg, HtmlUtil.Color.DANGER));
                autoHeight(msg);
            }
            return;
        }
        String msg = String.join("\n", data.getItem2());
        parametersForm.setSqlHtml(HtmlUtil.toHtml(msg, HtmlUtil.Color.DANGER));
        autoHeight(msg);
    }

    private void autoHeight(String content) {
        var defaultSize = getPreferredSize();
        var basicHeight = defaultSize.height;
        var maxContentHeight = 239;
        var minContentHeight = 100;
        var lineCount = StringUtil.countOfContains(content, "\n");
        var contentHeight = lineCount * 21 + 39;
        contentHeight = Math.max(minContentHeight, contentHeight);
        contentHeight = Math.min(contentHeight, maxContentHeight);
        setSize(defaultSize.width, basicHeight + contentHeight);
    }
}
