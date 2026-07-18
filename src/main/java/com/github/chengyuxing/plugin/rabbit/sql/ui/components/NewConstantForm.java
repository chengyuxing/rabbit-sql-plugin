package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.function.Consumer;

public class NewConstantForm extends JPanel {
    private JBTextField name;
    private JBTextField value;
    private InlineHelpText message;
    private Consumer<String> inputChanged = v -> {
    };

    public NewConstantForm() {
        createUIComponents();
    }

    private void createUIComponents() {
        setLayout(new MigLayout("insets 0,hidemode 3", "[][grow]", "[][]"));
        message = new InlineHelpText(MessageBundle.message("ui.dialog.newConstant.message", ""), 250);
        name = new JBTextField();
        name.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                inputChanged.accept(name.getText());
                updateMessage();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                inputChanged.accept(name.getText());
                updateMessage();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });
        value = new JBTextField();

        add(new JBLabel(MessageBundle.message("action.newConstant.form.nameLabel")));
        add(name, "growx, wrap");
        add(new JBLabel(MessageBundle.message("action.newConstant.form.valueLabel")));
        add(value, "growx, wrap");
        add(message, "skip, growx, span, wrap");
    }

    private void updateMessage() {
        message.setText(MessageBundle.message("ui.dialog.newConstant.message", name.getText().trim()));
    }

    public void setInputChanged(Consumer<String> inputChanged) {
        this.inputChanged = inputChanged;
    }

    public String getName() {
        return name.getText().trim();
    }

    public String getValue() {
        return value.getText().trim();
    }
}
