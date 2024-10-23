package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.intellij.openapi.actionSystem.AnAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AnActionWrapper extends AbstractAction {

    public AnActionWrapper(AnAction action) {
        putValue("AnAction", action);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
