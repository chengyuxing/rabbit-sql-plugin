package com.github.chengyuxing.plugin.rabbit.sql.ui.renderer;

import com.github.chengyuxing.plugin.rabbit.sql.ui.types.DataCell;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class LinkCellRender extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof DataCell) {
            var richText = "<html>" + HtmlUtil.span(value.toString(), HtmlUtil.Color.FUNCTION, "text-decoration: underline") + "</html>";
            return super.getTableCellRendererComponent(table, richText, isSelected, hasFocus, row, column);
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
}
