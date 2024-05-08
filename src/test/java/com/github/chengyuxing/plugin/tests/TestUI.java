/*
 * Created by JFormDesigner on Wed May 08 15:46:30 CST 2024
 */

package com.github.chengyuxing.plugin.tests;

import java.awt.*;
import javax.swing.*;
import com.intellij.ui.components.fields.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

/**
 * @author chengyuxing
 */
public class TestUI extends JPanel {
    public TestUI() {
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        expandableTextField1 = new ExpandableTextField();
        expandableTextField2 = new ExpandableTextField();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setLayout(new FormLayout(
            new ColumnSpec[] {
                FormFactory.DEFAULT_COLSPEC,
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC,
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC,
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC
            },
            new RowSpec[] {
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.LINE_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.LINE_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.LINE_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC
            }));

        //---- expandableTextField1 ----
        expandableTextField1.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        add(expandableTextField1, cc.xy(7, 3));
        add(expandableTextField2, cc.xy(7, 7));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private ExpandableTextField expandableTextField1;
    private ExpandableTextField expandableTextField2;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
