package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.fasterxml.jackson.jr.ob.JSON;
import com.github.chengyuxing.common.script.exception.CheckViolationException;
import com.github.chengyuxing.common.script.exception.GuardViolationException;
import com.github.chengyuxing.common.util.StringUtils;
import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.common.ResourceManager;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.database.DatabaseId;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.database.DatasourceManager;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.FeatureChecker;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.database.DatabaseUtil;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.ParametersForm;
import com.github.chengyuxing.plugin.rabbit.sql.ui.renderer.IconListCellRenderer;
import com.github.chengyuxing.plugin.rabbit.sql.util.AnActionGroupWrapper;
import com.github.chengyuxing.plugin.rabbit.sql.util.ExceptionUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.util.SqlUtils;
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

import static com.github.chengyuxing.common.util.StringUtils.NEW_LINE;

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
        this.paramsHistory = ResourceManager.getInstance(project).getResource().getDynamicSqlParamHistory();
        this.paramsList = ResourceManager.getInstance(project).getResource().getHistoryList();
        var paramsMapping = com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil.getParamsMappingInfo(this.config.getSqlGenerator(), sql);
        this.parametersForm = new ParametersForm(paramsMapping, paramsHistory, paramsList);
        this.parametersForm.setClickEmptyTableTextLink(this::doHelpAction);
        this.datasourceList = new ComboBox<>();
        setTitle(MessageBundle.message("ui.dialog.execute.title"));
        setOKButtonText(MessageBundle.message("ui.dialog.execute.ok"));
        setCancelButtonText(MessageBundle.message("ui.dialog.execute.cancel"));
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
                        return new Action[]{new AnActionGroupWrapper(group)};
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
        return new AnAction(MessageBundle.message("ui.dialog.execute.action.preview.text"), MessageBundle.message("ui.dialog.execute.action.preview.description"), AllIcons.Diff.MagicResolve) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                parseDynamicSQL((sql, args) -> {
                    var fullSql = SqlUtils.formatSqlTemplate(sql, args);
                    var rawSql = config.getSqlGenerator().generateSql(fullSql, args, v -> SqlUtils.toSqlLiteral(v, true));
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
        return new AnAction(MessageBundle.message("ui.dialog.execute.action.prepare.pos.text"), MessageBundle.message("ui.dialog.execute.action.prepare.pos.description"), AllIcons.Actions.Compile) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                parseDynamicSQL((sql, args) -> {
                    var preparedSQL = config.getSqlGenerator().generatePreparedSql(sql, args).getPrepareSql();
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
        return new AnAction(MessageBundle.message("ui.dialog.execute.action.prepare.named.text"), MessageBundle.message("ui.dialog.execute.action.prepare.named.description"), AllIcons.Actions.Compile) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                parseDynamicSQL((sql, args) -> {
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
            datasourceList.addItem(DatabaseId.empty(MessageBundle.message("ui.dialog.execute.datasource.placeholder")));
            datasourceList.setEnabled(false);
        } else {
            loadDatasourceList();
        }
        panel.add(datasourceList);
        panel.add(createOpenDatabaseButton());
        return panel;
    }

    private void loadDatasourceList() {
        var resource = DatasourceManager.getInstance(project).getResource();
        var databases = resource.getConfiguredDatabases();
        datasourceList.removeAllItems();
        datasourceList.addItem(DatabaseId.empty(MessageBundle.message("ui.dialog.execute.datasource.placeholder")));
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
        Object o = formatStringValue(v);
        if (Objects.nonNull(o)) {
            String item = o.toString();
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
        data.forEach((k, v) -> paramsHistory.put(k, formatStringValue(v)));
    }

    private Object formatStringValue(Object v) {
        if (v instanceof Collection || v instanceof Map || v instanceof Object[]) {
            try {
                return JSON.std.asString(v);
            } catch (IOException ignore) {
            }
        }
        if (v instanceof String && !v.equals("")) {
            return "'" + v + "'";
        }
        return v;
    }

    @Override
    protected @NonNls @Nullable String getHelpId() {
        return MessageBundle.message("ui.dialog.execute.help");
    }

    @Override
    protected void setHelpTooltip(@NotNull JButton helpButton) {
        helpButton.setToolTipText(MessageBundle.message("ui.dialog.execute.help.tooltip"));
    }

    @Override
    protected void doOKAction() {
        parseDynamicSQL((sql, args) -> {
            var fullSql = SqlUtils.formatSqlTemplate(sql, args);
            var rawSql = config.getSqlGenerator().generateSql(fullSql, args, v -> SqlUtils.toSqlLiteral(v, true));
            // execute sql
            var idx = datasourceList.getSelectedIndex();
            if (isDatabasePluginEnabled) {
                var resource = DatasourceManager.getInstance(project).getResource();
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
                var args = new HashMap<>(data.getItem1());
                var result = xqlFileManager.get(sqlName, args);
                var finalSql = result.getItem1();
                var vars = result.getItem2();
                // generate raw sql.
                args.putAll(vars);
                then.accept(finalSql, args);
                data.getItem1().forEach((k, v) -> addToParamList(v));
            } catch (CheckViolationException | GuardViolationException e) {
                var msg = e.getClass().getSimpleName() + ": " + e.getMessage();
                parametersForm.setSqlHtml(HtmlUtil.pre(msg, HtmlUtil.Color.DANGER));
                autoHeight(msg);
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

    private JButton createOpenDatabaseButton() {
        var btn = new FixedSizeButton();
        btn.setIcon(AllIcons.Actions.AddMulticaret);
        if (isDatabasePluginEnabled) {
            btn.setToolTipText(MessageBundle.message("ui.dialog.execute.datasource.action.configure"));
            btn.addActionListener(e -> {
                if (btn.getIcon() == AllIcons.Actions.AddMulticaret) {
                    DatabaseUtil.openDatasourceDialog(config.getProject(), this::dispose);
                    btn.setIcon(AllIcons.Actions.Refresh);
                    btn.setToolTipText(MessageBundle.message("ui.dialog.execute.datasource.action.refresh"));
                    return;
                }
                if (btn.getIcon() == AllIcons.Actions.Refresh) {
                    btn.setIcon(AllIcons.Actions.AddMulticaret);
                    btn.setToolTipText(MessageBundle.message("ui.dialog.execute.datasource.action.configure"));
                    loadDatasourceList();
                }
            });
        } else {
            btn.setEnabled(false);
            btn.setToolTipText(MessageBundle.message("ui.dialog.execute.datasource.action.disabled"));
        }
        return btn;
    }

    private void autoHeight(String content) {
        var defaultSize = getPreferredSize();
        var basicHeight = defaultSize.height;
        var maxContentHeight = 239;
        var minContentHeight = 100;
        var lineCount = StringUtils.countOccurrences(content, NEW_LINE);
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
