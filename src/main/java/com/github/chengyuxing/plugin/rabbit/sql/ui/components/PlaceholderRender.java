package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.github.chengyuxing.common.script.Comparators;
import com.github.chengyuxing.common.utils.StringUtil;
import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class PlaceholderRender extends DefaultTableCellRenderer {
    final private String placeholder;

    public PlaceholderRender(String placeholder) {
        super();
        this.placeholder = placeholder;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {

        if ((value == null) || (value.equals(""))) {
            var placeHolderCom = super.getTableCellRendererComponent(table, this.placeholder, isSelected, hasFocus, row, column);
            placeHolderCom.setForeground(JBColor.GRAY);
            return placeHolderCom;
        } else {
            var valueCom = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            var typedValue = Comparators.valueOf(value);
            if (typedValue instanceof Comparators.ValueType) {
                valueCom.setForeground(new JBColor(new Color(0xCC7832), new Color(0x1D31BC)));
            } else if (StringUtil.isNumeric(typedValue)) {
                valueCom.setForeground(new JBColor(new Color(0x56A9B6), new Color(0x364fed)));
            } else if (Comparators.isString(typedValue)) {
                valueCom.setForeground(new JBColor(new Color(0x79A978), new Color(0x097c52)));
            }
            valueCom.setBackground(null);
            return valueCom;
        }
    }
}
