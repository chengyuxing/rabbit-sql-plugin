package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.fasterxml.jackson.jr.ob.JSON;
import com.github.chengyuxing.common.script.Comparators;
import com.github.chengyuxing.common.utils.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.common.DatasourceManager;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.ParametersForm;
import com.github.chengyuxing.plugin.rabbit.sql.ui.renderer.IconListCellRenderer;
import com.github.chengyuxing.plugin.rabbit.sql.util.ExceptionUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.database.datagrid.DataRequest;
import com.intellij.database.view.ui.DataSourceManagerDialog;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.FixedSizeButton;
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
    private final DatasourceManager.Resource datasourceResource;
    private final XQLConfigManager.Config config;
    private final ParametersForm parametersForm;
    private final ComboBox<DatasourceManager.DatabaseId> datasourceList;

    public DynamicSqlCalcDialog(String sqlName, XQLConfigManager.Config config, DatasourceManager.Resource datasourceResource) {
        super(true);
        this.sqlName = sqlName;
        this.config = config;
        this.datasourceResource = datasourceResource;
        this.xqlFileManager = this.config.getXqlFileManager();
        this.sql = this.xqlFileManager.get(sqlName);
        this.paramsHistory = datasourceResource.getParamsHistory();
        var paramsMapping = com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil.getParamsMappingInfo(this.config.getSqlGenerator(), sql);
        this.parametersForm = new ParametersForm(paramsMapping, paramsHistory);
        this.parametersForm.setClickEmptyTableTextLink(this::doHelpAction);
        this.datasourceList = new ComboBox<>();
        setTitle("Parameters");
        setOKButtonText("Execute");
        setCancelButtonText("Close");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return parametersForm;
    }

    @Override
    protected @Nullable JPanel createSouthAdditionalPanel() {
        var panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
        datasourceList.addItem(DatasourceManager.DatabaseId.empty("<Configured database>"));
        if (datasourceResource != null) {
            var dsInfo = datasourceResource.getConfiguredDatabases();
            datasourceList.setRenderer(new IconListCellRenderer(dsInfo));
            dsInfo.forEach((k, v) -> datasourceList.addItem(k));
            datasourceList.setSwingPopup(false);
            var selected = datasourceResource.getSelected();
            if (selected != null) {
                if (dsInfo.containsKey(selected)) {
                    datasourceList.setSelectedItem(selected);
                }
            }
            panel.add(datasourceList);
        }
        panel.add(createOpenDatabaseButton());
        return panel;
    }

    @Override
    protected void doHelpAction() {
        parametersForm.setSqlHtml(HtmlUtil.highlightSql(sql));
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
        helpButton.setToolTipText("Show raw sql");
    }

    @Override
    protected void doOKAction() {
        var data = parametersForm.getData();
        if (data.getItem2().isEmpty()) {
            try {
                // named parameter sql
                // select ... from tb where id = :id and ${temp}
                var args = parseArgs2raw(data.getItem1());
                var result = xqlFileManager.get(sqlName, args);
                var finalSql = result.getItem1();
                var forVars = result.getItem2();
                // generate raw sql.
                args.put(XQLFileManager.DynamicSqlParser.FOR_VARS_KEY, forVars);
                var rawSql = config.getSqlGenerator()
                        .generateSql(finalSql, args);
                // execute sql
                var idx = datasourceList.getSelectedIndex();
                if (datasourceResource != null) {
                    if (idx > 0) {
                        var db = datasourceList.getItemAt(idx);
                        var console = datasourceResource.getConsole(db);
                        if (console != null) {
                            datasourceResource.setSelected(db);
                            var request = new ExecuteRequest(console, rawSql, DataRequest.newConstraints(), null);
                            console.getMessageBus().getDataProducer().processRequest(request);
                            dispose();
                            return;
                        }
                    }
                    datasourceResource.setSelected(null);
                }
                parametersForm.setSqlHtml(HtmlUtil.highlightSql(rawSql));
                autoHeight(rawSql);
            } catch (Exception e) {
                var errors = ExceptionUtil.getCauseMessages(e);
                var msg = String.join(NEW_LINE, errors);
                parametersForm.setSqlHtml(HtmlUtil.pre(msg, HtmlUtil.Color.DANGER));
                autoHeight(msg);
            }
            return;
        }
        // show error messages
        String msg = String.join(NEW_LINE, data.getItem2());
        parametersForm.setSqlHtml(HtmlUtil.pre(msg, HtmlUtil.Color.DANGER));
        autoHeight(msg);
    }

    private Map<String, Object> parseArgs2raw(Map<String, ?> args) {
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

    private JButton createOpenDatabaseButton() {
        var btn = new FixedSizeButton(30);
        btn.setToolTipText("Configure database");
        btn.setIcon(AllIcons.Actions.AddMulticaret);
        btn.addActionListener(e -> {
            dispose();
            DataSourceManagerDialog.showDialog(config.getProject(), null, null);
        });
        return btn;
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
