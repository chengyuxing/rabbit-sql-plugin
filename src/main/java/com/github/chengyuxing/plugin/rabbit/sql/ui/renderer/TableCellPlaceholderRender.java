package com.github.chengyuxing.plugin.rabbit.sql.ui.renderer;

import com.github.chengyuxing.common.util.StringUtils;
import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.Global;
import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

import static com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil.isQuote;

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
            if (StringUtils.isNumber(value)) {
                valueCom.setForeground(new JBColor(new Color(0x364FED), new Color(0x56A9B6)));
            } else if (isQuote(value.toString())) {
                valueCom.setForeground(new JBColor(new Color(0x097C52), new Color(0x79A978)));
            } else if (isJSON(value.toString())) {
                valueCom.setForeground(new JBColor(new Color(0x9C9715), new Color(0xBBB529)));
            } else if (StringUtils.equalsAnyIgnoreCase(value.toString(), Constants.XQL_VALUE_KEYWORDS)) {
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
