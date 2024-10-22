package com.github.chengyuxing.plugin.rabbit.sql.ui.renderer;

import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class XQLMethodCellRenderer extends DefaultTableCellRenderer {
    private final XQLFileManager.Resource resource;
    private final String hexColor;

    public XQLMethodCellRenderer(XQLFileManager.Resource resource, String hexColor) {
        this.resource = resource;
        this.hexColor = hexColor;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        var code = Integer.parseInt(hexColor.substring(1), 16);
        @SuppressWarnings("UseJBColor") var color = new Color(code);
        setForeground(new JBColor(color.darker(), color));
        var description = resource.getEntry().get(value.toString()).getDescription();
        if (column == table.convertColumnIndexToView(0) && !description.isEmpty()) {
            setToolTipText(description);
        } else {
            setToolTipText(null);
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
}
