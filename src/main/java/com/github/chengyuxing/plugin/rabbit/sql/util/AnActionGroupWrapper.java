package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.intellij.openapi.actionSystem.ActionGroup;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AnActionGroupWrapper extends AbstractAction {

    public AnActionGroupWrapper(ActionGroup actionGroup) {
        putValue("AnAction", actionGroup);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
