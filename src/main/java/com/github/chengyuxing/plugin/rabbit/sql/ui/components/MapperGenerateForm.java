package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.github.chengyuxing.common.util.ValueUtils;
import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.ui.renderer.CheckboxCellRenderer;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XQLJavaType;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLMapperConfig;
import com.github.chengyuxing.plugin.rabbit.sql.ui.ReturnTypesDialog;
import com.github.chengyuxing.plugin.rabbit.sql.ui.renderer.XQLMethodCellRenderer;
import com.github.chengyuxing.plugin.rabbit.sql.ui.renderer.SqlTypePlaceHolder;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.XQLInvocationHandler;
import com.github.chengyuxing.sql.annotation.SqlStatementType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.TabbedPaneWrapper;
import com.intellij.ui.components.*;
import com.intellij.ui.table.JBTable;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.*;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;

public class MapperGenerateForm extends JPanel {
    private final Project project;
    private final String alias;
    private final XQLFileManager xqlFileManager;
    private final XQLMapperConfig mapperConfig;

    private JBTable table;
    private JBCheckBox bakiCheckBox;
    private JBTextField bakiTextField;
    private JBTextField packageTextField;

    private JBTextField pageTextField;
    private JBTextField sizeTextField;

    private TabbedPaneWrapper tabs;

    private final Disposable disposable;

    private static final Object[] thead = MessageBundle.message("ui.mapperGenForm.fields").split(",");
    public static final List<String> RETURN_TYPES = List.of(
            XQLJavaType.List.toString(),
            XQLJavaType.Set.toString(),
            XQLJavaType.Stream.toString(),
            XQLJavaType.PagedResource.toString(),
            XQLJavaType.Optional.toString(),
            XQLJavaType.GenericT.getValue(),
            XQLJavaType.IPageable.getValue(),
            XQLJavaType.Integer.getValue(),
            XQLJavaType.Long.getValue(),
            XQLJavaType.Double.getValue());
    public static final List<String> SQL_TYPES = List.of(SqlStatementType.query.name(),
            SqlStatementType.insert.name(),
            SqlStatementType.update.name(),
            SqlStatementType.delete.name(),
            SqlStatementType.procedure.name(),
            SqlStatementType.function.name(),
            SqlStatementType.ddl.name(),
            SqlStatementType.plsql.name(),
            SqlStatementType.unset.name());
    public static final List<String> GENERIC_TYPES = List.of(XQLJavaType.Map.toString(), XQLJavaType.DataRow.toString());
    public static final List<String> PARAM_TYPES = List.of(XQLJavaType.Map.getValue(), XQLJavaType.MultiArgs.toString());

    public MapperGenerateForm(Project project, String alias, XQLFileManager xqlFileManager, XQLMapperConfig mapperConfig, Disposable disposable) {
        this.project = project;
        this.alias = alias;
        this.xqlFileManager = xqlFileManager;
        this.mapperConfig = mapperConfig;
        this.disposable = disposable;
        initComponents();
    }

    private void initComponents() {
        setPreferredSize(new Dimension(750, 300));
        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new MigLayout(
                "insets 0,hidemode 3",
                // columns
                "[grow 1,fill]",
                // rows
                "[grow 1,fill]"));

        tabs = new TabbedPaneWrapper(disposable);

        tabs.addTab(com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil.generateInterfaceMapperName(alias), AllIcons.Nodes.Interface, createMapperPanel(), "");
        tabs.addTab(MessageBundle.message("ui.mapperGenForm.tab1"), AllIcons.General.Settings, createSettingPanel(), "");
        tabs.addTab(MessageBundle.message("ui.mapperGenForm.tab2"), AllIcons.General.ShowInfos, createAboutPanel(), "");

