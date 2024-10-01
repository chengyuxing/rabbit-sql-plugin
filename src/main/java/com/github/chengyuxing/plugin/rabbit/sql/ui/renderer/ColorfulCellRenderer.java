package com.github.chengyuxing.plugin.rabbit.sql.ui.renderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class ColorfulCellRenderer extends DefaultTableCellRenderer {
    private final String hexColor;

    public ColorfulCellRenderer(String hexColor) {
        this.hexColor = hexColor;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        var code = hexColor.substring(1);
        setForeground(new Color(Integer.parseInt(code, 16)));
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
}
