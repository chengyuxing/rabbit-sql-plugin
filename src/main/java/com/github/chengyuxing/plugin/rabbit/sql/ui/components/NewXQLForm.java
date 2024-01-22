/*
 * Created by JFormDesigner on Sun Jan 21 20:37:59 CST 2024
 */

package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.github.chengyuxing.common.tuple.Triple;
import com.github.chengyuxing.common.tuple.Tuples;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.intellij.ui.JBColor;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.StringJoiner;
import java.util.function.Consumer;

/**
 * @author chengyuxing
 */
public class NewXQLForm extends JPanel {
    private final String resourceRoot;

    private final Consumer<Triple<String, String, String>> inputChanged;

    public NewXQLForm(String resourceRoot, Consumer<Triple<String, String, String>> inputChanged) {
        this.resourceRoot = resourceRoot;
        this.inputChanged = inputChanged;
        initComponents();
        customInit();
    }

    public Triple<String, String, String> getData() {
        var userInputPath = filename.getText().trim();
        var abPath = genAbPath(userInputPath);
        if (isYmlListType(userInputPath)) {
            userInputPath = "!path [ " + String.join(", ", abPath.split("/")) + " ]";
            return Tuples.of(alias.getText(), userInputPath, abPath);
        }
        if (userInputPath.startsWith("/")) {
            userInputPath = userInputPath.substring(1);
        }
        if (!userInputPath.endsWith(".xql")) {
            userInputPath += ".xql";
        }
        return Tuples.of(alias.getText(), userInputPath, abPath);
    }

    /**
     * [alias, userInput, resolvedInput]
     *
     * @return [alias, userInput, resolvedInput]
     */
    Triple<String, String, String> genData() {
        var userInputPath = filename.getText().trim();
        var abPath = genAbPath(userInputPath);
        var abAlias = genAlias(abPath);
        return Tuples.of(abAlias, abPath, userInputPath);
    }

    public void alert(String text) {
        message.setText(HtmlUtil.toHtml(HtmlUtil.span(text, HtmlUtil.Color.WARNING)));
    }

    void userInputChanged() {
        var data = genData();
        alias.setText(data.getItem1());
        message.setText(resourceRoot + "/" + data.getItem2());
        inputChanged.accept(Tuples.of(alias.getText(), data.getItem2(), data.getItem3()));
    }

    void aliasInputChanged() {
        var data = genData();
        message.setText(resourceRoot + "/" + data.getItem2());
        inputChanged.accept(Tuples.of(alias.getText(), data.getItem2(), data.getItem3()));
    }

    private void customInit() {
        message.setText(resourceRoot);
        filename.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                userInputChanged();
                alias.setEditable(true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                userInputChanged();
                if (filename.getText().trim().isEmpty())
                    alias.setEditable(false);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });
        alias.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aliasInputChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aliasInputChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });
    }

    private String genAlias(String text) {
        var alias = text;
        if (alias.isEmpty()) {
            return "";
        }
        alias = joinPath(alias);
        if (alias.startsWith("/")) {
            alias = alias.substring(1);
        }
        int sep = alias.lastIndexOf("/");
        if (sep != -1) {
            alias = alias.substring(sep + 1);
        }
        if (alias.endsWith(".xql")) {
            alias = alias.substring(0, alias.length() - 4);
        }
        return alias;
    }

    private String genAbPath(String text) {
        var filename = text;
        if (filename.isEmpty()) {
            return "";
        }
        filename = joinPath(filename);
        if (filename.startsWith("/")) {
            filename = filename.substring(1);
        }
        if (filename.endsWith(".xql")) {
            return filename;
        }
        return filename + ".xql";
    }

    private String joinPath(String paths) {
        if (isYmlListType(paths)) {
            var parts = paths.substring(1, paths.length() - 1).split(",");
            var sb = new StringJoiner("/");
            for (var part : parts) {
                sb.add(part.trim());
            }
            return sb.toString();
        }
        return paths;
    }

    private boolean isYmlListType(String s) {
        return s.startsWith("[") && s.endsWith("]");
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        label1 = new JLabel();
        filename = new JTextField();
        panel2 = new JPanel();
        filenameTooltip = new JLabel();
        label2 = new JLabel();
        alias = new JTextField();
        panel1 = new JPanel();
        message = new JLabel();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setMinimumSize(new Dimension(350, 108));
        setPreferredSize(new Dimension(350, 108));
        setLayout(new FormLayout(
            new ColumnSpec[] {
                new ColumnSpec(Sizes.dluX(35)),
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                new ColumnSpec(ColumnSpec.FILL, Sizes.dluX(50), FormSpec.DEFAULT_GROW)
            },
            new RowSpec[] {
                FormFactory.DEFAULT_ROWSPEC,
                new RowSpec(Sizes.DLUY1),
                FormFactory.MIN_ROWSPEC,
                new RowSpec(Sizes.DLUY4),
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.LINE_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC
            }));

        //---- label1 ----
        label1.setText("File Name:");
        add(label1, cc.xy(1, 1));
        add(filename, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.DEFAULT));

        //======== panel2 ========
        {
            panel2.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));

            //---- filenameTooltip ----
            filenameTooltip.setText("Path by '/' or Array e.g [xqls, mysql, home.xql]");
            filenameTooltip.setVerticalAlignment(SwingConstants.TOP);
            filenameTooltip.setFont(filenameTooltip.getFont().deriveFont(filenameTooltip.getFont().getSize() - 1f));
            filenameTooltip.setForeground(new JBColor(new Color(0x7A7A7A), new Color(0x727782)));
            panel2.add(filenameTooltip);
        }
        add(panel2, cc.xy(3, 3, CellConstraints.FILL, CellConstraints.DEFAULT));

        //---- label2 ----
        label2.setText("Alias:");
        add(label2, cc.xy(1, 5));

        //---- alias ----
        alias.setEditable(false);
        add(alias, cc.xy(3, 5, CellConstraints.FILL, CellConstraints.DEFAULT));

        //======== panel1 ========
        {
            panel1.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 1));

            //---- message ----
            message.setText("...");
            message.setForeground(new JBColor(new Color(0x7A7A7A), new Color(0x727782)));
            message.setFont(message.getFont().deriveFont(message.getFont().getSize() - 1f));
            panel1.add(message);
        }
        add(panel1, cc.xy(3, 7, CellConstraints.FILL, CellConstraints.DEFAULT));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JLabel label1;
    private JTextField filename;
    private JPanel panel2;
    private JLabel filenameTooltip;
    private JLabel label2;
    private JTextField alias;
    private JPanel panel1;
    private JLabel message;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
