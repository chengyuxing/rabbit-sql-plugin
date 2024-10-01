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
        if (methodName.matches(XQLInvocationHandler.INSERT_PATTERN)) {
            sqlType = "insert";
        } else if (methodName.matches(XQLInvocationHandler.UPDATE_PATTERN)) {
            sqlType = "update";
        } else if (methodName.matches(XQLInvocationHandler.DELETE_PATTERN)) {
            sqlType = "delete";
        } else if (methodName.matches(XQLInvocationHandler.CALL_PATTERN)) {
            sqlType = "procedure";
        } else if (methodName.matches(XQLInvocationHandler.QUERY_PATTERN)) {
            sqlType = "query";
        }
        return sqlType;
    }
}
