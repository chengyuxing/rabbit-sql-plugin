package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.github.chengyuxing.common.MostDateTime;
import com.github.chengyuxing.common.utils.ObjectUtil;
import com.github.chengyuxing.plugin.rabbit.sql.ui.renderer.FieldInfoRender;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLMapperConfig;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.TabbedPaneWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class EntityGenerateFrom extends JPanel {
    private final Project project;
    private final Map<String, Set<String>> fieldMapping;
    private final Map<String, XQLMapperConfig.XQLParam> paramMeta;
    private final Set<String> lombok;
    private final List<JBCheckBox> checkBoxes;
    private TabbedPaneWrapper tabs;

    private JBTable table;

    private JBTextField classTextField;
    private JBTextField commentTextField;

    private final Disposable disposable;

    private static final Object[] thead = new Object[]{
            "Field",
            "Type",
            "Comment",
            "Required"};

    private static final List<String> FIELD_TYPES = List.of(
            String.class.getSimpleName(),
            Integer.class.getSimpleName(),
            Double.class.getSimpleName(),
            Float.class.getSimpleName(),
            Long.class.getSimpleName(),
            Character.class.getSimpleName(),
            Boolean.class.getSimpleName(),
            Short.class.getSimpleName(),
            byte[].class.getSimpleName(),
            Object.class.getSimpleName(),
            UUID.class.getName(),
            Date.class.getName(),
            LocalDateTime.class.getName(),
            OffsetDateTime.class.getName(),
            ZonedDateTime.class.getName(),
            LocalDate.class.getName(),
            LocalTime.class.getName(),
            OffsetTime.class.getName(),
            Instant.class.getName(),
            MostDateTime.class.getName(),
            File.class.getName(),
            InputStream.class.getName(),
            Path.class.getName(),
            Map.class.getName() + "<" + String.class.getSimpleName() + ", " + Object.class.getSimpleName() + ">",
            List.class.getName() + "<" + Object.class.getSimpleName() + ">",
            Set.class.getName() + "<" + Object.class.getSimpleName() + ">"
    );

    private static final List<String> LOMBOK_SUPPORTS = List.of(
            "@Getter",
            "@Setter",
            "@Data",
            "@Builder",
            "@ToString",
            "@EqualsAndHashCode"
    );

    public EntityGenerateFrom(Project project, Map<String, Set<String>> fieldMapping, Map<String, XQLMapperConfig.XQLParam> paramMeta, Set<String> lombok, Disposable disposable) {
        this.project = project;
        this.fieldMapping = fieldMapping;
        this.paramMeta = paramMeta;
        this.lombok = lombok;
        this.checkBoxes = new ArrayList<>();
        this.disposable = disposable;
        initComponents();
    }

    @SuppressWarnings("rawtypes")
    public Vector<Vector> getFieldMappingData() {
        return ((DefaultTableModel) table.getModel()).getDataVector();
    }

    public Set<String> getSelectedLombok() {
        return checkBoxes.stream()
                .filter(AbstractButton::isSelected)
                .map(AbstractButton::getText)
                .collect(Collectors.toSet());
    }

    public String getFullyClassName() {
        return classTextField.getText();
    }

    public String getComment() {
        return commentTextField.getText();
    }

    public void selectConfigTab() {
        tabs.setSelectedIndex(0);
        classTextField.requestFocus();
    }

    public void setClassName(String className) {
        if (Objects.nonNull(className)) {
            classTextField.setText(className);
        }
    }

    public void setComment(String className) {
        if (Objects.nonNull(className)) {
            commentTextField.setText(className);
        }
    }

    private void initComponents() {
        setPreferredSize(new Dimension(540, 200));
        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new MigLayout(
                "insets 0,hidemode 3",
                // columns
                "[grow 1,fill]",
                // rows
                "[grow 1,fill]"));

        tabs = new TabbedPaneWrapper(disposable);

        tabs.addTab("Fields", AllIcons.Nodes.Field, createTablePanel(), "");
        tabs.addTab("Class", AllIcons.Nodes.Class, createConfigPanel(), "");

        add(tabs.getComponent(), "cell 0 0,grow");
    }

    private JPanel createTablePanel() {
        var panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder());
        panel.setLayout(new MigLayout(
                "insets 8 0 0 0,hidemode 3",
                // columns
                "[grow 1,fill]",
                // rows
                "[grow 1,fill]"));
        table = new JBTable() {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == table.convertColumnIndexToView(3)) {
                    return Boolean.class;
                }
                return super.getColumnClass(column);
            }
        };
        table.setBorder(BorderFactory.createEmptyBorder());
        table.setShowVerticalLines(false);
        table.setRowHeight(30);
        table.setSelectionForeground(null);
        table.setSelectionBackground(null);
        table.setFillsViewportHeight(true);
        table.getEmptyText().setText("There are no fields in the table.");
        var tableScrollPane = new JBScrollPane();
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableScrollPane.setViewportView(table);
        initTable();
        panel.add(tableScrollPane);
        return panel;
    }

    private JPanel createConfigPanel() {
        var panel = new JPanel();
        panel.setLayout(new FormLayout(
                new ColumnSpec[]{
                        new ColumnSpec(Sizes.dluX(55)),
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        new ColumnSpec(ColumnSpec.FILL, Sizes.dluX(50), FormSpec.DEFAULT_GROW)
                },
                RowSpec.decodeSpecs("default, 4dlu, min, 3dlu, min, 4dlu, fill:default:grow")));
        CellConstraints cc = new CellConstraints();

        classTextField = new JBTextField();
        panel.add(new JBLabel("Fully class name:"), cc.xy(1, 3));
        panel.add(classTextField, cc.xy(3, 3, CellConstraints.FILL, CellConstraints.DEFAULT));

        commentTextField = new JBTextField();
        panel.add(new JBLabel("Comment:"), cc.xy(1, 5));
        panel.add(commentTextField, cc.xy(3, 5, CellConstraints.FILL, CellConstraints.DEFAULT));

        var lombokLabel = new JBLabel("Lombok:");
        lombokLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        panel.add(lombokLabel, cc.xy(1, 7, CellConstraints.LEFT, CellConstraints.TOP));
        panel.add(buildLombokPanel(), cc.xy(3, 7, CellConstraints.LEFT, CellConstraints.FILL));

        return panel;
    }

    private void initTable() {
        var model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };
        table.setModel(model);
        var tbody = fieldMapping.keySet().stream()
                .map(filed -> {
                    var xqlParam = paramMeta.get(filed);
                    var type = FIELD_TYPES.get(0);
                    var comment = "";
                    var required = true;
                    if (Objects.nonNull(xqlParam)) {
                        type = ObjectUtil.coalesce(xqlParam.getType(), type);
                        comment = ObjectUtil.coalesce(xqlParam.getComment(), comment);
                        required = ObjectUtil.coalesce(xqlParam.getRequired(), required);
                    }
                    return new Object[]{filed, type, comment, required};
                })
                .toArray(i -> new Object[i][4]);
        model.setDataVector(tbody, thead);
        table.getColumnModel().getColumn(0).setCellRenderer(new FieldInfoRender(fieldMapping));
        table.getColumnModel().getColumn(1).setCellEditor(buildTyeSelector());
        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JBTextField()));
        table.getColumnModel().getColumn(3).setMaxWidth(80);
    }

    private DefaultCellEditor buildTyeSelector() {
        var cbx = new ComboBox<>();
        cbx.setEditable(false);
        for (String item : EntityGenerateFrom.FIELD_TYPES) {
            cbx.addItem(item);
        }
        return new DefaultCellEditor(cbx);
    }

    private JPanel buildLombokPanel() {
        var panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        LOMBOK_SUPPORTS.forEach(s -> {
            var checkBox = new JBCheckBox(s);
            checkBox.setSelected(lombok.contains(s));
            panel.add(checkBox);
            checkBoxes.add(checkBox);
        });
        return panel;
    }
}
