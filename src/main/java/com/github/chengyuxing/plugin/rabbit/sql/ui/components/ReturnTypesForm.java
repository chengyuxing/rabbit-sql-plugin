package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLMapperConfig;
import com.github.chengyuxing.sql.annotation.SqlStatementType;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ReturnTypesForm extends JPanel {
    private final List<JBCheckBox> checkBoxes;
    private int checked = 0;
    private JBTable pageConfigTable;
    private final String sqlType;

    public ReturnTypesForm(String sqlType, XQLMapperConfig.ReturnType selected, Consumer<Integer> checkedCount) {
        this.sqlType = sqlType;
        this.checkBoxes = new ArrayList<>();
        for (var e : MapperGenerateForm.RETURN_TYPES.entrySet()) {
            var type = e.getKey();
            var allows = e.getValue();
            var check = new JBCheckBox(type);
            check.setEnabled(allows.contains(sqlType));
            if (selected.getItems().contains(type)) {
                check.setSelected(true);
                checked++;
            }
            check.addActionListener(a -> {
                if (check.isSelected()) {
                    checked++;
                } else {
                    checked--;
                }
                checkedCount.accept(checked);
            });
            checkBoxes.add(check);
        }
        initComponents(selected.getPageConfig());
    }

    private void initComponents(XQLMapperConfig.PageableConfigProps pageableConfig) {
        setLayout(new MigLayout(
                "insets 0,hidemode 3",
                "[][][][][][grow]",
                "[][][]20[][][][]"));
        var restCheckboxes = checkBoxes;
        for (int i = 0, j = restCheckboxes.size(); i < j; i++) {
            if ((i + 1) % 5 == 0) {
                add(restCheckboxes.get(i), "wrap");
            } else if (i == j - 1) {
                add(restCheckboxes.get(i), "span, wrap");
            } else {
                add(restCheckboxes.get(i));
            }
        }
        add(new TitleLine(MessageBundle.message("ui.dialog.returnType.config.title")), "growx, span, wrap");
        pageConfigTable = new JBTable();
        pageConfigTable.setBorder(BorderFactory.createEmptyBorder());
        pageConfigTable.setShowVerticalLines(false);
        pageConfigTable.setShowHorizontalLines(false);
        pageConfigTable.setRowHeight(30);
        pageConfigTable.setSelectionForeground(null);
        pageConfigTable.setSelectionBackground(null);
        pageConfigTable.setFillsViewportHeight(true);
        var tableScrollPane = new JBScrollPane();
        tableScrollPane.setViewportView(pageConfigTable);
        var model = new DefaultTableModel();
        pageConfigTable.setModel(model);
        model.setDataVector(
                new Object[][]{
                        {
                                pageableConfig.getStartNumKey(),
                                pageableConfig.getEndNumKey(),
                                pageableConfig.getPageHelperClass()
                        }
                },
                new Object[]{"startNumKey", "endNumKey", "pageHelperProvider"}
        );
        pageConfigTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JBTextField()));
        pageConfigTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JBTextField()));
        pageConfigTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JBTextField()));
        pageConfigTable.setEnabled(Objects.equals(sqlType, SqlStatementType.query.name()));
        add(tableScrollPane, "growx, span, wrap");
        add(new InlineHelpText(MessageBundle.message("ui.dialog.returnType.help"), 370), "growx, span, wrap");

    }

    public XQLMapperConfig.ReturnType getSelected() {
        var items = checkBoxes.stream()
                .filter(AbstractButton::isSelected)
                .map(AbstractButton::getText)
                .toList();
        var result = new XQLMapperConfig.ReturnType();
        result.setItems(items);
        result.setPageConfig(getPageConfig());
        return result;
    }

    private XQLMapperConfig.PageableConfigProps getPageConfig() {
        var model = ((DefaultTableModel) pageConfigTable.getModel());
        var data = new XQLMapperConfig.PageableConfigProps();
        data.setStartNumKey(model.getValueAt(0, 0).toString());
        data.setEndNumKey(model.getValueAt(0, 1).toString());
        data.setPageHelperClass(model.getValueAt(0, 2).toString());
        return data;
    }
}
