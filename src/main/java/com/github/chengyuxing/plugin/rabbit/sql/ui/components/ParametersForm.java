/*
 * Created by JFormDesigner on Mon May 29 16:00:17 CST 2023
 */

package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.fasterxml.jackson.jr.ob.JSON;
import com.github.chengyuxing.common.script.Comparators;
import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.common.utils.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.ui.renderer.FieldInfoRender;
import com.github.chengyuxing.plugin.rabbit.sql.ui.renderer.TableCellPlaceholderRender;
import com.github.chengyuxing.plugin.rabbit.sql.util.ExceptionUtil;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * @author chengyuxing
 */
public class ParametersForm extends JPanel {
    private final Map<String, Set<String>> paramsMapping;
    private final Map<String, Object> paramsHistory;

    public ParametersForm(Map<String, Set<String>> paramsMapping, Map<String, Object> paramsHistory) {
        this.paramsMapping = paramsMapping;
        this.paramsHistory = paramsHistory;
        initComponents();
        initCustomComponents();
        initComponentConfigs();
    }

    public Pair<Map<String, ?>, List<String>> getData() {
        if (paramsTable.isEditing()) {
            paramsTable.getCellEditor().stopCellEditing();
        }
        var model = (DefaultTableModel) paramsTable.getModel();
        var data = model.getDataVector();
        var map = new HashMap<String, Object>();
        var errors = new ArrayList<String>();
        data.forEach(row -> {
            var k = row.get(0).toString();
            var v = row.get(1);
            if (v != null) {
                var sv = v.toString().trim();
                if (sv.startsWith("[") && sv.endsWith("]")) {
                    try {
                        v = JSON.std.listFrom(sv);
                    } catch (Exception e) {
                        errors.add("JSON array of parameter '" + k + "' serialized error.");
                        errors.addAll(ExceptionUtil.getCauseMessages(e));
                    }
                } else if (sv.startsWith("{") && sv.endsWith("}")) {
                    try {
                        v = JSON.std.mapFrom(sv);
                    } catch (Exception e) {
                        errors.add("JSON object of parameter '" + k + "' serialized error.");
                        errors.addAll(ExceptionUtil.getCauseMessages(e));
                    }
                } else if (StringUtil.isNumeric(sv)) {
                    if (sv.contains(".")) {
                        v = Double.parseDouble(sv);
                    } else {
                        v = Integer.parseInt(sv);
                    }
                } else if (StringUtil.equalsAnyIgnoreCase(sv, "blank", "null", "true", "false")) {
                    v = Comparators.valueOf(v);
                }
            }
            map.put(k, v);
        });
        return Pair.of(map, errors);
    }

    public void setSqlHtml(String sql) {
        sqlContent.setText(sql);
        if (!scrollPane2.isVisible()) {
            scrollPane2.setVisible(true);
        }
    }

    private void initCustomComponents() {
        var splitter = new JBSplitter();
        splitter.setOrientation(true);
        splitter.setProportion(0.4f);
        splitter.setFirstComponent(scrollPane1);
        splitter.setSecondComponent(scrollPane2);
        add(splitter, "cell 0 0,grow");
    }

    private void initComponentConfigs() {
        var params = paramsMapping.keySet().stream()
                .map(name -> new Object[]{name, paramsHistory.getOrDefault(name, "")})
                .toArray(i -> new Object[i][2]);
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };
        paramsTable.setModel(model);
        model.setDataVector(params, new Object[]{"", ""});
        paramsTable.getColumnModel().getColumn(0).setCellRenderer(new FieldInfoRender(paramsMapping));
        paramsTable.getColumnModel().getColumn(1).setCellEditor(buildParamsEditor());
        paramsTable.getColumnModel().getColumn(1).setCellRenderer(new TableCellPlaceholderRender("<blank>"));
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
        scrollPane1 = new JBScrollPane();
        paramsTable = new JBTable();
        scrollPane2 = new JBScrollPane();
        sqlContent = new JTextPane();

        //======== this ========
        setBorder(BorderFactory.createEmptyBorder());
        setMinimumSize(new Dimension(58, 22));
        setPreferredSize(new Dimension(470, 115));
        setLayout(new MigLayout(
                "insets 0,hidemode 3",
                // columns
                "[grow 1,fill]",
                // rows
                "[grow 1,fill]"));

        //======== scrollPane1 ========
        {
            scrollPane1.setBorder(new LineBorder(new JBColor(new Color(0xD2D2D2), new Color(0x323232))));

            //---- paramsTable ----
            paramsTable.setBorder(BorderFactory.createEmptyBorder());
            paramsTable.setShowHorizontalLines(false);
            paramsTable.setShowVerticalLines(false);
            paramsTable.setIntercellSpacing(new Dimension(0, 0));
            paramsTable.setFillsViewportHeight(true);
            paramsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            paramsTable.setFont(paramsTable.getFont().deriveFont(paramsTable.getFont().getSize() + 1f));
            paramsTable.setRowHeight(30);
            paramsTable.setSelectionForeground(null);
            paramsTable.setSelectionBackground(null);
            scrollPane1.setViewportView(paramsTable);
        }

        //======== scrollPane2 ========
        {
            scrollPane2.setBorder(new LineBorder(new JBColor(new Color(0xD2D2D2), new Color(0x323232))));
            scrollPane2.setVisible(false);
            //---- sqlContent ----
            sqlContent.setContentType("text/html");
            sqlContent.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
            sqlContent.setEditable(false);
            sqlContent.setMargin(JBUI.insets(3,10));
            scrollPane2.setViewportView(sqlContent);
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JScrollPane scrollPane1;
    private JTable paramsTable;
    private JScrollPane scrollPane2;
    private JTextPane sqlContent;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
