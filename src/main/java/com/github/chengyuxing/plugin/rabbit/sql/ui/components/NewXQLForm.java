/*
 * Created by JFormDesigner on Sun Jan 21 20:37:59 CST 2024
 */

package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.github.chengyuxing.common.tuple.Quadruple;
import com.github.chengyuxing.common.tuple.Triple;
import com.github.chengyuxing.common.tuple.Tuples;
import com.github.chengyuxing.plugin.rabbit.sql.common.Global;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.fields.*;
import com.intellij.util.ui.JBUI;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Consumer;

/**
 * @author chengyuxing
 */
public class NewXQLForm extends JPanel {
    private final String resourceRoot;
    private Map<String, String> anchors = Map.of();
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
        if (!anchors.isEmpty()) {
            var sb = new StringJoiner(", ");
            anchors.forEach((k, v) -> sb.add(k + "=" + v));
            anchorTag.setToolTipText(sb.toString());
            anchorTag.setVisible(true);
        }
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

    public void setAnchors(Map<String, String> anchors) {
        if (Objects.nonNull(anchors))
            this.anchors = anchors;
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
        label1 = new JLabel();
        filename = new JTextField();
        panel2 = new JPanel();
        filenameTooltip = new JLabel();
        anchorTag = new JLabel();
        label2 = new JLabel();
        alias = new JTextField();
        label3 = new JLabel();
        description = new ExpandableTextField();
        panel1 = new JPanel();
        message = new JLabel();
        CellConstraints cc = new CellConstraints();

        filename.setFont(Global.getEditorFont(filename.getFont().getSize()));
        alias.setFont(Global.getEditorFont(alias.getFont().getSize()));
        description.setFont(Global.getEditorFont(description.getFont().getSize()));

        //======== this ========
        setPreferredSize(new Dimension(500, 160));
        setLayout(new FormLayout(
            new ColumnSpec[] {
                new ColumnSpec(Sizes.dluX(41)),
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
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.LINE_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC
            }));

        //---- label1 ----
        label1.setText("File name:");
        add(label1, cc.xy(1, 1));
        add(filename, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.DEFAULT));

        //======== panel2 ========
        {
            panel2.setLayout(new FormLayout(
                new ColumnSpec[] {
                    new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, 0.01),
                    FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                    new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, 0.01)
                },
                RowSpec.decodeSpecs("fill:default:grow(0.01)")));

            //---- filenameTooltip ----
            filenameTooltip.setText("Divided by '/' or array e.g. [a, b, c]");
            filenameTooltip.setVerticalAlignment(SwingConstants.TOP);
            filenameTooltip.setFont(filenameTooltip.getFont().deriveFont(filenameTooltip.getFont().getSize() - 1f));
            filenameTooltip.setForeground(InlineHelpText.COLOR);
            panel2.add(filenameTooltip, cc.xy(1, 1, CellConstraints.LEFT, CellConstraints.CENTER));

            //---- anchorTag ----
            anchorTag.setText("[Anchors]");
            anchorTag.setHorizontalAlignment(SwingConstants.TRAILING);
            anchorTag.setFont(anchorTag.getFont().deriveFont(anchorTag.getFont().getSize() - 1f));
            anchorTag.setForeground(new JBColor(new Color(0x48a0a2), new Color(0x1D7FC5)));
            anchorTag.setVisible(false);
            panel2.add(anchorTag, cc.xy(3, 1, CellConstraints.RIGHT, CellConstraints.CENTER));
        }
        add(panel2, new CellConstraints(3, 3, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, JBUI.insets(0, 4)));

        //---- label2 ----
        label2.setText("Alias:");
        add(label2, cc.xy(1, 5));
        add(alias, cc.xy(3, 5, CellConstraints.FILL, CellConstraints.DEFAULT));

        //---- label3 ----
        label3.setText("Description:");
        add(label3, cc.xy(1, 7));
        add(description, cc.xy(3, 7));

        //======== panel1 ========
        {
            panel1.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 1));

            //---- message ----
            message.setText("...");
            message.setFont(message.getFont().deriveFont(message.getFont().getSize() - 1f));
            message.setForeground(InlineHelpText.COLOR);
            panel1.add(message);
        }
        add(panel1, cc.xy(3, 9, CellConstraints.FILL, CellConstraints.DEFAULT));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JLabel label1;
    private JTextField filename;
    private JPanel panel2;
    private JLabel filenameTooltip;
    private JLabel anchorTag;
    private JLabel label2;
    private JTextField alias;
    private JLabel label3;
    private ExpandableTextField description;
    private JPanel panel1;
    private JLabel message;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
