package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.fasterxml.jackson.jr.ob.JSON;
import com.github.chengyuxing.common.script.Comparators;
import com.github.chengyuxing.common.utils.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.common.DatasourceCache;
import com.github.chengyuxing.plugin.rabbit.sql.common.ResourceCache;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.IconListCellRenderer;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.ParametersForm;
import com.github.chengyuxing.plugin.rabbit.sql.util.ExceptionUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.database.datagrid.DataRequest;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.github.chengyuxing.common.utils.StringUtil.NEW_LINE;

public class DynamicSqlCalcDialog extends DialogWrapper {
    private final String sqlName;
    private final String sql;
    private final Map<String, Object> paramsHistory;
    private final XQLFileManager xqlFileManager;
    private final DatasourceCache.Resource datasourceResource;
    private final ResourceCache.Resource resource;
    private final ParametersForm parametersForm;
    private final ComboBox<DatasourceCache.DatabaseId> datasourceList;

    public DynamicSqlCalcDialog(String sqlName, ResourceCache.Resource resource, DatasourceCache.Resource datasourceResource) {
        super(true);
        this.sqlName = sqlName;
        this.resource = resource;
        this.datasourceResource = datasourceResource;
        this.xqlFileManager = this.resource.getXqlFileManager();
        this.sql = this.xqlFileManager.get(sqlName);
        this.paramsHistory = datasourceResource.getParamsHistory();
        var paramsMapping = com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil.getParamsMappingInfo(this.resource.getSqlGenerator(), sql);
        this.parametersForm = new ParametersForm(paramsMapping, paramsHistory);
        this.datasourceList = new ComboBox<>();
        setTitle("Parameters");
        createDefaultActions();
        init();
    }

    @Override
    protected @NotNull Action getOKAction() {
        var a = super.getOKAction();
        a.putValue("Name", "Execute");
        return a;
    }

    @Override
    protected @NotNull Action getCancelAction() {
        var a = super.getCancelAction();
        a.putValue("Name", "Close");
        return a;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return parametersForm;
    }

    @Override
    protected @Nullable JPanel createSouthAdditionalPanel() {
        var panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
        datasourceList.addItem(DatasourceCache.DatabaseId.empty("<Configured database>"));
        if (datasourceResource != null) {
            var dsInfo = datasourceResource.getConfiguredDatabases();
            datasourceList.setRenderer(new IconListCellRenderer(dsInfo));
            dsInfo.forEach((k, v) -> datasourceList.addItem(k));
            datasourceList.setSwingPopup(false);
            panel.add(datasourceList);
        }
        return panel;
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
                try {
                    cache.put(k, JSON.std.asString(v));
                } catch (IOException ignore) {
                }
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
                // named parameter sql
                // select ... from tb where id = :id and ${temp}
                var finalSql = xqlFileManager.get(sqlName, data.getItem1(), false);
                // generate raw sql.
                var args = parseArgs2Raw(data.getItem1());
                var rawSql = resource.getSqlGenerator()
                        .generateSql(finalSql, args, false)
                        .getItem1();
                // execute sql
                var idx = datasourceList.getSelectedIndex();
                if (datasourceResource != null && idx > 0) {
                    var db = datasourceList.getItemAt(idx);
                    var console = datasourceResource.getConsole(db);
                    if (console != null) {
                        var request = new ExecuteRequest(console, rawSql, DataRequest.newConstraints(), null);
                        console.getMessageBus().getDataProducer().processRequest(request);
                        dispose();
                        return;
                    }
                }
                parametersForm.setSqlHtml(HtmlUtil.toHighlightSqlHtml(rawSql));
                autoHeight(rawSql);
            } catch (Exception e) {
                var errors = ExceptionUtil.getCauseMessages(e);
                var msg = String.join(NEW_LINE, errors);
                parametersForm.setSqlHtml(HtmlUtil.toHtml(msg, HtmlUtil.Color.DANGER));
                autoHeight(msg);
            }
            return;
        }
        // show error messages
        String msg = String.join(NEW_LINE, data.getItem2());
        parametersForm.setSqlHtml(HtmlUtil.toHtml(msg, HtmlUtil.Color.DANGER));
        autoHeight(msg);
    }

    private Map<String, ?> parseArgs2Raw(Map<String, ?> args) {
        var cache = new HashMap<String, Object>();
        args.forEach((k, v) -> {
            if (v == Comparators.ValueType.BLANK || v == Comparators.ValueType.NULL) {
                cache.put(k, null);
            } else if (v == Comparators.ValueType.TRUE) {
                cache.put(k, true);
            } else if (v == Comparators.ValueType.FALSE) {
                cache.put(k, false);
            } else if (v instanceof String) {
                cache.put(k, Comparators.getString(v));
            } else {
                cache.put(k, v);
            }
        });
        return cache;
    }

    private void autoHeight(String content) {
        var defaultSize = getPreferredSize();
        var basicHeight = defaultSize.height;
        var maxContentHeight = 239;
        var minContentHeight = 100;
        var lineCount = StringUtil.countOfContains(content, NEW_LINE);
        var contentHeight = lineCount * 21 + 39;
        contentHeight = Math.max(minContentHeight, contentHeight);
        contentHeight = Math.min(contentHeight, maxContentHeight);
        var height = basicHeight + contentHeight;
        var width = defaultSize.width;
        var userSize = getSize();
        var userHeight = Math.max(height, userSize.getHeight());
        var userWidth = Math.max(width, userSize.getWidth());
        setSize((int) userWidth, (int) userHeight);
    }

    public static class ExecuteRequest extends DataRequest.QueryRequest {
        protected ExecuteRequest(@NotNull Owner owner, @NotNull String query, @NotNull Constraints constraints, @Nullable Object params) {
            super(owner, query, constraints, params);
        }
    }
}
