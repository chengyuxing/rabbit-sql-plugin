package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.github.chengyuxing.common.utils.ObjectUtil;
import com.github.chengyuxing.common.utils.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.ui.renderer.CheckboxCellRenderer;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XQLJavaType;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XQLMapperConfig;
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
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.components.*;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBEditorTabs;
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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

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

    private JBEditorTabs tabs;

    private final Disposable disposable;

    private static final Object[] thead = new Object[]{
            "SQL",
            "Method",
            "SQL Type",
            "Param Type",
            "Return Types",
            "<T>",
            "Enable"};
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
            SqlStatementType.plsql.name());
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

        tabs = new JBEditorTabs(project, IdeFocusManager.getInstance(project), disposable);

        var mapperPanel = createMapperPanel();
        var settingPanel = createSettingPanel();
        var aboutPanel = createAboutPanel();

        var tableInfo = new TabInfo(mapperPanel);
        tableInfo.setIcon(AllIcons.Nodes.Interface);
        tableInfo.setText(com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil.generateInterfaceMapperName(alias));
        tabs.addTab(tableInfo);

        var configInfo = new TabInfo(settingPanel);
        configInfo.setIcon(AllIcons.General.Settings);
        configInfo.setText("Configuration");
        tabs.addTab(configInfo);

        var aboutInfo = new TabInfo(aboutPanel);
        aboutInfo.setIcon(AllIcons.General.ShowInfos);
        aboutInfo.setText("About");
        tabs.addTab(aboutInfo);

        add(tabs, "cell 0 0,grow");
    }

    public void selectConfigTab() {
        var tab = tabs.getTabAt(1);
        tabs.select(tab, true);
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
        table.getEmptyText().setText("No SQLs in " + alias + ".");
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
                FormFactory.MIN_ROWSPEC,
                FormFactory.MIN_ROWSPEC,
                FormFactory.MIN_ROWSPEC,
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

        JBLabel packageLabel = new JBLabel("Package:");
        packageTextField = new JBTextField();
        JBLabel pageLabel = new JBLabel("Page key:");
        pageTextField = new JBTextField("page");
        JBLabel sizeLabel = new JBLabel("Size key:");
        sizeTextField = new JBTextField("size");

        panel.add(bakiCheckBox, cc.xy(1, 2));
        panel.add(bakiTextField, cc.xy(3, 2));
        panel.add(new InlineHelpText("Specify the name if there are multiple baki in the spring context."), cc.xyw(3, 4, 6, CellConstraints.LEFT, CellConstraints.CENTER));

        panel.add(packageLabel, cc.xy(1, 6));
        panel.add(packageTextField, cc.xyw(3, 6, 5));
        panel.add(new InlineHelpText("Where the mapper interface generated."), cc.xyw(3, 8, 6, CellConstraints.LEFT, CellConstraints.CENTER));

        panel.add(pageLabel, cc.xy(1, 10));
        panel.add(pageTextField, cc.xy(3, 10));
        panel.add(sizeLabel, cc.xy(5, 10));
        panel.add(sizeTextField, cc.xy(7, 10));
        panel.add(new InlineHelpText("The default page arg name of the PagedResource&lt;T&gt; return type,"), cc.xyw(3, 12, 6, CellConstraints.LEFT, CellConstraints.CENTER));
        panel.add(new InlineHelpText("it must be consistent with BakiDao's properties: <code>pageKey</code> and"), cc.xyw(3, 13, 6, CellConstraints.LEFT, CellConstraints.CENTER));
        panel.add(new InlineHelpText("<code>sizeKey</code> when the <b>Param Type</b> is <code>@Arg</code>."), cc.xyw(3, 14, 6, CellConstraints.LEFT, CellConstraints.CENTER));
        return panel;
    }

    private JPanel createAboutPanel() {
        var panel = new JPanel();
        panel.setLayout(new FormLayout(new ColumnSpec[]{
                new ColumnSpec(ColumnSpec.FILL, Sizes.dluX(10), FormSpec.DEFAULT_GROW)
        }, new RowSpec[]{
                new RowSpec(Sizes.DLUY4),
                FormFactory.DEFAULT_ROWSPEC,
                new RowSpec(Sizes.DLUY3),
                FormFactory.DEFAULT_ROWSPEC,
                new RowSpec(Sizes.DLUY4),
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                new RowSpec(Sizes.DLUY3),
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
        }));
        CellConstraints cc = new CellConstraints();

        var label1 = new JBLabel("1. Custom java bean for 'Param Type' and '<T>' must be fully qualified class name.");
        var label2 = new JBLabel("    Example: org.example.User");
        label2.setForeground(InlineHelpText.COLOR);

        var label3 = new JBLabel("2. If method 'Return Types' is 'PagedResource<T>' and has another method that ends with ");
        var label4 = new JBLabel("    'count', 'Count' or '-count', it will be treated as count query.");

        var label5 = new JBLabel("    /*[queryUsers]*/");
        label5.setForeground(InlineHelpText.COLOR);
        var label6 = new JBLabel("    select * from user where id = :id;");
        label6.setForeground(InlineHelpText.COLOR);
        var label7 = new JBLabel("    /*[queryUsersCount]*/");
        label7.setForeground(InlineHelpText.COLOR);
        var label8 = new JBLabel("    select count(*) from user where id = :id;");
        label8.setForeground(InlineHelpText.COLOR);

        panel.add(label1, cc.xy(1, 2));
        panel.add(label2, cc.xy(1, 4));
        panel.add(label3, cc.xy(1, 6));
        panel.add(label4, cc.xy(1, 7));
        panel.add(label5, cc.xy(1, 9));
        panel.add(label6, cc.xy(1, 10));
        panel.add(label7, cc.xy(1, 11));
        panel.add(label8, cc.xy(1, 12));

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
                        if (StringUtil.startsWiths(methodName, "get", "query", "search", "select", "list")) {
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
                        if (StringUtils.isNotEmpty(xqlMethod.getParamType())) {
                            paramType = xqlMethod.getParamType();
                        }
                        if (StringUtils.isNotEmpty(xqlMethod.getReturnGenericType())) {
                            returnGenericType = xqlMethod.getReturnGenericType();
                        }
                        enable = ObjectUtil.coalesce(xqlMethod.getEnable(), true);
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
