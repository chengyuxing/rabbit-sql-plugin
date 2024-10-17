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
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.github.chengyuxing.common.MostDateTime;
import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.tuple.Triple;
import com.github.chengyuxing.common.tuple.Tuples;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.renderer.LinkCellRender;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.DataCell;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.*;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBEditorTabs;
import com.jgoodies.forms.layout.*;
import net.miginfocom.swing.*;
import org.jetbrains.annotations.NotNull;

/**
 * @author chengyuxing
 */
public class StatisticsForm extends JPanel {
    private final Project project;
    private JBEditorTabs tabPane;
    // (module, configs)
    private final Map<Path, Set<XQLConfigManager.Config>> configMap;
    // (table, configs)
    private final Map<JBTable, List<XQLConfigManager.Config>> dataMap = new LinkedHashMap<>();

    private static final Object[] summaryTableHeader = new Object[]{"Config", "Total XQL Files", "Total SQLs", "Total Lines", "Total Size"};
    private static final Object[] detailsTableHeader = new Object[]{"File Name", "Alias", "SQLs", "Lines", "Size", "Last Modified"};

    private Consumer<Path> clickEmptyTableTextLink = (module) -> {
    };

    private final Disposable disposable;

    public StatisticsForm(Project project, Map<Path, Set<XQLConfigManager.Config>> configMap, Disposable disposable) {
        this.project = project;
        this.configMap = configMap;
        this.disposable = disposable;
        initComponents();
        customInitComponents();
        initTableDatasource();
    }

    @SuppressWarnings("rawtypes")
    public Triple<String, ArrayList<String>, Vector<Vector>> getDisplayData() {
        var tabInfo = tabPane.getSelectedInfo();
        if (Objects.isNull(tabInfo)) {
            return null;
        }
        var tabIndex = tabPane.getIndexOf(tabInfo);
        if (tabIndex == -1) {
            return null;
        }
        int i = 0;
        for (var table : dataMap.keySet()) {
            if (tabIndex == i) {
                var model = (DefaultTableModel) table.getModel();
                var headerColumn = table.getTableHeader().getColumnModel().getColumns();
                var header = new ArrayList<String>();
                while (headerColumn.hasMoreElements()) {
                    header.add(headerColumn.nextElement().getHeaderValue().toString());
                }
                var module = ((Path) tabInfo.getObject()).getFileName().toString();
                return Tuples.of(module, header, model.getDataVector());
            }
            i++;
        }
        return null;
    }

    private void initTableData(JBTable table, List<XQLConfigManager.Config> pairConfig) {
        var tbody = pairConfig.stream().map(config -> {
            var xqlFileManager = config.getXqlFileManager();
            long totalLines = 0;
            long totalSize = 0;
            for (var file : xqlFileManager.getFiles().values()) {
                if (!ProjectFileUtil.isLocalFileUri(file)) {
                    continue;
                }
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
        tabPane = new JBEditorTabs(project, IdeFocusManager.getInstance(project), disposable);
        configMap.forEach((path, configs) -> {
            var validConfigs = configs.stream()
                    .filter(XQLConfigManager.Config::isValid)
                    .filter(config -> Objects.nonNull(config.getXqlFileManager()))
                    .toList();
            var module = path.getFileName().toString();

            var panel = new JPanel();
            panel.setLayout(new MigLayout(
                    "insets 8 0 0 0,hidemode 3",
                    // columns
                    "[grow 1,fill]",
                    // rows
                    "[grow 1,fill]"));
            CellConstraints cc = new CellConstraints();

            var tablePanel = new JBScrollPane();
            tablePanel.setBorder(BorderFactory.createEmptyBorder());
            var table = createTable();
            dataMap.put(table, validConfigs);
            tablePanel.setViewportView(table);
            panel.add(tablePanel);
            var info = new TabInfo(panel);
            info.setIcon(AllIcons.Nodes.Module);
            info.setText(module + (validConfigs.isEmpty() ? " *" : ""));
            info.setTooltipText(path.toString());
            info.setObject(path);
            tabPane.addTab(info);
        });
        add(tabPane, "cell 0 0,grow");
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
                var tabInfo = tabPane.getSelectedInfo();
                if (Objects.isNull(tabInfo)) {
                    return;
                }
                clickEmptyTableTextLink.accept((Path) tabInfo.getObject());
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
                                            var lines = -1L;
                                            var fileLength = -1L;
                                            var lastModified = "--";
                                            try {
                                                if (ProjectFileUtil.isLocalFileUri(filePath)) {
                                                    var path = Path.of(URI.create(filePath));
                                                    lines = ProjectFileUtil.lineNumber(path);
                                                    fileLength = Files.size(path);
                                                    var fileModifiedDate = Files.getLastModifiedTime(path).toInstant();
                                                    lastModified = MostDateTime.of(fileModifiedDate).toString("yyyy/MM/dd HH:mm:ss");
                                                }
                                            } catch (IOException ignored) {

                                            }
                                            var size = fileLength >= 0 ? StringUtil.formatFileSize(fileLength) : "-1";
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

        //======== this ========
        setPreferredSize(new Dimension(650, 320));
        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new MigLayout(
            "fill,hidemode 3,align left top",
            // columns
            "[grow,left]",
            // rows
            "[fill]"));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
