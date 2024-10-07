package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.github.chengyuxing.common.utils.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.types.XQLMapperConfig;
import com.github.chengyuxing.plugin.rabbit.sql.ui.ReturnTypesDialog;
import com.github.chengyuxing.plugin.rabbit.sql.ui.renderer.ColorfulCellRenderer;
import com.github.chengyuxing.plugin.rabbit.sql.ui.renderer.SqlTypePlaceHolder;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.XQLInvocationHandler;
import com.github.chengyuxing.sql.yaml.HyphenatedPropertyUtil;
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
            "<T>"};
    public static final List<String> RETURN_TYPES = List.of(
            "List<T>",
            "Set<T>",
            "Stream<T>",
            "PagedResource<T>",
            "Optional<T>",
            "<T>",
            "IPageable",
            "Integer",
            "Long",
            "Double");
    public static final List<String> SQL_TYPES = List.of("query",
            "insert",
            "update",
            "delete",
            "procedure",
            "function",
            "ddl",
            "plsql");
    public static final List<String> GENERIC_TYPES = List.of("Map<String, Object>", "DataRow");
    public static final List<String> PARAM_TYPES = List.of("Map", "@Arg");

    public MapperGenerateForm(Project project, String alias, XQLFileManager xqlFileManager, XQLMapperConfig mapperConfig) {
        this.project = project;
        this.alias = alias;
        this.xqlFileManager = xqlFileManager;
        this.mapperConfig = mapperConfig;
        initComponents();
    }

    private void initComponents() {
        setMinimumSize(new Dimension(750, 300));
        setPreferredSize(new Dimension(1050, 520));
        setBorder(null);
        setLayout(new MigLayout(
                "fill,hidemode 3,align left top",
                // columns
                "[grow,left]",
                // rows
                "[fill]"));

        table = new JBTable();
        table.setBorder(BorderFactory.createEmptyBorder());
        table.setShowVerticalLines(false);
        table.setShowLastHorizontalLine(false);
        table.setRowHeight(30);
        table.setSelectionForeground(null);
        table.setSelectionBackground(null);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getEmptyText().setText("No SQLs in " + alias + ".");
        var scrollPane = new JBScrollPane();
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportView(table);
        add(scrollPane, "cell 0 0,grow");
        initTable();
    }

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
                    var methodName = sqlName.replace("_", "-");
                    methodName = HyphenatedPropertyUtil.camelize(methodName);
                    methodName = methodName.replaceAll("\\W", "");
                    var sqlType = "query";
                    var returnType = "List<T>";
                    if (methodName.matches(XQLInvocationHandler.INSERT_PATTERN)) {
                        sqlType = "";
                        returnType = "Integer";
                    } else if (methodName.matches(XQLInvocationHandler.UPDATE_PATTERN)) {
                        sqlType = "";
                        returnType = "Integer";
                    } else if (methodName.matches(XQLInvocationHandler.DELETE_PATTERN)) {
                        sqlType = "";
                        returnType = "Integer";
                    } else if (methodName.matches(XQLInvocationHandler.CALL_PATTERN)) {
                        sqlType = "";
                        returnType = "DataRow";
                    } else if (methodName.matches(XQLInvocationHandler.QUERY_PATTERN)) {
                        sqlType = "";
                        if (StringUtil.startsWiths(methodName, "query", "search", "select")) {
                            returnType = "List<T>";
                        } else {
                            returnType = "<T>";
                        }
                    }

                    var paramType = "Map";
                    var returnGenericType = "DataRow";

                    var mappingConfig = this.mapperConfig.getMethods().get(sqlName);
                    if (Objects.nonNull(mappingConfig)) {
                        if (StringUtils.isNotEmpty(mappingConfig.getSqlType())) {
                            sqlType = mappingConfig.getSqlType();
                        }
                        if (StringUtils.isNotEmpty(mappingConfig.getReturnType())) {
                            returnType = mappingConfig.getReturnType();
                        }
                        if (StringUtils.isNotEmpty(mappingConfig.getParamType())) {
                            paramType = mappingConfig.getParamType();
                        }
                        if (StringUtils.isNotEmpty(mappingConfig.getReturnGenericType())) {
                            returnGenericType = mappingConfig.getReturnGenericType();
                        }
                    }

                    return new Object[]{
                            sqlName,
                            methodName,
                            sqlType,
                            paramType,
                            returnType,
                            returnGenericType
                    };
                }).toArray(i -> new Object[i][6]);
        model.setDataVector(tbody, thead);
        table.getColumnModel().getColumn(0).setCellRenderer(new ColorfulCellRenderer(HtmlUtil.Color.HIGHLIGHT.getCode()));
        table.getColumnModel().getColumn(1).setCellRenderer(new ColorfulCellRenderer(HtmlUtil.Color.FUNCTION.getCode()));

        table.getColumnModel().getColumn(2).setCellEditor(buildSelector(false, SQL_TYPES));
        table.getColumnModel().getColumn(2).setCellRenderer(new SqlTypePlaceHolder());

        table.getColumnModel().getColumn(3).setCellEditor(buildSelector(true, PARAM_TYPES));
        table.getColumnModel().getColumn(5).setCellEditor(buildSelector(true, GENERIC_TYPES));
        table.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    var x = table.rowAtPoint(e.getPoint());
                    var y = table.columnAtPoint(e.getPoint());
                    if (x >= 0 && y >= 0) {
                        if (y == 4) {
                            var method = table.getValueAt(x, 1).toString();
                            var values = table.getValueAt(x, y).toString();
                            ApplicationManager.getApplication().invokeLater(() -> {
                                var queryTypesDialog = new ReturnTypesDialog(project, method, values, selected -> table.setValueAt(selected, x, y));
                                queryTypesDialog.showAndGet();
                            });
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

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