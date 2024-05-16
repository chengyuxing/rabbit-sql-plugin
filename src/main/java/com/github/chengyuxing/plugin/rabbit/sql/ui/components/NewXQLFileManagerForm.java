/*
 * Created by JFormDesigner on Mon Apr 29 19:11:48 CST 2024
 */

package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import java.awt.*;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.intellij.ui.JBColor;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

/**
 * @author chengyuxing
 */
public class NewXQLFileManagerForm extends JPanel {
    private Consumer<String> inputChanged = v -> {
    };

    public NewXQLFileManagerForm() {
        initComponents();
        initComponentsCustom();
    }

    public String getSecondaryFileName() {
        return secondaryFileName.getText();
    }

    public void setMessage(String message) {
        this.message.setText(message);
    }

    private void updateMessage() {
        var configName = FileResource.getFileName(Constants.CONFIG_NAME, false);
        var secondaryFilename = configName + "-" + getSecondaryFileName() + ".yml";
        message.setText(secondaryFilename);
        inputChanged.accept(getSecondaryFileName());
    }

    public void setInputChanged(Consumer<String> inputChanged) {
        this.inputChanged = inputChanged;
    }

    private void initComponentsCustom() {
        secondaryFileName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateMessage();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateMessage();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        title = new JLabel();
        secondaryFileName = new JTextField();
        panel1 = new JPanel();
        message = new JLabel();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setMinimumSize(new Dimension(200, 55));
        setPreferredSize(new Dimension(200, 55));
        setLayout(new FormLayout(
            new ColumnSpec[] {
                new ColumnSpec(Sizes.dluX(25)),
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                new ColumnSpec(ColumnSpec.FILL, Sizes.dluX(50), FormSpec.DEFAULT_GROW)
            },
            RowSpec.decodeSpecs("default, default")));

        //---- title ----
        title.setText("Name:");
        add(title, cc.xy(1, 1));
        add(secondaryFileName, cc.xy(3, 1));

        //======== panel1 ========
        {
            panel1.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 1));

            //---- message ----
            message.setText("xql-file-manager-*.yml");
            message.setForeground(new JBColor(new Color(0x7A7A7A), new Color(0x727782)));
            message.setFont(message.getFont().deriveFont(message.getFont().getSize() - 1f));
            panel1.add(message);
        }
        add(panel1, cc.xy(3, 2, CellConstraints.FILL, CellConstraints.DEFAULT));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JLabel title;
    private JTextField secondaryFileName;
    private JPanel panel1;
    private JLabel message;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
