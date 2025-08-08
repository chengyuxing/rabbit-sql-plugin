package com.github.chengyuxing.plugin.rabbit.sql.ui.datasource.components;

import com.github.chengyuxing.plugin.rabbit.sql.common.Global;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DatasourceConfigPanel extends JPanel {
    private final List<String> datasources = new ArrayList<>();

    private JBSplitter splitter;
    private JBScrollPane datasourcePane;
    private JPanel configPane;
    private JBList<String> datasourceList;

    private JBTextField name;
    private JBTextField comment;

    private JBTextField jdbcUrl;
    private JBTextField jdbcUsername;
    private JBTextField jdbcPassword;

    public DatasourceConfigPanel() {
        initComponents();
    }

    private void initComponents() {
        setPreferredSize(new Dimension(750, 400));
        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new MigLayout(
                "insets 0,hidemode 3",
                // columns
                "[grow 1,fill]",
                // rows
                "[grow 1,fill]"));

        // init datasource list component
        datasourceList = new JBList<>(datasources);
        datasourceList.setFont(Global.getEditorFont(datasourceList.getFont().getSize() + 1));
        datasourceList.setEmptyText("No datasources.");
        datasourceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        datasourcePane = new JBScrollPane();
        datasourcePane.setBorder(new LineBorder(new JBColor(new Color(0x6A6A6A), new Color(0x0C0C0C))));
        datasourcePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        datasourcePane.setViewportView(datasourceList);

        // init datasource config component
        configPane = new JPanel();
        UIUtil.addInsets(configPane, JBUI.insetsLeft(5));
        configPane.setLayout(new FormLayout(
                new ColumnSpec[]{
                        new ColumnSpec(Sizes.dluX(35)),
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        new ColumnSpec(ColumnSpec.FILL, Sizes.dluX(50), FormSpec.DEFAULT_GROW)
                },
                RowSpec.decodeSpecs("default, 4dlu, min, 4dlu, min, 10dlu, min, 4dlu, min, 4dlu, min, 4dlu, fill:default:grow")));
        CellConstraints cc = new CellConstraints();



        name = new JBTextField();
        configPane.add(new JBLabel("Name:"), cc.xy(1, 3));
        configPane.add(name, cc.xy(3, 3, CellConstraints.FILL, CellConstraints.DEFAULT));

        comment = new JBTextField();
        configPane.add(new JBLabel("Comment:"), cc.xy(1, 5));
        configPane.add(comment, cc.xy(3, 5, CellConstraints.FILL, CellConstraints.DEFAULT));

        jdbcUrl = new JBTextField();
        configPane.add(new JBLabel("JDBC URL:"), cc.xy(1, 7));
        configPane.add(jdbcUrl, cc.xy(3, 7, CellConstraints.FILL, CellConstraints.DEFAULT));

        jdbcUsername = new JBTextField();
        configPane.add(new JBLabel("User:"), cc.xy(1, 9));
        configPane.add(jdbcUsername, cc.xy(3, 9));

        jdbcPassword = new JBTextField();
        configPane.add(new JBLabel("Password:"), cc.xy(1, 11));
        configPane.add(jdbcPassword, cc.xy(3, 11));

        splitter = new JBSplitter();
        splitter.setOrientation(false);
        splitter.setProportion(0.3f);
        splitter.setFirstComponent(datasourcePane);
        splitter.setSecondComponent(configPane);
        add(splitter, "cell 0 0,grow");

        datasources.add("ABC");
    }
}
