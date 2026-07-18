package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.function.Consumer;

public class ModifyPropertyForm extends JPanel {
    private JBTextField value;
    private Consumer<String> inputChanged = v -> {
    };

    public ModifyPropertyForm() {
        createUIComponents();
    }

    private void createUIComponents() {
        setLayout(new MigLayout("insets 0,hidemode 3", "[][grow]", "[][]"));
        InlineHelpText message = new InlineHelpText("", 250);
        value = new JBTextField();
        value.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                inputChanged.accept(value.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                inputChanged.accept(value.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });

        add(new JBLabel(MessageBundle.message("ui.dialog.modify.valueLabel")));
        add(value, "growx, wrap");
        add(message, "skip, growx, wrap");
    }

    public void setInputChanged(Consumer<String> inputChanged) {
        this.inputChanged = inputChanged;
    }

    public String getValue() {
        return value.getText().trim();
    }
}
