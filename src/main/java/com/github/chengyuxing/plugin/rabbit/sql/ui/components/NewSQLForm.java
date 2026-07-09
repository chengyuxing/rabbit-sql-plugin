/*
 * Created by JFormDesigner on Mon May 06 15:13:34 CST 2024
 */

package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import java.awt.*;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.common.Global;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
import net.miginfocom.swing.MigLayout;

/**
 * @author chengyuxing
 */
public class NewSQLForm extends JPanel {
    private Consumer<String> inputChanged = v -> {
    };

    public NewSQLForm() {
        initComponents();
        customInit();
    }

    public Pair<String, String> getData() {
        return Pair.of(name.getText(), description.getText());
    }

    private void customInit() {
        name.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                inputChanged.accept(name.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                inputChanged.accept(name.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });
    }

    public void setMessage(String message) {
        this.message.setText(message);
    }

    public void setInputChanged(Consumer<String> inputChanged) {
        this.inputChanged = inputChanged;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
        JLabel abc = new JLabel();
        name = new JTextField();
        JLabel def = new JLabel();
        description = new ExpandableTextField();
        message = new InlineHelpText(MessageBundle.message("ui.newSqlForm.message"),250);

        name.setFont(Global.getEditorFont(name.getFont().getSize()));
        description.setFont(Global.getEditorFont(description.getFont().getSize()));

        //======== this ========
        setLayout(new MigLayout("insets 0,hidemode 3", "[][grow]","[][]"));

        //---- abc ----
        abc.setText(MessageBundle.message("ui.newSqlForm.name"));
        add(abc);
        add(name, "growx, wrap");

        //---- def ----
        def.setText(MessageBundle.message("ui.newSqlForm.description"));
        add(def);
        add(description, "growx, wrap");

        add(message, "skip, growx, span, wrap");

    }

    private JTextField name;
    private ExpandableTextField description;
    private InlineHelpText message;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
