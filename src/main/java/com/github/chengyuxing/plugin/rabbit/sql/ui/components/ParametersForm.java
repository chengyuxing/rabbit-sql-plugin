/*
 * Created by JFormDesigner on Mon May 29 16:00:17 CST 2023
 */

package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.github.chengyuxing.common.script.Comparators;
import com.github.chengyuxing.common.utils.ReflectUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.ExceptionUtil;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chengyuxing
 */
public class ParametersForm extends JPanel {
    private final List<String> parameterNames;
    private final List<String> errors = new ArrayList<>();

    public ParametersForm(List<String> parameterNames) {
        this.parameterNames = parameterNames;
        initComponents();
        buildTableData();
    }

    public List<String> getErrors() {
        return errors;
    }

    public Map<String, ?> getData() {
        errors.clear();
        if (paramsTable.isEditing()) {
            paramsTable.getCellEditor().stopCellEditing();
        }
        var model = (DefaultTableModel) paramsTable.getModel();
        var data = model.getDataVector();
        var map = new HashMap<String, Object>();
        data.forEach(row -> {
            var k = row.get(0).toString();
            var v = row.get(1);
            if (v != null) {
                var sv = v.toString().trim();
                if (sv.startsWith("[") && sv.endsWith("]")) {
                    try {
                        v = ReflectUtil.json2Obj(sv, List.class);
                    } catch (Exception e) {
                        errors.add("JSON array of parameter '" + k + "' serialized error.");
                        errors.addAll(ExceptionUtil.getCauseMessages(e));
                    }
                }
            }
            var objV = Comparators.valueOf(v);
            map.put(k, objV);
        });
        return map;
    }

    public void setSqlHtml(String sql) {
        sqlContent.setText(sql);
    }

    private void buildTableData() {
        var params = parameterNames.stream()
                .distinct()
                .map(name -> new Object[]{name, ""})
                .toArray(i -> new Object[i][2]);
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };
        paramsTable.setModel(model);
        model.setDataVector(params, new Object[]{"", ""});
        paramsTable.getColumnModel().getColumn(1).setCellEditor(buildParamsEditor());
        paramsTable.getColumnModel().getColumn(1).setCellRenderer(new PlaceholderRender("<blank>"));
        paramsTable.setTableHeader(null);
    }

    private DefaultCellEditor buildParamsEditor() {
        var cbx = new ComboBox<>();
        cbx.addItem("blank");
        cbx.addItem("null");
        cbx.addItem("true");
        cbx.addItem("false");
        cbx.setEditable(true);
        return new DefaultCellEditor(cbx);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        scrollPane1 = new JScrollPane();
        paramsTable = new JTable();
        scrollPane2 = new JScrollPane();
        sqlContent = new JTextPane();

        //======== this ========
        setBorder(null);
        setMinimumSize(new Dimension(58, 22));
        setPreferredSize(new Dimension(370, 190));
        setLayout(new MigLayout(
            "fill,insets 0,hidemode 3,align left top",
            // columns
            "[fill]",
            // rows
            "[233,grow,center]" +
            "[]"));

        //======== scrollPane1 ========
        {
            scrollPane1.setBorder(new LineBorder(new JBColor(new Color(0xD2D2D2), new Color(0x323232))));

            //---- paramsTable ----
            paramsTable.setBorder(null);
            paramsTable.setShowHorizontalLines(false);
            paramsTable.setShowVerticalLines(false);
            paramsTable.setIntercellSpacing(new Dimension(0, 0));
            paramsTable.setFillsViewportHeight(true);
            paramsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            paramsTable.setFont(new Font("JetBrains Mono", Font.PLAIN, 14));
            paramsTable.setRowHeight(30);
            paramsTable.setSelectionBackground(null);
            scrollPane1.setViewportView(paramsTable);
        }
        add(scrollPane1, "cell 0 0,aligny top,growy 0,hmin 80");

        //======== scrollPane2 ========
        {
            scrollPane2.setBorder(new LineBorder(new JBColor(new Color(0xD2D2D2), new Color(0x323232))));

            //---- sqlContent ----
            sqlContent.setContentType("text/html");
            sqlContent.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
            sqlContent.setEditable(false);
            sqlContent.setMargin(JBUI.insets(5));
            scrollPane2.setViewportView(sqlContent);
        }
        add(scrollPane2, "cell 0 1,aligny top,grow 100 0,hmin 200");
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JScrollPane scrollPane1;
    private JTable paramsTable;
    private JScrollPane scrollPane2;
    private JTextPane sqlContent;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
