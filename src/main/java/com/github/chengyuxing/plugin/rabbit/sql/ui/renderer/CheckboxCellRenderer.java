package com.github.chengyuxing.plugin.rabbit.sql.ui.renderer;

import com.intellij.ui.components.JBCheckBox;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class CheckboxCellRenderer extends JBCheckBox implements TableCellRenderer {
    public CheckboxCellRenderer() {
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof Boolean) {
            setSelected((Boolean) value);
            setBackground(table.getBackground());
        }
        return this;
    }
}
