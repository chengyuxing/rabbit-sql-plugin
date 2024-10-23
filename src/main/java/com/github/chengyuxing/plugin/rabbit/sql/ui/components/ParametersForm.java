/*
 * Created by JFormDesigner on Mon May 29 16:00:17 CST 2023
 */

package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.github.chengyuxing.common.MostDateTime;
import com.github.chengyuxing.common.script.expression.Comparators;
import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.common.utils.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.common.Global;
import com.github.chengyuxing.plugin.rabbit.sql.ui.renderer.FieldInfoRender;
import com.github.chengyuxing.plugin.rabbit.sql.ui.renderer.TableCellPlaceholderRender;
import com.github.chengyuxing.plugin.rabbit.sql.util.ExceptionUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.JSON;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.UIUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.*;

/**
 * @author chengyuxing
 */
public class ParametersForm extends JPanel {
    private final Map<String, Set<String>> paramsMapping;
    private final Map<String, Object> paramsHistory;
    private Runnable clickEmptyTableTextLink = () -> {
    };

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
                if (sv.matches(".+::[a-zA-Z]+$")) {
                    try {
                        var type = sv.substring(sv.lastIndexOf("::") + 2);
                        var value = sv.substring(0, sv.lastIndexOf("::"));
                        switch (type) {
                            case "number":
                                v = Double.parseDouble(value);
                                break;
                            case "date":
                                v = MostDateTime.of(value).toDate();
                                break;
                            default:
                                v = value;
                                break;
                        }
                    } catch (Exception e) {
                        errors.add("Type parse of parameter '" + k + "' error.");
                        errors.addAll(ExceptionUtil.getCauseMessages(e));
                    }
                } else if (sv.startsWith("[") && sv.endsWith("]")) {
                    try {
                        v = JSON.std.readValue(sv, List.class);
                    } catch (Exception e) {
                        errors.add("JSON array of parameter '" + k + "' serialized error.");
                        errors.addAll(ExceptionUtil.getCauseMessages(e));
                    }
                } else if (sv.startsWith("{") && sv.endsWith("}")) {
                    try {
                        v = JSON.std.readValue(sv, Map.class);
                    } catch (Exception e) {
                        errors.add("JSON object of parameter '" + k + "' serialized error.");
                        errors.addAll(ExceptionUtil.getCauseMessages(e));
                    }
                } else if (StringUtil.isNumeric(sv)) {
                    try {
                        if (sv.contains(".")) {
                            v = Double.parseDouble(sv);
                        } else {
                            v = Long.parseLong(sv);
                        }
                    } catch (Exception e) {
                        errors.add("Parse number '" + k + "' error.");
                        errors.addAll(ExceptionUtil.getCauseMessages(e));
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
        if (!scrollPane2.isVisible()) {
            scrollPane2.setVisible(true);
        }
        sqlContent.setText(HtmlUtil.toHtml(sql));
        sqlContent.setCaretPosition(0);
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
        paramsTable.getEmptyText().setText("No parameters need to be entered.");
        paramsTable.getEmptyText().appendSecondaryText("Show raw sql", SimpleTextAttributes.LINK_PLAIN_ATTRIBUTES, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clickEmptyTableTextLink.run();
            }
        });
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
        cbx.setFont(Global.getEditorFont(cbx.getFont().getSize()));
        cbx.setEditor(new BasicComboBoxEditor() {
            @Override
            protected JTextField createEditorComponent() {
                return new ExpandableTextField();
            }
        });
        return new DefaultCellEditor(cbx);
    }

    public void setClickEmptyTableTextLink(Runnable clickEmptyTableTextLink) {
        this.clickEmptyTableTextLink = clickEmptyTableTextLink;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        scrollPane1 = new JBScrollPane();
        paramsTable = new JBTable();
        scrollPane2 = new JBScrollPane();
        sqlContent = new JEditorPane();

        //======== this ========
        setBorder(BorderFactory.createEmptyBorder());
        setMinimumSize(new Dimension(58, 22));
        setPreferredSize(new Dimension(520, 135));
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
            UIUtil.addInsets(sqlContent, 2, 10, 2, 10);
            sqlContent.setContentType("text/html");
            sqlContent.setEditable(false);
            sqlContent.setOpaque(false);
            var color = UIManager.getColor("Label.background");
            sqlContent.setBackground(new JBColor(color, color.darker()));
            scrollPane2.setViewportView(sqlContent);
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JBScrollPane scrollPane1;
    private JBTable paramsTable;
    private JBScrollPane scrollPane2;
    private JEditorPane sqlContent;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
