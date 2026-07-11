package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.function.Consumer;

public class NewPipeForm extends JPanel {
    private JBTextField fullClassName;
    private JBTextField pipeName;
    private ComboBox<String> resultType;
    private InlineHelpText message;
    private Consumer<Pair<String, String>> inputChanged = v -> {
    };
    private final String[] resultTypes = new String[]{
            Object.class.getSimpleName(),
            String.class.getSimpleName(),
            Integer.class.getSimpleName(),
            Long.class.getSimpleName(),
            Double.class.getSimpleName(),
            Float.class.getSimpleName(),
            Boolean.class.getSimpleName(),
    };

    public NewPipeForm() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new MigLayout("insets 0,hidemode 3", "[][grow]", "[][][]"));

        fullClassName = new JBTextField();
        fullClassName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                doInputChange();
                updatePipeNameExample();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                doInputChange();
                updatePipeNameExample();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });
        message = new InlineHelpText("", 250);
        resultType = createComboBox();
        pipeName = new JBTextField();
        pipeName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                doInputChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                doInputChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });

        add(new JBLabel(MessageBundle.message("action.newPipe.form.classNameLabel")));
        add(fullClassName, "growx, wrap");

        add(new JBLabel(MessageBundle.message("action.newPipe.form.resultLabel")));
        add(resultType, "growx, span, wrap, gapleft 4,gapright 5");

        add(new JBLabel(MessageBundle.message("action.newPipe.form.pipeNameLabel")));
        add(pipeName, "growx, wrap");

        add(message, "skip, growx, span, wrap");
    }

    private void updatePipeNameExample() {
        String className = getFullClassName();
        int dotIndex = className.lastIndexOf('.');
        String name = dotIndex == -1 ? className : className.substring(dotIndex + 1);
        if (name.matches("(?i)\\w+pipe$")) {
            name = name.substring(0, name.length() - 4);
        }
        this.pipeName.setText(name.toLowerCase());
    }

    private void doInputChange() {
        inputChanged.accept(Pair.of(fullClassName.getText(), pipeName.getText()));
    }

    private ComboBox<String> createComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setEditable(false);
        for (String result : resultTypes) {
            comboBox.addItem(result);
        }
        comboBox.setSelectedIndex(0);
        return comboBox;
    }

    public String getFullClassName() {
        return fullClassName.getText().trim();
    }

    public String getPipeName() {
        return pipeName.getText().trim();
    }

    public String getResultType() {
        return (String) resultType.getSelectedItem();
    }

    public void setMessage(String text) {
        message.setText(text);
    }

    public void setInputChanged(Consumer<Pair<String, String>> inputChanged) {
        this.inputChanged = inputChanged;
    }
}
