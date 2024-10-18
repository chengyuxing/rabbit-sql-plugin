package com.github.chengyuxing.plugin.rabbit.sql.ui.renderer;

import com.github.chengyuxing.plugin.rabbit.sql.plugins.database.DatasourceManager;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class IconListCellRenderer extends DefaultListCellRenderer {
    private final Map<DatasourceManager.DatabaseId, Icon> info;

    public IconListCellRenderer(Map<DatasourceManager.DatabaseId, Icon> info) {
        this.info = info;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        var label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof DatasourceManager.DatabaseId) {
            var icon = info.get(value);
            if (icon != null) {
                label.setIcon(icon);
            }
        }
        return label;
    }
}
