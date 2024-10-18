package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.intellij.ui.components.JBCheckBox;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ReturnTypesForm extends JPanel {
    private static final String SYMBOL = "&";
    private final List<JBCheckBox> checkBoxes;
    private int checked = 0;

    public static List<String> splitReturnTypes(String returnType) {
        if (StringUtils.isEmpty(returnType)) {
            return List.of();
        }
        return Arrays.stream(returnType.split("\\s*" + SYMBOL + "\\s*"))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public ReturnTypesForm(String selected, Consumer<Integer> checkedCount) {
        setLayout(new GridLayout(4, 3));

        this.checkBoxes = new ArrayList<>();
        var values = splitReturnTypes(selected);
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
                .collect(Collectors.joining(" " + SYMBOL + " "));
    }
}
