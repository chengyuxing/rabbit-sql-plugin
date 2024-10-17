package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;

public class InlineHelpText extends JPanel {
    public static final Color COLOR = new JBColor(new Color(0x7A7A7A), new Color(0x727782));
    private final String text;
    private int hgap = 4;

    public InlineHelpText(String text) {
        this.text = text;
        init();
    }

    private void init() {
        setLayout(new FlowLayout(FlowLayout.LEFT, hgap, 0));
        var label = new JLabel(text);
        label.setText("<html>" + text + "</html>");
        label.setFont(getFont().deriveFont(getFont().getSize() - 1f));
        label.setForeground(COLOR);
        label.setVerticalAlignment(SwingConstants.TOP);
        add(label);
    }

    public void setHgap(int hgap) {
        this.hgap = hgap;
    }
}
