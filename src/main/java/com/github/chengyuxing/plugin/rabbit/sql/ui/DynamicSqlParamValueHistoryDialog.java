package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.github.chengyuxing.plugin.rabbit.sql.common.Global;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.FixedSizeButton;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import net.miginfocom.swing.MigLayout;
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
    private JBList<String> list;
    private final List<String> histories;

    public DynamicSqlParamValueHistoryDialog(List<String> histories, Consumer<String> consumer) {
        super(true);
        this.consumer = consumer;
        this.histories = histories;
        setTitle("History");
        setOKButtonText("Insert");
        setCancelButtonText("Cancel");
        setSize(350, Math.min(Math.max(histories.size() * 30 + 50, 230), 470));
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        list = new JBList<>(histories);
        list.setFont(Global.getEditorFont(list.getFont().getSize() + 1));
        list.setEmptyText("No history.");
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
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        var clearBtn = new FixedSizeButton();
        clearBtn.setIcon(AllIcons.Actions.GC);
        clearBtn.setToolTipText("Clear All.");
        clearBtn.addActionListener(e -> {
            list.setModel(new DefaultListModel<>());
            histories.clear();
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
