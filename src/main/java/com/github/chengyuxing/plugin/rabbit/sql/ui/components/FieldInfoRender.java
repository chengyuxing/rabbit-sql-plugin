package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Map;
import java.util.Set;

public class FieldInfoRender extends DefaultTableCellRenderer {
    final private Map<String, Set<String>> keyInfoMapping;

    public FieldInfoRender(Map<String, Set<String>> keyInfoMapping) {
        super();
        this.keyInfoMapping = keyInfoMapping;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {

        if (value != null && !value.equals("")) {
            var parts = keyInfoMapping.get(value.toString());
            var richText = "<html>" + value + "&nbsp;&nbsp;" + String.join(", ", parts) + "</html>";
            return super.getTableCellRendererComponent(table, richText, isSelected, hasFocus, row, column);
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
}
