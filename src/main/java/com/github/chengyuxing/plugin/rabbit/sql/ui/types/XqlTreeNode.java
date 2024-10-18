package com.github.chengyuxing.plugin.rabbit.sql.ui.types;

import javax.swing.tree.DefaultMutableTreeNode;

public class XqlTreeNode extends DefaultMutableTreeNode {

    public XqlTreeNode(Object userObject) {
        super(userObject);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof XqlTreeNode)) return false;
        var that = (XqlTreeNode) o;

        return getUserObject().equals(that.getUserObject());
    }

    @Override
    public int hashCode() {
        return getUserObject().hashCode();
    }
}