        add(tabs.getComponent(), "cell 0 0,grow");
    }

    public void selectConfigTab() {
        tabs.setSelectedIndex(0);
        packageTextField.requestFocus();
    }

    public String getBaki() {
        if (bakiCheckBox.isSelected()) {
            return bakiTextField.getText().trim();
        }
        return null;
    }

    public void setBaki(String baki) {
        if (Objects.nonNull(baki)) {
            bakiCheckBox.setSelected(true);
            bakiTextField.setEnabled(true);
            bakiTextField.setText(baki);
        }
    }

    public String getPackage() {
        return packageTextField.getText().trim();
    }

    public void setPackage(String packageName) {
        if (Objects.nonNull(packageName)) {
            packageTextField.setText(packageName);
        }
    }

    public String getPageKey() {
        return pageTextField.getText().trim();
    }

    public void setPageKey(String pageKey) {
        if (Objects.nonNull(pageKey)) {
            pageTextField.setText(pageKey);
        }
    }

    public String getSizeKey() {
        return sizeTextField.getText().trim();
    }

    public void setSizeKey(String sizeKey) {
        if (Objects.nonNull(sizeKey)) {
            sizeTextField.setText(sizeKey);
        }
    }

    @SuppressWarnings("rawtypes")
    public Vector<Vector> getData() {
        return ((DefaultTableModel) table.getModel()).getDataVector();
    }

    private JPanel createMapperPanel() {
        var panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder());
        panel.setLayout(new MigLayout(
                "insets 8 0 0 0,hidemode 3",
                // columns
                "[grow 1,fill]",
                // rows
                "[grow 1,fill]"));

        table = new JBTable() {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == table.convertColumnIndexToView(6)) {
                    return Boolean.class;
                }
                return super.getColumnClass(column);
            }
        };
        table.setBorder(BorderFactory.createEmptyBorder());
        table.setShowVerticalLines(false);
        table.setRowHeight(30);
        table.setSelectionForeground(null);
        table.setSelectionBackground(null);
        table.setFillsViewportHeight(true);
        table.getEmptyText().setText(MessageBundle.message("ui.mapperGenForm.tab0.empty", alias));
        var tableScrollPane = new JBScrollPane();
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableScrollPane.setViewportView(table);
        initTable();
        panel.add(tableScrollPane);
        return panel;
    }

    private JPanel createSettingPanel() {
        var panel = new JPanel();
        panel.setLayout(new FormLayout(new ColumnSpec[]{
                new ColumnSpec(Sizes.dluX(32)),
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                new ColumnSpec(Sizes.dluX(75)),
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                new ColumnSpec(Sizes.dluX(32)),
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                new ColumnSpec(Sizes.dluX(75)),
                new ColumnSpec(ColumnSpec.FILL, Sizes.dluX(10), FormSpec.DEFAULT_GROW)
        }, new RowSpec[]{
                new RowSpec(Sizes.DLUY4),

                FormFactory.DEFAULT_ROWSPEC,
                new RowSpec(Sizes.DLUY1),
                FormFactory.MIN_ROWSPEC,

                new RowSpec(Sizes.DLUY4),

                FormFactory.DEFAULT_ROWSPEC,
                new RowSpec(Sizes.DLUY1),
                FormFactory.MIN_ROWSPEC,

                new RowSpec(Sizes.DLUY4),

                FormFactory.DEFAULT_ROWSPEC,
                new RowSpec(Sizes.DLUY1),
                FormFactory.MIN_ROWSPEC
        }));
        CellConstraints cc = new CellConstraints();

        bakiCheckBox = new JBCheckBox("Baki:");
        bakiTextField = new JBTextField();
        bakiTextField.setEnabled(false);
        bakiCheckBox.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bakiTextField.setEnabled(bakiCheckBox.isSelected());
            }
        });

        JBLabel packageLabel = new JBLabel(MessageBundle.message("ui.mapperGenForm.tab1.package"));
        packageTextField = new JBTextField();
        JBLabel pageLabel = new JBLabel(MessageBundle.message("ui.mapperGenForm.tab1.page"));
        pageTextField = new JBTextField("page");
        JBLabel sizeLabel = new JBLabel(MessageBundle.message("ui.mapperGenForm.tab1.size"));
        sizeTextField = new JBTextField("size");

        panel.add(bakiCheckBox, cc.xy(1, 2));
        panel.add(bakiTextField, cc.xy(3, 2));
        panel.add(new InlineHelpText(MessageBundle.message("ui.mapperGenForm.tab1.baki.description")), cc.xyw(3, 4, 6, CellConstraints.LEFT, CellConstraints.CENTER));

        panel.add(packageLabel, cc.xy(1, 6));
        panel.add(packageTextField, cc.xyw(3, 6, 5));
        panel.add(new InlineHelpText(MessageBundle.message("ui.mapperGenForm.tab1.package.description")), cc.xyw(3, 8, 6, CellConstraints.LEFT, CellConstraints.CENTER));

        panel.add(pageLabel, cc.xy(1, 10));
        panel.add(pageTextField, cc.xy(3, 10));
        panel.add(sizeLabel, cc.xy(5, 10));
        panel.add(sizeTextField, cc.xy(7, 10));
        panel.add(new InlineHelpText(MessageBundle.message("ui.mapperGenForm.tab1.pageSize.description")), cc.xyw(3, 12, 6, CellConstraints.LEFT, CellConstraints.CENTER));
        return panel;
    }

    private JPanel createAboutPanel() {
        var panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder());
        panel.setLayout(new MigLayout(
                "insets 8 0 0 0,hidemode 3",
                // columns
                "[grow 1,fill]",
                // rows
                "[grow 1,fill]"));

        var contentPane = new JEditorPane();
        contentPane.setContentType("text/html");
        // language=html
        var html = """
                <html lang="en">
                <header>
                <style>
                body{
                font-family: sans-serif;
                }
                h1{
                text-align: center;
                }
                p{
                margin-bottom: 4px;
                margin-top: 4px;
                }
                </style>
                </header>
                <body>
                ${about}
                </body>
                </html>
                """;
        var exampleSql = HtmlUtil.highlightSql("""
                /*[queryUsers]*/
                select * from user where id = :id;
                /*[queryUsersCount]*/
                select count(*) from user where id = :id;
                """);
        var content = com.github.chengyuxing.common.util.StringUtils.FMT.format(html,
                Map.of("about", MessageBundle.message("ui.mapperGenForm.tab2.about"),
                        "exampleSql", exampleSql));
        contentPane.setText(content);

        var contentScrollPane = new JBScrollPane();
        contentScrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentScrollPane.setViewportView(contentPane);

        panel.add(contentScrollPane);

        return panel;
    }

    private void initTable() {
        var model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0 && column != 1 && column != 4;
            }
        };
        table.setModel(model);

        var tbody = xqlFileManager.getResource(alias).getEntry()
                .keySet()
                .stream()
                .filter(key -> !key.startsWith("${"))
                .map(sqlName -> {
                    var methodName = com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil.camelizeAndClean(sqlName);
                    var sqlType = SqlStatementType.query.name();
                    var returnType = XQLJavaType.List.toString();
                    if (methodName.matches(XQLInvocationHandler.INSERT_PATTERN)) {
                        sqlType = SqlStatementType.insert.name();
                        returnType = XQLJavaType.Integer.getValue();
                    } else if (methodName.matches(XQLInvocationHandler.UPDATE_PATTERN)) {
                        sqlType = SqlStatementType.update.name();
                        returnType = XQLJavaType.Integer.getValue();
                    } else if (methodName.matches(XQLInvocationHandler.DELETE_PATTERN)) {
                        sqlType = SqlStatementType.delete.name();
                        returnType = XQLJavaType.Integer.getValue();
                    } else if (methodName.matches(XQLInvocationHandler.CALL_PATTERN)) {
                        sqlType = SqlStatementType.procedure.name();
                        returnType = XQLJavaType.GenericT.getValue();
                    } else if (methodName.matches(XQLInvocationHandler.QUERY_PATTERN)) {
                        sqlType = SqlStatementType.query.name();
                        if (com.github.chengyuxing.common.util.StringUtils.startsWiths(methodName, "get", "query", "search", "select", "list")) {
                            returnType = XQLJavaType.List.toString();
                        } else {
                            returnType = XQLJavaType.GenericT.getValue();
                        }
                    }

                    var paramType = XQLJavaType.Map.getValue();
                    var returnGenericType = XQLJavaType.DataRow.getValue();
                    var enable = true;

                    var xqlMethod = this.mapperConfig.getMethods().get(sqlName);
                    if (Objects.nonNull(xqlMethod)) {
                        if (StringUtils.isNotEmpty(xqlMethod.getSqlType()) && SQL_TYPES.contains(xqlMethod.getSqlType())) {
                            sqlType = xqlMethod.getSqlType();
                        }
                        if (StringUtils.isNotEmpty(xqlMethod.getReturnType()) &&
                                new HashSet<>(RETURN_TYPES).containsAll(ReturnTypesForm.splitReturnTypes(xqlMethod.getReturnType()))) {
                            returnType = xqlMethod.getReturnType();
                        }

                        var paramMeta = xqlMethod.getParamMeta();
                        if (Objects.nonNull(paramMeta)) {
                            var className = paramMeta.getClassName();
                            if (!com.github.chengyuxing.common.util.StringUtils.isEmpty(className)) {
                                paramType = className;
                            }
                        }
                        if (StringUtils.isNotEmpty(xqlMethod.getParamType())) {
                            paramType = xqlMethod.getParamType();
                        }

                        if (StringUtils.isNotEmpty(xqlMethod.getReturnGenericType())) {
                            returnGenericType = xqlMethod.getReturnGenericType();
                        }
                        enable = ValueUtils.coalesce(xqlMethod.getEnable(), true);
                    }

                    return new Object[]{
                            sqlName,
                            methodName,
                            sqlType,
                            paramType,
                            returnType,
                            returnGenericType,
                            enable
                    };
                }).toArray(i -> new Object[i][6]);
        model.setDataVector(tbody, thead);
        table.getColumnModel().getColumn(0).setCellRenderer(new XQLMethodCellRenderer(xqlFileManager.getResource(alias), HtmlUtil.Color.HIGHLIGHT.getCode()));
        table.getColumnModel().getColumn(1).setCellRenderer(new XQLMethodCellRenderer(xqlFileManager.getResource(alias), HtmlUtil.Color.FUNCTION.getCode()));
        table.getColumnModel().getColumn(2).setCellEditor(buildSelector(false, SQL_TYPES));
        table.getColumnModel().getColumn(2).setCellRenderer(new SqlTypePlaceHolder());
        table.getColumnModel().getColumn(3).setCellEditor(buildSelector(true, PARAM_TYPES));
        table.getColumnModel().getColumn(5).setCellEditor(buildSelector(true, GENERIC_TYPES));
        table.getColumnModel().getColumn(6).setCellRenderer(new CheckboxCellRenderer());
        table.getColumnModel().getColumn(6).setMaxWidth(60);

        table.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    var x = table.rowAtPoint(e.getPoint());
                    var y = table.columnAtPoint(e.getPoint());
                    if (x >= 0 && y == table.convertColumnIndexToView(4)) {
                        var method = table.getValueAt(x, 1).toString();
                        var values = table.getValueAt(x, y).toString();
                        ApplicationManager.getApplication().invokeLater(() -> {
                            var queryTypesDialog = new ReturnTypesDialog(project, method, values, selected -> table.setValueAt(selected, x, y));
                            queryTypesDialog.showAndGet();
                        });
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                var x = table.rowAtPoint(e.getPoint());
                var y = table.columnAtPoint(e.getPoint());
                if (x >= 0 && y == table.convertColumnIndexToView(6)) {
                    var currentValue = (Boolean) table.getValueAt(x, y);
                    table.setValueAt(!currentValue, x, y);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

    private DefaultCellEditor buildSelector(boolean editable, List<String> items) {
        var cbx = new ComboBox<>();
        cbx.setEditable(editable);
        for (String item : items) {
            cbx.addItem(item);
        }
        return new DefaultCellEditor(cbx);
    }
}
