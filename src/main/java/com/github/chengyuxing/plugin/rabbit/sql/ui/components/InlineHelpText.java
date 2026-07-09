package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;

public class InlineHelpText extends JPanel {
    public static final Color COLOR = new JBColor(new Color(0x7A7A7A), new Color(0x727782));
    private final JLabel label;
    private int width = 330;

    public InlineHelpText(String text, int width) {
        this.width = width;
        this.label = new JLabel(richText(text));
        init();
    }

    public InlineHelpText(String text) {
        this.label = new JLabel(richText(text));
        init();
    }

    private void init() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
        label.setFont(getFont().deriveFont(getFont().getSize() - 1f));
        label.setForeground(COLOR);
        label.setVerticalAlignment(SwingConstants.TOP);
        add(label);
    }

    private String richText(String text) {
        return "<html><body style='width:" + width + "px';word-break:break-all;>" + text + "</body></html>";
    }

    public void setText(String text) {
        label.setText(richText(text));
    }
}
