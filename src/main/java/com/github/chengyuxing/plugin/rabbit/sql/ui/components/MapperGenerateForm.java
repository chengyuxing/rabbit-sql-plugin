package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.github.chengyuxing.common.util.StringUtils;
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
import com.intellij.ui.JBColor;
import com.intellij.ui.TabbedPaneWrapper;
import com.intellij.ui.components.*;
import com.intellij.ui.table.JBTable;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.*;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;

import static com.github.chengyuxing.sql.annotation.SqlStatementType.*;

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
    // (return type, allows sql type)
    public static final Map<String, Set<String>> RETURN_TYPES = new LinkedHashMap<>() {
        {
            put(XQLJavaType.Stream.toString(), Set.of(query.name()));
            put(XQLJavaType.List.toString(), Set.of(query.name()));
            put(XQLJavaType.Set.toString(), Set.of(query.name()));
            put(XQLJavaType.Optional.toString(), Set.of(query.name()));
            put(XQLJavaType.GenericT.getValue(), Set.of(
                    query.name(), insert.name(),
                    delete.name(), update.name(),
                    procedure.name(), function.name(),
                    dml.name(), plsql.name(),
                    unset.name(), ddl.name())
            );
            put(XQLJavaType.String.getValue(), Set.of(query.name()));
            put(XQLJavaType.Integer.getValue(), Set.of(
                    query.name(), insert.name(),
                    delete.name(), update.name(),
                    dml.name())
            );
            put(XQLJavaType.Long.getValue(), Set.of(query.name()));
            put(XQLJavaType.Double.getValue(), Set.of(query.name()));
            put(XQLJavaType.Boolean.getValue(), Set.of(query.name()));
            put(XQLJavaType.BatchResult.getValue(), Set.of(batch.name()));
            put(XQLJavaType.IPageable.getValue(), Set.of(query.name()));
            put(XQLJavaType.PagedResource.toString(), Set.of(query.name()));
        }
    };
    public static final List<String> SQL_TYPES = List.of(
            query.name(),
            insert.name(),
            update.name(),
            delete.name(),
            dml.name(),
            batch.name(),
            procedure.name(),
            function.name(),
            ddl.name(),
            plsql.name(),
            unset.name());
    public static final List<String> GENERIC_TYPES = List.of(
            XQLJavaType.DataRow.getValue(),
            XQLJavaType.Map.getValue());
    public static final List<String> PARAM_TYPES = List.of(
            XQLJavaType.Map.getValue(),
            XQLJavaType.Object.getValue());

    public static final ComboBox<String> GENERIC_TYPES_COMBOBOX = new ComboBox<>(GENERIC_TYPES.toArray(new String[0]));

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

            @Override
            public @NotNull Component prepareRenderer(@NotNull TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                String sqlName = table.getValueAt(row, convertColumnIndexToView(0)).toString();
                boolean exists = mapperConfig.getMethods().containsKey(sqlName);
                if (!exists) {
                    comp.setBackground(new JBColor(new Color(0.1f, 0.9f, 0.1f, 0.4f), new Color(0.1f, 0.9f, 0.1f, 0.2f)));
                } else {
                    comp.setBackground(table.getBackground());
                }
                return comp;
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
                word-break:break-all;
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
        var subquery = HtmlUtil.highlightSql("""
                /*[queryUsersCustomPage]*/
                with cte as (select * from user limit :length offset :index)
                select * from cte;
                """);
        var method = HtmlUtil.pre("""
                @PageableConfig(disableDefaultPageSql = {"length", "index"}, pageHelper = org.example.MyPagehelper.class)
                PagedResource&lt;DataRow&gt; queryUsersCustomPage(Map&lt;String, Object&gt;);
                """, HtmlUtil.Color.EMPTY);
        var content = StringUtils.FMT.format(html,
                Map.of("about", MessageBundle.message("ui.mapperGenForm.tab2.about"),
                        "exampleSql", exampleSql,
                        "subquery", subquery,
                        "method", method));
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

        model.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int column = e.getColumn();
                if (column == 2) {
                    var sqlType = model.getValueAt(e.getFirstRow(), 2).toString();
                    var returnType = detectReturnTypeBySqlType(sqlType);
                    model.setValueAt(returnType, e.getFirstRow(), 4);
                    if (!Objects.equals(sqlType, query.name())) {
                        model.setValueAt(GENERIC_TYPES.get(0), e.getFirstRow(), 5);
                    }
                }
            }
        });

        table.setModel(model);

        var tbody = xqlFileManager.getResource(alias).getEntry()
                .keySet()
                .stream()
                .filter(key -> !key.startsWith("${"))
                .map(sqlName -> {
                    var methodName = com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil.camelizeAndClean(sqlName);
                    var sqlType = query.name();
                    var returnType = new XQLMapperConfig.ReturnType();
                    var paramType = XQLJavaType.Map.getValue();
                    var returnGenericType = XQLJavaType.DataRow.getValue();
                    returnType.itemsOf(XQLJavaType.List.toString());
                    if (XQLInvocationHandler.INSERT_PATTERN.matcher(methodName).matches()) {
                        sqlType = SqlStatementType.insert.name();
                        returnType.itemsOf(XQLJavaType.Integer.getValue());
                    } else if (XQLInvocationHandler.UPDATE_PATTERN.matcher(methodName).matches()) {
                        sqlType = SqlStatementType.update.name();
                        returnType.itemsOf(XQLJavaType.Integer.getValue());
                    } else if (XQLInvocationHandler.DELETE_PATTERN.matcher(methodName).matches()) {
                        sqlType = SqlStatementType.delete.name();
                        returnType.itemsOf(XQLJavaType.Integer.getValue());
                    } else if (XQLInvocationHandler.BATCH_PATTERN.matcher(methodName).matches()) {
                        sqlType = SqlStatementType.batch.name();
                        returnType.itemsOf(XQLJavaType.BatchResult.getValue());
                        paramType = XQLJavaType.Object.getValue();
                    } else if (XQLInvocationHandler.CALL_PATTERN.matcher(methodName).matches()) {
                        sqlType = SqlStatementType.procedure.name();
                        returnType.itemsOf(XQLJavaType.GenericT.getValue());
                    } else if (XQLInvocationHandler.QUERY_PATTERN.matcher(methodName).matches()) {
                        sqlType = query.name();
                        if (StringUtils.startsWiths(methodName, "get", "query", "search", "select", "list")) {
                            returnType.itemsOf(XQLJavaType.List.toString());
                        } else {
                            returnType.itemsOf(XQLJavaType.GenericT.getValue());
                        }
                    }

                    var enable = true;

                    var xqlMethod = this.mapperConfig.getMethods().get(sqlName);
                    if (Objects.nonNull(xqlMethod)) {
                        if (!StringUtils.isEmpty(xqlMethod.getSqlType()) && SQL_TYPES.contains(xqlMethod.getSqlType())) {
                            sqlType = xqlMethod.getSqlType();
                        }
                        if (xqlMethod.getReturnType() != null &&
                                RETURN_TYPES.keySet().containsAll(xqlMethod.getReturnType().getItems())) {
                            returnType = xqlMethod.getReturnType();
                        }

                        var paramMeta = xqlMethod.getParamMeta();
                        String myPramType = null;
                        if (Objects.nonNull(paramMeta)) {
                            var className = paramMeta.getClassName();
                            if (!StringUtils.isEmpty(className)) {
                                myPramType = className;
                            }
                        }
                        if (!StringUtils.isEmpty(xqlMethod.getParamType())) {
                            myPramType = xqlMethod.getParamType().equals("@Arg")
                                    ? XQLJavaType.Object.getValue()
                                    : xqlMethod.getParamType();
                        }
                        if (myPramType != null) {
                            paramType = myPramType;
                        }

                        if (!StringUtils.isEmpty(xqlMethod.getReturnGenericType())) {
                            returnGenericType = xqlMethod.getReturnGenericType();
                        }
                        enable = ValueUtils.coalesceNonNull(xqlMethod.getEnable(), true);
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
        table.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(GENERIC_TYPES_COMBOBOX) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                var sqlTypeCol = table.convertColumnIndexToView(2);
                var sqlType = table.getValueAt(row, sqlTypeCol).toString();
                // only query type can have custom generic type
                GENERIC_TYPES_COMBOBOX.setEditable(Objects.equals(sqlType, query.name()));
                return super.getTableCellEditorComponent(table, value, isSelected, row, column);
            }
        });
        table.getColumnModel().getColumn(6).setCellRenderer(new CheckboxCellRenderer());
        table.getColumnModel().getColumn(6).setMaxWidth(60);

        table.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    var x = table.rowAtPoint(e.getPoint());
                    var y = table.columnAtPoint(e.getPoint());
                    var returnTypeCol = table.convertColumnIndexToView(4);
                    if (x >= 0 && y == returnTypeCol) {
                        var method = table.getValueAt(x, table.convertColumnIndexToView(1)).toString();
                        var sqlType = table.getValueAt(x, table.convertColumnIndexToView(2)).toString();
                        var values = table.getValueAt(x, returnTypeCol);
                        ApplicationManager.getApplication().invokeLater(() -> {
                            var queryTypesDialog = new ReturnTypesDialog(project,
                                    sqlType,
                                    method,
                                    (XQLMapperConfig.ReturnType) values,
                                    selected -> table.setValueAt(selected, x, returnTypeCol));
                            queryTypesDialog.showAndGet();
                        });
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                var x = table.rowAtPoint(e.getPoint());
                var y = table.columnAtPoint(e.getPoint());
                var enableCol = table.convertColumnIndexToView(6);
                if (x >= 0 && y == enableCol) {
                    var currentValue = (Boolean) table.getValueAt(x, enableCol);
                    table.setValueAt(!currentValue, x, enableCol);
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

    private static XQLMapperConfig.@NotNull ReturnType detectReturnTypeBySqlType(String sqlType) {
        var returnType = new XQLMapperConfig.ReturnType(XQLJavaType.List.toString());
        if (StringUtils.equalsAny(sqlType,
                insert.name(), update.name(), delete.name())) {
            returnType.itemsOf(XQLJavaType.Integer.getValue());
        } else if (StringUtils.equalsAny(sqlType,
                procedure.name(), function.name(),
                dml.name(), ddl.name(),
                unset.name(), plsql.name())) {
            returnType.itemsOf(XQLJavaType.GenericT.getValue());
        } else if (Objects.equals(sqlType, batch.name())) {
            returnType.itemsOf(XQLJavaType.BatchResult.getValue());
        }
        return returnType;
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
