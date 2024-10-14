package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class DynamicSqlParamValueHistoryDialog extends DialogWrapper {
    private final Consumer<String> consumer;
    private final JBList<String> list;
    private final Runnable clearButtonCallback;

    protected DynamicSqlParamValueHistoryDialog(List<String> histories, Consumer<String> consumer, Runnable clearButtonCallback) {
        super(true);
        this.list = new JBList<>(histories);
        this.consumer = consumer;
        this.clearButtonCallback = clearButtonCallback;
        setOKButtonText("Insert");
        setCancelButtonText("Cancel");
        setSize(350, Math.min(Math.max(histories.size() * 30 + 50, 230), 470));
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    consumer.accept(list.getSelectedValue());
                    dispose();
                }
            }
        });
        var scrollPane = new JBScrollPane();
        scrollPane.setBorder(new LineBorder(new JBColor(new Color(0xD2D2D2), new Color(0x323232))));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setViewportView(list);
        return scrollPane;
    }

    @Override
    protected @Nullable JPanel createSouthAdditionalPanel() {
        var panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        var clearBtn = new JButton("Clear");
        clearBtn.setToolTipText("Clear history.");
        clearBtn.addActionListener(e -> {
            list.clearSelection();
            clearButtonCallback.run();
        });
        panel.add(clearBtn);
        return panel;
    }

    @Override
    protected void doOKAction() {
        var selectedValue = list.getSelectedValue();
        if (Objects.nonNull(selectedValue)) {
            consumer.accept(list.getSelectedValue());
        }
        dispose();
    }
}
