package com.github.chengyuxing.plugin.rabbit.sql.util;

import javax.swing.*;

public abstract class BtnAction extends AbstractAction {
    public BtnAction(String name, String description) {
        super(name);
        putValue(NAME, name);
        putValue(SHORT_DESCRIPTION, description);
    }

    public BtnAction(String name, String description, Icon icon) {
        super(name, icon);
        putValue(NAME, name);
        putValue(SHORT_DESCRIPTION, description);
    }
}
