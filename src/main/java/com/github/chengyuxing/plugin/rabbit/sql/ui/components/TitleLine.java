package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.intellij.ui.components.JBLabel;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class TitleLine extends JPanel {
    public TitleLine(@NotNull String title) {
        setLayout(new MigLayout("insets 0,hidemode 3", "[][grow]"));
        add(new JBLabel(title), "growx");
        var line = new JSeparator();
        line.setMinimumSize(new Dimension(0, 2));
        add(line, "growx, span");
    }
}
