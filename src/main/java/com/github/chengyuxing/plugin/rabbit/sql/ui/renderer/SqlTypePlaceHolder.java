package com.github.chengyuxing.plugin.rabbit.sql.ui.renderer;

import com.github.chengyuxing.sql.XQLInvocationHandler;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Objects;

public class SqlTypePlaceHolder extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (Objects.equals("", value)) {
            var methodName = table.getModel().getValueAt(row, 1).toString();
            var sqlType = getSqlType(methodName);
            if (!sqlType.isEmpty()) {
                var holderCom = super.getTableCellRendererComponent(table, "<" + sqlType + ">", isSelected, hasFocus, row, column);
                holderCom.setForeground(JBColor.GRAY);
                return holderCom;
            }
        }
        var defaultCom = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        defaultCom.setForeground(null);
        return defaultCom;
    }

    private static @NotNull String getSqlType(String methodName) {
        var sqlType = "";
        if (XQLInvocationHandler.INSERT_PATTERN.matcher(methodName).matches()) {
            sqlType = "insert";
        } else if (XQLInvocationHandler.UPDATE_PATTERN.matcher(methodName).matches()) {
            sqlType = "update";
        } else if (XQLInvocationHandler.DELETE_PATTERN.matcher(methodName).matches()) {
            sqlType = "delete";
        } else if (XQLInvocationHandler.CALL_PATTERN.matcher(methodName).matches()) {
            sqlType = "procedure";
        } else if (XQLInvocationHandler.QUERY_PATTERN.matcher(methodName).matches()) {
            sqlType = "query";
        }
        return sqlType;
    }
}
