package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class IconListCellRenderer extends DefaultListCellRenderer {
    private final Map<String, Icon> info;

    public IconListCellRenderer(Map<String, Icon> info) {
        this.info = info;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        var label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        var icon = info.get(value.toString());
        if (icon != null) {
            label.setIcon(icon);
        }
        return label;
    }
}
