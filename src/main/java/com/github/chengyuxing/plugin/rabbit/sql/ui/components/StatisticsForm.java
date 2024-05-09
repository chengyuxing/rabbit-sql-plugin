/*
 * Created by JFormDesigner on Wed May 08 21:07:36 CST 2024
 */

package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import com.github.chengyuxing.common.MostDateTime;
import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.renderer.LinkCellRender;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.DataCell;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.*;
import net.miginfocom.swing.*;
import org.jdesktop.swingx.*;
import org.jetbrains.annotations.NotNull;

/**
 * @author chengyuxing
 */
public class StatisticsForm extends JPanel {
    private final Map<Path, Set<XQLConfigManager.Config>> configMap;
    private final Map<JBTable, Set<XQLConfigManager.Config>> dataMap = new LinkedHashMap<>();

    private static final Object[] summaryTableHeader = new Object[]{"Config", "Total XQL Files", "Total SQLs", "Total Lines", "Total Size"};
    private static final Object[] detailsTableHeader = new Object[]{"File Name", "Alias", "SQLs", "Lines", "Size", "Last Modified"};

    public StatisticsForm(Map<Path, Set<XQLConfigManager.Config>> configMap) {
        this.configMap = configMap;
        initComponents();
        customInitComponents();
        initTableDatasource();
    }

    private void initTableData(JBTable table, Set<XQLConfigManager.Config> configs) {
        var tbody = configs.stream().map(config -> {
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
            var sqlSize = xqlFileManager.getFiles().size();
            return new Object[]{
                    config.getConfigName(),
                    sqlSize > 0 ? new DataCell(sqlSize, xqlFileManager) : sqlSize,
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
        model.setDataVector(tbody, summaryTableHeader);
        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        table.getColumnModel().getColumn(1).setCellRenderer(new LinkCellRender());
    }

    private void initTableDatasource() {
        dataMap.forEach(this::initTableData);
    }

    private void customInitComponents() {
        scrollPanel.setBorder(BorderFactory.createEmptyBorder());
        configMap.forEach((path, configs) -> {
            var validConfigs = configs.stream()
                    .filter(XQLConfigManager.Config::isValid)
                    .filter(config -> Objects.nonNull(config.getXqlFileManager()))
                    .collect(Collectors.toSet());
            if (!validConfigs.isEmpty()) {
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
                tablePanel.setMinimumSize(new Dimension(0, 60));
                var table = createTable();
                dataMap.put(table, validConfigs);
                panel.add(moduleCom, "cell 0 0,growx");
                tablePanel.setViewportView(table);
                panel.add(tablePanel, "cell 0 1,growx");
                container.add(panel);
            }
        });
    }

    private @NotNull JBTable createTable() {
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
        table.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    var x = table.rowAtPoint(e.getPoint());
                    var y = table.columnAtPoint(e.getPoint());
                    if (x >= 0 && y >= 0) {
                        var data = table.getModel();
                        var value = data.getValueAt(x, y);
                        if (value instanceof DataCell dataCell) {
                            var source = dataCell.getData();
                            if (source instanceof XQLFileManager xqlFileManager) {
                                var tbody = xqlFileManager.getResources().keySet().stream()
                                        .map(alias -> {
                                            var resource = xqlFileManager.getResources().get(alias);
                                            var filePath = resource.getFilename();
                                            var filename = FileResource.getFileName(filePath, true);
                                            var lines = ProjectFileUtil.lineNumber(Path.of(URI.create(filePath)));
                                            var fileLength = 0L;
                                            var lastModified = "--";
                                            try {
                                                fileLength = Files.size(Path.of(URI.create(filePath)));
                                                var fileModifiedDate = Files.getLastModifiedTime(Path.of(URI.create(filePath))).toInstant();
                                                lastModified = MostDateTime.of(fileModifiedDate).toString("yyyy/MM/dd HH:mm:ss");
                                            } catch (IOException ignored) {

                                            }
                                            var size = ProjectFileUtil.formatFileSize(fileLength);
                                            return new Object[]{
                                                    new DataCell(filename, table),
                                                    alias,
                                                    resource.getEntry().size(),
                                                    lines,
                                                    size,
                                                    lastModified
                                            };
                                        }).toArray(i -> new Object[i][6]);
                                var model = new DefaultTableModel() {
                                    @Override
                                    public boolean isCellEditable(int row, int column) {
                                        return false;
                                    }
                                };
                                table.setModel(model);
                                model.setDataVector(tbody, detailsTableHeader);
                                table.getColumnModel().getColumn(0).setCellRenderer(new LinkCellRender());
                                return;
                            }
                            if (source instanceof JBTable jbTable) {
                                var configs = dataMap.get(jbTable);
                                initTableData(table, configs);
                            }
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        return table;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        scrollPanel = new JBScrollPane();
        container = new JPanel();

        //======== this ========
        setPreferredSize(new Dimension(600, 320));
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
