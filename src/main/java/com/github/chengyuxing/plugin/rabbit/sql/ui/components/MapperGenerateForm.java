package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.github.chengyuxing.common.utils.ObjectUtil;
import com.github.chengyuxing.common.utils.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.ui.renderer.CheckboxCellRenderer;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XQLJavaType;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XQLMapperConfig;
import com.github.chengyuxing.plugin.rabbit.sql.ui.ReturnTypesDialog;
import com.github.chengyuxing.plugin.rabbit.sql.ui.renderer.ColorfulCellRenderer;
import com.github.chengyuxing.plugin.rabbit.sql.ui.renderer.SqlTypePlaceHolder;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.XQLInvocationHandler;
import com.github.chengyuxing.sql.annotation.Type;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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
    public static final List<String> SQL_TYPES = List.of(Type.query.name(),
            Type.insert.name(),
            Type.update.name(),
            Type.delete.name(),
            Type.procedure.name(),
            Type.function.name(),
            Type.ddl.name(),
            Type.plsql.name());
    public static final List<String> GENERIC_TYPES = List.of(XQLJavaType.Map.toString(), XQLJavaType.DataRow.toString());
    public static final List<String> PARAM_TYPES = List.of(XQLJavaType.Map.getValue(), XQLJavaType.MultiArgs.toString());

    public MapperGenerateForm(Project project, String alias, XQLFileManager xqlFileManager, XQLMapperConfig mapperConfig) {
        this.project = project;
        this.alias = alias;
        this.xqlFileManager = xqlFileManager;
        this.mapperConfig = mapperConfig;
        initComponents();
    }

    private void initComponents() {
        setPreferredSize(new Dimension(750, 300));
        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new MigLayout(
                "fill,hidemode 3,align left top",
                // columns
                "[grow,left]",
                // rows
                "[fill]"));

        table = new JBTable() {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 6) {
                    return Boolean.class;
                }
                return super.getColumnClass(column);
            }
        };
        table.setBorder(BorderFactory.createEmptyBorder());
        table.setShowVerticalLines(false);
        table.setShowLastHorizontalLine(false);
        table.setRowHeight(30);
        table.setSelectionForeground(null);
        table.setSelectionBackground(null);
        table.setFillsViewportHeight(true);
        table.getEmptyText().setText("No SQLs in " + alias + ".");
        var scrollPane = new JBScrollPane();
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportView(table);
        add(scrollPane, "cell 0 0,grow");
        initTable();
    }

    @SuppressWarnings("rawtypes")
    public Vector<Vector> getData() {
        return ((DefaultTableModel) table.getModel()).getDataVector();
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
                    var sqlType = Type.query.name();
                    var returnType = XQLJavaType.List.toString();
                    if (methodName.matches(XQLInvocationHandler.INSERT_PATTERN)) {
                        sqlType = Type.insert.name();
                        returnType = XQLJavaType.Integer.getValue();
                    } else if (methodName.matches(XQLInvocationHandler.UPDATE_PATTERN)) {
                        sqlType = Type.update.name();
                        returnType = XQLJavaType.Integer.getValue();
                    } else if (methodName.matches(XQLInvocationHandler.DELETE_PATTERN)) {
                        sqlType = Type.delete.name();
                        returnType = XQLJavaType.Integer.getValue();
                    } else if (methodName.matches(XQLInvocationHandler.CALL_PATTERN)) {
                        sqlType = Type.procedure.name();
                        returnType = XQLJavaType.GenericT.getValue();
                    } else if (methodName.matches(XQLInvocationHandler.QUERY_PATTERN)) {
                        sqlType = Type.query.name();
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
        table.getColumnModel().getColumn(0).setCellRenderer(new ColorfulCellRenderer(HtmlUtil.Color.HIGHLIGHT.getCode()));
        table.getColumnModel().getColumn(1).setCellRenderer(new ColorfulCellRenderer(HtmlUtil.Color.FUNCTION.getCode()));

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
                    if (x >= 0 && y == 4) {
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
                if (x >= 0 && y == 6) {
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
