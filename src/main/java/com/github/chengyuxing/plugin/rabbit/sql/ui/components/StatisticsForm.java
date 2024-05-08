/*
 * Created by JFormDesigner on Wed May 08 21:07:36 CST 2024
 */

package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.*;
import net.miginfocom.swing.*;
import org.jdesktop.swingx.*;

/**
 * @author chengyuxing
 */
public class StatisticsForm extends JPanel {
    private final Map<Path, Set<XQLConfigManager.Config>> configMap;

    public StatisticsForm(Map<Path, Set<XQLConfigManager.Config>> configMap) {
        this.configMap = configMap;
        initComponents();
        init();
    }

    public void init() {
        scrollPanel.setBorder(BorderFactory.createEmptyBorder());
        configMap.forEach((path, configs) -> {
            var module = path.getFileName().toString();
            var panel = new JPanel();
            panel.setLayout(new MigLayout(
                    "fillx,insets 0,hidemode 3,align center center,gap 5 5",
                    // columns
                    "[grow,left]",
                    // rows
                    "[fill][fill]"));
            var moduleCom = new TitledSeparator(module);
            var tablePanel = new JBScrollPane();
            tablePanel.setBorder(new LineBorder(new JBColor(new Color(0xD2D2D2), new Color(0x323232))));
            var table = new JBTable();
            table.setBorder(BorderFactory.createEmptyBorder());
            table.setShowVerticalLines(false);
            table.setShowLastHorizontalLine(false);
            table.setRowHeight(30);
            table.setSelectionForeground(null);
            table.setSelectionBackground(null);
            table.setFillsViewportHeight(true);
            table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            table.setIntercellSpacing(new Dimension(0, 0));
            var thead = new Object[]{"Config", "Total XQL Files", "Total SQLs", "Total Lines", "Total Size"};
            var tbody = configs.stream()
                    .filter(XQLConfigManager.Config::isValid)
                    .filter(config -> Objects.nonNull(config.getXqlFileManager()))
                    .map(config -> {
                        var xqlFileManager = config.getXqlFileManager();
                        long totalLines = 0;
                        long totalSize = 0;
                        for (var file : xqlFileManager.getFiles().values()) {
                            var filePath = Path.of(URI.create(file));
                            totalLines += ProjectFileUtil.lineNumber(filePath);
                            try {
                                totalSize += Files.size(filePath);
                            } catch (IOException ignored) {

                            }
                        }
                        return new Object[]{
                                config.getConfigName(),
                                xqlFileManager.getFiles().size(),
                                xqlFileManager.size(),
                                totalLines,
                                ProjectFileUtil.formatFileSize(totalSize)
                        };
                    }).toArray(i -> new Object[i][5]);
            var model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            table.setModel(model);
            model.setDataVector(tbody, thead);
            table.getColumnModel().getColumn(0).setPreferredWidth(130);
            panel.add(moduleCom, "cell 0 0,growx");
            tablePanel.setViewportView(table);
            panel.add(tablePanel, "cell 0 1,growx");
            container.add(panel);
        });
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        scrollPanel = new JBScrollPane();
        container = new JPanel();

        //======== this ========
        setPreferredSize(new Dimension(600, 220));
        setLayout(new MigLayout(
            "fill,insets 0,hidemode 3,align center center",
            // columns
            "[grow,left]",
            // rows
            "[fill]"));

        //======== scrollPanel ========
        {

            //======== container ========
            {
                container.setLayout(new VerticalLayout(15));
            }
            scrollPanel.setViewportView(container);
        }
        add(scrollPanel, "cell 0 0,grow");
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JBScrollPane scrollPanel;
    private JPanel container;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
