package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.fasterxml.jackson.jr.ob.JSON;
import com.github.chengyuxing.common.script.expression.Comparators;
import com.github.chengyuxing.common.utils.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.common.ResourceManager;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.database.DatabaseId;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.database.DatasourceManager;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.FeatureChecker;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.database.DatabaseUtil;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.ParametersForm;
import com.github.chengyuxing.plugin.rabbit.sql.ui.renderer.IconListCellRenderer;
import com.github.chengyuxing.plugin.rabbit.sql.util.AnActionWrapper;
import com.github.chengyuxing.plugin.rabbit.sql.util.ExceptionUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.utils.SqlUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.FixedSizeButton;
import com.intellij.openapi.ui.OptionAction;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;

import static com.github.chengyuxing.common.utils.StringUtil.NEW_LINE;

public class DynamicSqlCalcDialog extends DialogWrapper {
    private final Project project;
    private final String sqlName;
    private final String sql;
    private final XQLFileManager xqlFileManager;
    private final XQLConfigManager.Config config;
    private final Map<String, Object> paramsHistory;
    private final List<String> paramsList;
    private final ParametersForm parametersForm;
    private final ComboBox<DatabaseId> datasourceList;
    private final boolean isDatabasePluginEnabled;

    public DynamicSqlCalcDialog(String sqlName, XQLConfigManager.Config config, Project project) {
        super(true);
        this.project = project;
        this.isDatabasePluginEnabled = FeatureChecker.isPluginEnabled(FeatureChecker.DATABASE_PLUGIN_ID);
        this.sqlName = sqlName;
        this.config = config;
        this.xqlFileManager = this.config.getXqlFileManager();
        this.sql = this.xqlFileManager.get(sqlName);
        this.paramsHistory = ResourceManager.getInstance().getResource(project).getDynamicSqlParamHistory();
        this.paramsList = ResourceManager.getInstance().getResource(project).getHistoryList();
        var paramsMapping = com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil.getParamsMappingInfo(this.config.getSqlGenerator(), sql);
        this.parametersForm = new ParametersForm(paramsMapping, paramsHistory, paramsList);
        this.parametersForm.setClickEmptyTableTextLink(this::doHelpAction);
        this.datasourceList = new ComboBox<>();
        setTitle("Parameters");
        setOKButtonText("Execute");
        setCancelButtonText("Close");
        init();
    }

