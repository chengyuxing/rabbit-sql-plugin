package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.github.chengyuxing.plugin.rabbit.sql.common.DatasourceCache;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class IconListCellRenderer extends DefaultListCellRenderer {
    private final Map<DatasourceCache.DatabaseId, Icon> info;

    public IconListCellRenderer(Map<DatasourceCache.DatabaseId, Icon> info) {
        this.info = info;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        var label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof DatasourceCache.DatabaseId dbId) {
            var icon = info.get(dbId);
            if (icon != null) {
                label.setIcon(icon);
            }
        }
        return label;
    }
}
