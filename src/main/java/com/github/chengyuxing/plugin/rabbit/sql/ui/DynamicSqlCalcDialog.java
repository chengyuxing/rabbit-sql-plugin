package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.fasterxml.jackson.jr.ob.JSON;
import com.github.chengyuxing.common.script.expression.Comparators;
import com.github.chengyuxing.common.utils.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.database.DatasourceManager;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.FeatureChecker;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.database.DatabaseUtil;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.ParametersForm;
import com.github.chengyuxing.plugin.rabbit.sql.ui.renderer.IconListCellRenderer;
import com.github.chengyuxing.plugin.rabbit.sql.util.ExceptionUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.utils.SqlUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.FixedSizeButton;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;

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
    private final boolean isDatabasePluginEnabled;

    public DynamicSqlCalcDialog(String sqlName, XQLConfigManager.Config config, Project project) {
        super(true);
        this.isDatabasePluginEnabled = FeatureChecker.isPluginEnabled(FeatureChecker.DATABASE_PLUGIN_ID);
        this.sqlName = sqlName;
        this.config = config;
        this.datasourceResource = DatasourceManager.getInstance().getResource(project);
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
        datasourceList.setSwingPopup(false);
        if (!isDatabasePluginEnabled) {
            datasourceList.addItem(DatasourceManager.DatabaseId.empty("<Configured database>"));
            datasourceList.setEnabled(false);
        } else if (datasourceResource != null) {
            loadDatasourceList();
        }
        panel.add(datasourceList);
        panel.add(createOpenDatabaseButton());
        return panel;
    }

    private void loadDatasourceList() {
        var dsInfo = datasourceResource.getConfiguredDatabases();
        datasourceList.removeAllItems();
        datasourceList.addItem(DatasourceManager.DatabaseId.empty("<Configured database>"));
        datasourceList.setRenderer(new IconListCellRenderer(dsInfo));
        dsInfo.forEach((k, v) -> datasourceList.addItem(k));
        var selected = datasourceResource.getSelected();
        if (selected != null) {
            if (dsInfo.containsKey(selected)) {
                datasourceList.setSelectedItem(selected);
            }
        }
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
                rawSql = SqlUtil.repairSyntaxError(rawSql);
                // execute sql
                var idx = datasourceList.getSelectedIndex();
                if (datasourceResource != null && isDatabasePluginEnabled) {
                    if (idx > 0) {
                        var db = datasourceList.getItemAt(idx);
                        var executed = DatabaseUtil.executeSQL(rawSql, datasourceResource, db);
                        if (executed) {
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
        var btn = new FixedSizeButton();
        btn.setIcon(AllIcons.Actions.AddMulticaret);
        if (isDatabasePluginEnabled) {
            btn.setToolTipText("Configure database");
            btn.addActionListener(e -> {
                if (btn.getIcon() == AllIcons.Actions.AddMulticaret) {
                    DatabaseUtil.openDatasourceDialog(config.getProject());
                    btn.setIcon(AllIcons.Actions.Refresh);
                    btn.setToolTipText("Refresh database");
                    return;
                }
                if (btn.getIcon() == AllIcons.Actions.Refresh) {
                    btn.setIcon(AllIcons.Actions.AddMulticaret);
                    btn.setToolTipText("Configure database");
                    loadDatasourceList();
                }
            });
        } else {
            btn.setEnabled(false);
            btn.setToolTipText("Database Tool and SQL plugin is not enabled. Execute SQL features are disabled.");
        }
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
}
