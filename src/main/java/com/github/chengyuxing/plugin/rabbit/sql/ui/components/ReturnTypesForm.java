package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.intellij.ui.components.JBCheckBox;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ReturnTypesForm extends JPanel {
    private final List<JBCheckBox> checkBoxes;
    private int checked = 0;

    public ReturnTypesForm(String selected, Consumer<Integer> checkedCount) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));
        setMinimumSize(new Dimension(360, 105));

        this.checkBoxes = new ArrayList<>();
        var values = Arrays.asList(selected.split(" & "));
        for (String type : MapperGenerateForm.RETURN_TYPES) {
            var check = new JBCheckBox(type);
            if (values.contains(type)) {
                check.setSelected(true);
                checked++;
            }
            check.addActionListener(e -> {
                if (check.isSelected()) {
                    checked++;
                } else {
                    checked--;
                }
                checkedCount.accept(checked);
            });

            checkBoxes.add(check);
            add(check);
        }
    }

    public String getSelected() {
        return checkBoxes.stream()
                .filter(AbstractButton::isSelected)
                .map(AbstractButton::getText)
                .collect(Collectors.joining(" & "));
    }
}
