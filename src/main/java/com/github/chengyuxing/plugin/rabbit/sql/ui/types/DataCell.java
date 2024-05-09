package com.github.chengyuxing.plugin.rabbit.sql.ui.types;

public class DataCell {
    private final Object value;
    private final Object data;

    public DataCell(Object value, Object data) {
        this.value = value;
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
