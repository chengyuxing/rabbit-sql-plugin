/*
 * Created by JFormDesigner on Wed May 08 21:07:36 CST 2024
 */

package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.github.chengyuxing.common.MostDateTime;
import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.common.tuple.Triple;
import com.github.chengyuxing.common.tuple.Tuples;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.renderer.LinkCellRender;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.DataCell;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.*;
import net.miginfocom.swing.*;
import org.jetbrains.annotations.NotNull;

/**
 * @author chengyuxing
 */
public class StatisticsForm extends JPanel {
    private final Map<Path, Set<XQLConfigManager.Config>> configMap;
    // (table, (module, configs))
    private final Map<JBTable, Pair<Path, List<XQLConfigManager.Config>>> dataMap = new LinkedHashMap<>();

    private static final Object[] summaryTableHeader = new Object[]{"Config", "Total XQL Files", "Total SQLs", "Total Lines", "Total Size"};
    private static final Object[] detailsTableHeader = new Object[]{"File Name", "Alias", "SQLs", "Lines", "Size", "Last Modified"};

    private Consumer<Path> clickEmptyTableTextLink = (module) -> {
    };

    public StatisticsForm(Map<Path, Set<XQLConfigManager.Config>> configMap) {
        this.configMap = configMap;
        initComponents();
        customInitComponents();
        initTableDatasource();
    }

    @SuppressWarnings("rawtypes")
    public Triple<String, ArrayList<String>, Vector<Vector>> getDisplayData() {
        var tabIndex = tabPane.getSelectedIndex();
        if (tabIndex == -1) {
            return null;
        }
        int i = 0;
        for (var e : dataMap.entrySet()) {
            if (tabIndex == i) {
                var table = e.getKey();
                var model = (DefaultTableModel) table.getModel();
                var headerColumn = table.getTableHeader().getColumnModel().getColumns();
                var header = new ArrayList<String>();
                while (headerColumn.hasMoreElements()) {
                    header.add(headerColumn.nextElement().getHeaderValue().toString());
                }
                var configs = dataMap.get(table);
                if (!configs.getItem2().isEmpty()) {
                    var module = configs.getItem2().get(0).getModuleName();
                    return Tuples.of(module, header, model.getDataVector());
                }
            }
            i++;
        }
        return null;
    }

    private void initTableData(JBTable table, Pair<Path, List<XQLConfigManager.Config>> pairConfig) {
        var tbody = pairConfig.getItem2().stream().map(config -> {
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
                    StringUtil.formatFileSize(totalSize)
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
    }

    private void initTableDatasource() {
        dataMap.forEach(this::initTableData);
    }

    private void customInitComponents() {
        configMap.forEach((path, configs) -> {
            var validConfigs = configs.stream()
                    .filter(XQLConfigManager.Config::isValid)
                    .filter(config -> Objects.nonNull(config.getXqlFileManager()))
                    .collect(Collectors.toList());
            var module = path.getFileName().toString();
            var tablePanel = new JBScrollPane();
            tablePanel.setBorder(BorderFactory.createEmptyBorder());
            var table = createTable();
            dataMap.put(table, Pair.of(path, validConfigs));
            tablePanel.setViewportView(table);
            tabPane.addTab(module, AllIcons.Nodes.Module, tablePanel);
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
        table.setDefaultRenderer(Object.class, new LinkCellRender());
        table.getEmptyText().setText("No XQLFileManager configs added.");
        table.getEmptyText().appendSecondaryText("Add XQLFileManager config", SimpleTextAttributes.LINK_PLAIN_ATTRIBUTES, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                var tabIndex = tabPane.getSelectedIndex();
                if (tabIndex == -1) {
                    return;
                }
                int i = 0;
                for (var pairConfig : dataMap.values()) {
                    if (tabIndex == i) {
                        clickEmptyTableTextLink.accept(pairConfig.getItem1());
                        return;
                    }
                    i++;
                }
            }
        });
        table.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    var x = table.rowAtPoint(e.getPoint());
                    var y = table.columnAtPoint(e.getPoint());
                    if (x >= 0 && y >= 0) {
                        var value = table.getValueAt(x, y);
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
                                            var size = StringUtil.formatFileSize(fileLength);
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

    public void setClickEmptyTableTextLink(Consumer<Path> clickEmptyTableTextLink) {
        this.clickEmptyTableTextLink = clickEmptyTableTextLink;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        tabPane = new JTabbedPane();

        //======== this ========
        setPreferredSize(new Dimension(650, 320));
        setBorder(null);
        setLayout(new MigLayout(
            "fill,hidemode 3,align left top",
            // columns
            "[grow,left]",
            // rows
            "[fill]"));

        //======== tabPane ========
        {
            tabPane.setBorder(null);
            tabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        }
        add(tabPane, "cell 0 0,grow");
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JTabbedPane tabPane;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
