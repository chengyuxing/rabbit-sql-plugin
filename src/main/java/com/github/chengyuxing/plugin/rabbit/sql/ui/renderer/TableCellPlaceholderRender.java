package com.github.chengyuxing.plugin.rabbit.sql.ui.renderer;

import com.github.chengyuxing.common.script.expression.Comparators;
import com.github.chengyuxing.common.utils.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.common.Global;
import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TableCellPlaceholderRender extends DefaultTableCellRenderer {
    final private String placeholder;

    public TableCellPlaceholderRender(String placeholder) {
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
            placeHolderCom.setFont(Global.getEditorFont(placeHolderCom.getFont().getSize()));
            placeHolderCom.setForeground(JBColor.GRAY);
            return placeHolderCom;
        } else {
            var valueCom = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (StringUtil.isNumeric(value)) {
                valueCom.setForeground(new JBColor(new Color(0x364FED), new Color(0x56A9B6)));
            } else if (Comparators.isQuote(value.toString())) {
                valueCom.setForeground(new JBColor(new Color(0x097C52), new Color(0x79A978)));
            } else if (isJSON(value.toString())) {
                valueCom.setForeground(new JBColor(new Color(0x9C9715), new Color(0xBBB529)));
            } else if (Comparators.valueOf(value) instanceof Comparators.ValueType) {
                valueCom.setForeground(new JBColor(new Color(0x1D31BC), new Color(0xCC7832)));
            } else {
                valueCom.setForeground(new JBColor(new Color(0x097C52), new Color(0x79A978)));
            }
            valueCom.setFont(Global.getEditorFont(valueCom.getFont().getSize()));
            valueCom.setBackground(null);
            return valueCom;
        }
    }

    static boolean isJSON(String text) {
        if (text.startsWith("[") && text.endsWith("]")) {
            return true;
        }
        return text.startsWith("{") && text.endsWith("}");
    }
}
