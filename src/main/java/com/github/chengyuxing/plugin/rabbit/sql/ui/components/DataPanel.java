package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import javax.swing.*;

public class DataPanel<T> extends JPanel {
    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
