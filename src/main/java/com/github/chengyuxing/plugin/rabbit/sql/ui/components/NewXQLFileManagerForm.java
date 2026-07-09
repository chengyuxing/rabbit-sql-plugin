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
import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.Global;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
import net.miginfocom.swing.MigLayout;

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
        // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
        JLabel title = new JLabel();
        secondaryFileName = new JTextField();
        message = new InlineHelpText("xql-file-manager-*.yml",220);

        secondaryFileName.setFont(Global.getEditorFont(secondaryFileName.getFont().getSize()));

        //======== this ========
        setLayout(new MigLayout("insets 0,hidemode 3", "[][grow]","[][]"));

        //---- title ----
        title.setText(MessageBundle.message("ui.newXqlFileManagerForm.name"));
        add(title);
        add(secondaryFileName, "growx, wrap");

        add(message, "skip, growx, wrap");

        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    private JTextField secondaryFileName;
    private InlineHelpText message;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