    @Override
    protected Action @NotNull [] createActions() {
        var group = new ActionGroup() {
            @Override
            public AnAction @NotNull [] getChildren(@Nullable AnActionEvent anActionEvent) {
                return new AnAction[]{
                        getPreviewAction(),
                        new Separator(),
                        getPrepareNamedParametersSQLAction(),
                        getPreparePositionalParametersAction(),
                };
            }
        };
        return new Action[]{
                new OptionAction() {
                    @Override
                    public Action @NotNull [] getOptions() {
                        return new Action[]{new AnActionWrapper(group)};
                    }

                    @Override
                    public Object getValue(String key) {
                        return getOKAction().getValue(key);
                    }

                    @Override
                    public void putValue(String key, Object value) {
                        getOKAction().putValue(key, value);
                    }

                    @Override
                    public void setEnabled(boolean b) {
                        setOKActionEnabled(b);
                    }

                    @Override
                    public boolean isEnabled() {
                        return getOKAction().isEnabled();
                    }

                    @Override
                    public void addPropertyChangeListener(PropertyChangeListener listener) {
                        getOKAction().addPropertyChangeListener(listener);
                    }

                    @Override
                    public void removePropertyChangeListener(PropertyChangeListener listener) {
                        getOKAction().removePropertyChangeListener(listener);
                    }

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        getOKAction().actionPerformed(e);
                    }
                },
                getCancelAction(),
                getHelpAction()
        };
    }

    private @NotNull AnAction getPreviewAction() {
        return new AnAction("Execute Test Preview", "Displays sql parsed for testing only.", AllIcons.Diff.MagicResolve) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                parseDynamicSQL((sql, args) -> {
                    var rawSql = config.getSqlGenerator().generateSql(sql, args);
                    rawSql = SqlUtil.repairSyntaxError(rawSql);
                    parametersForm.setSqlHtml(HtmlUtil.highlightSql(rawSql));
                    autoHeight(rawSql);
                });
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
    }

    private @NotNull AnAction getPreparePositionalParametersAction() {
        return new AnAction("Prepare Positional Parameters SQL", "Displays the actual prepared sql parsed for running in database.", AllIcons.Actions.Compile) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                parseDynamicSQL((sql, args) -> {
                    var preparedSQL = config.getSqlGenerator().generatePreparedSql(sql, args).getResultSql();
                    preparedSQL = SqlUtil.repairSyntaxError(preparedSQL);
                    parametersForm.setSqlHtml(HtmlUtil.highlightSql(preparedSQL));
                    autoHeight(preparedSQL);
                });
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
    }

    private @NotNull AnAction getPrepareNamedParametersSQLAction() {
        return new AnAction("Prepare Named Parameters SQL", "Displays the actual sql parsed for running in production.", AllIcons.Actions.Compile) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                parseDynamicSQL((sql, args) -> {
                    sql = SqlUtil.repairSyntaxError(sql);
                    parametersForm.setSqlHtml(HtmlUtil.highlightSql(sql));
                    autoHeight(sql);
                });
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
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
            datasourceList.addItem(DatabaseId.empty("<Configured database>"));
            datasourceList.setEnabled(false);
        } else {
            loadDatasourceList();
        }
        panel.add(datasourceList);
        panel.add(createOpenDatabaseButton());
        return panel;
    }

    private void loadDatasourceList() {
        var resource = DatasourceManager.getInstance().getResource(project);
        var databases = resource.getConfiguredDatabases();
        datasourceList.removeAllItems();
        datasourceList.addItem(DatabaseId.empty("<Configured database>"));
        datasourceList.setRenderer(new IconListCellRenderer(databases));
        databases.forEach((k, v) -> datasourceList.addItem(k));
        var selected = resource.getSelected();
        if (selected != null) {
            if (databases.containsKey(selected)) {
                datasourceList.setSelectedItem(selected);
            }
        }
    }

    private void addToParamList(Object v) {
        String item = null;
        if (v instanceof Collection || v instanceof Map) {
            try {
                item = JSON.std.asString(v);
            } catch (IOException ignore) {
            }
        } else if (Objects.nonNull(v) && !(v instanceof Comparators.ValueType)) {
            item = v.toString();
        }
        if (Objects.nonNull(item)) {
            paramsList.remove(item);
            paramsList.add(0, item);
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
        data.forEach((k, v) -> {
            if (v == Comparators.ValueType.BLANK) {
                paramsHistory.put(k, null);
            } else if (v instanceof Collection || v instanceof Map) {
                try {
                    var json = JSON.std.asString(v);
                    paramsHistory.put(k, json);
                } catch (IOException ignore) {
                }
            } else {
                paramsHistory.put(k, v);
            }
        });
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
        parseDynamicSQL((sql, args) -> {
            var rawSql = config.getSqlGenerator().generateSql(sql, args);
            rawSql = SqlUtil.repairSyntaxError(rawSql);
            // execute sql
            var idx = datasourceList.getSelectedIndex();
            if (isDatabasePluginEnabled) {
                var resource = DatasourceManager.getInstance().getResource(project);
                if (idx > 0) {
                    var db = datasourceList.getItemAt(idx);
                    var executed = DatabaseUtil.executeSQL(rawSql, resource, db);
                    if (executed) {
                        dispose();
                        return;
                    }
                }
                resource.setSelected(null);
            }
            parametersForm.setSqlHtml(HtmlUtil.highlightSql(rawSql));
            autoHeight(rawSql);
        });
    }

    private void parseDynamicSQL(BiConsumer<String, Map<String, Object>> then) {
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
                then.accept(finalSql, args);
                data.getItem1().forEach((k, v) -> addToParamList(v));
            } catch (Exception ex) {
                var errors = ExceptionUtil.getCauseMessages(ex);
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
            btn.setToolTipText("Database Tool and SQL plugin is not enabled.");
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
