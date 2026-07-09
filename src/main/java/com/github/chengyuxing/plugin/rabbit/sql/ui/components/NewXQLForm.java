/*
 * Created by JFormDesigner on Sun Jan 21 20:37:59 CST 2024
 */

package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.github.chengyuxing.common.tuple.Quadruple;
import com.github.chengyuxing.common.tuple.Triple;
import com.github.chengyuxing.common.tuple.Tuples;
import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.common.Global;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.intellij.ui.components.fields.*;
import com.jgoodies.forms.layout.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author chengyuxing
 */
public class NewXQLForm extends JPanel {
    private final String resourceRoot;
    private Consumer<Triple<String, String, String>> inputChanged = v -> {
    };
    private boolean aliasEditable = true;
    private String defaultAlias = "";

    public NewXQLForm(String resourceRoot) {
        this.resourceRoot = resourceRoot;
        initComponents();
    }

    public Quadruple<String, String, String, String> getData() {
        var userInputPath = filename.getText().trim();
        var abPath = genAbPath(userInputPath);
        if (isYmlListType(userInputPath)) {
            userInputPath = formatYmlArray(userInputPath);
            return Tuples.of(alias.getText(), userInputPath, abPath, description.getText());
        }
        if (userInputPath.startsWith("/")) {
            userInputPath = userInputPath.substring(1);
        }
        if (!userInputPath.endsWith(".xql")) {
            userInputPath += ".xql";
        }
        return Tuples.of(alias.getText(), userInputPath, abPath, description.getText());
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
        if (aliasEditable) {
            alias.setText(data.getItem1());
        }
        message.setText(resourceRoot + "/" + data.getItem2());
        inputChanged.accept(Tuples.of(alias.getText(), data.getItem2(), data.getItem3()));
    }

    void aliasInputChanged() {
        var data = genData();
        message.setText(resourceRoot + "/" + data.getItem2());
        inputChanged.accept(Tuples.of(alias.getText(), data.getItem2(), data.getItem3()));
    }

    public void init() {
        alias.setEditable(aliasEditable);
        alias.setText(defaultAlias);
        message.setText(resourceRoot);
        filename.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                userInputChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                userInputChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });
        if (aliasEditable) {
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
        return filename;
    }

    private String joinPath(String paths) {
        var finalPath = paths;
        if (isYmlListType(paths)) {
            var parts = paths.substring(1, paths.length() - 1).split("\\s*,\\s*");
            finalPath = String.join("/", parts).trim();
        }
        if (!finalPath.endsWith(".xql")) {
            finalPath += ".xql";
        }
        return finalPath;
    }

    public boolean isYmlListType(String s) {
        return s.startsWith("[") && s.endsWith("]");
    }

    private String formatYmlArray(String s) {
        var r = s.trim();
        var paths = r.substring(1, r.length() - 1).split("\\s*,\\s*");
        var path = String.join(", ", paths).trim();
        if (!path.endsWith(".xql")) {
            path += ".xql";
        }
        return "[ " + path + " ]";
    }

    public void setInputChanged(Consumer<Triple<String, String, String>> inputChanged) {
        if (Objects.nonNull(inputChanged))
            this.inputChanged = inputChanged;
    }

    public void setAliasEditable(boolean aliasEditable) {
        this.aliasEditable = aliasEditable;
    }

    public void setDefaultAlias(String defaultAlias) {
        if (Objects.nonNull(defaultAlias))
            this.defaultAlias = defaultAlias;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
        JLabel label1 = new JLabel();
        filename = new JTextField();
        JLabel label2 = new JLabel();
        alias = new JTextField();
        JLabel label3 = new JLabel();
        description = new ExpandableTextField();
        message = new InlineHelpText("...",300);

        filename.setFont(Global.getEditorFont(filename.getFont().getSize()));
        alias.setFont(Global.getEditorFont(alias.getFont().getSize()));
        description.setFont(Global.getEditorFont(description.getFont().getSize()));

        //======== this ========
        setLayout(new MigLayout("insets 0,hidemode 3", "[][grow]","[][][][][]"));

        //---- label1 ----
        label1.setText(MessageBundle.message("ui.newXqlForm.name"));
        add(label1);
        add(filename, "growx, wrap");

        add(new InlineHelpText(MessageBundle.message("ui.newXqlForm.name.tooltip"),300),"skip, growx, wrap");

        //---- label2 ----
        label2.setText(MessageBundle.message("ui.newXqlForm.alias"));
        add(label2);
        add(alias, "growx, wrap");

        //---- label3 ----
        label3.setText(MessageBundle.message("ui.newXqlForm.description"));
        add(label3);
        add(description, "growx, wrap");

        add(message,"skip, growx, span, wrap");

        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    private JTextField filename;
    private JTextField alias;
    private ExpandableTextField description;
    private InlineHelpText message;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
