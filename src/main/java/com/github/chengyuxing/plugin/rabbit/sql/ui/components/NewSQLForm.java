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
import com.github.chengyuxing.plugin.rabbit.sql.common.Global;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

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
        abc = new JLabel();
        name = new JTextField();
        def = new JLabel();
        description = new ExpandableTextField();
        panel1 = new JPanel();
        message = new JLabel();
        CellConstraints cc = new CellConstraints();

        name.setFont(Global.getEditorFont(name.getFont().getSize()));
        description.setFont(Global.getEditorFont(name.getFont().getSize()));

        //======== this ========
        setMinimumSize(new Dimension(350, 103));
        setPreferredSize(new Dimension(350, 103));
        setLayout(new FormLayout(
            new ColumnSpec[] {
                new ColumnSpec(Sizes.dluX(40)),
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                new ColumnSpec(ColumnSpec.FILL, Sizes.dluX(50), FormSpec.DEFAULT_GROW)
            },
            RowSpec.decodeSpecs("default, 4dlu, min, 3dlu, default")));

        //---- abc ----
        abc.setText("Name:");
        add(abc, cc.xy(1, 1));
        add(name, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.DEFAULT));

        //---- def ----
        def.setText("Description:");
        add(def, cc.xy(1, 3));
        add(description, cc.xy(3, 3, CellConstraints.FILL, CellConstraints.DEFAULT));

        //======== panel1 ========
        {
            panel1.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 1));

            //---- message ----
            message.setFont(message.getFont().deriveFont(message.getFont().getSize() - 1f));
            message.setText("Append a sql fragment to the end.");
            message.setForeground(new JBColor(new Color(0x7A7A7A), new Color(0x727782)));
            panel1.add(message);
        }
        add(panel1, cc.xy(3, 5));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JLabel abc;
    private JTextField name;
    private JLabel def;
    private ExpandableTextField description;
    private JPanel panel1;
    private JLabel message;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
