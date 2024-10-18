package com.github.chengyuxing.plugin.rabbit.sql.ui.types;

import java.util.Objects;

public class XqlTreeNodeData {
    private final Type type;
    private final String title;
    private final Object source;

    public XqlTreeNodeData(Type type, String title, Object source) {
        this.type = type;
        this.title = title;
        this.source = source;
    }

    public Object getSource() {
        return source;
    }

    public String getTitle() {
        return title;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof XqlTreeNodeData)) return false;

        XqlTreeNodeData that = (XqlTreeNodeData) o;
        return getType() == that.getType() && Objects.equals(getTitle(), that.getTitle()) && Objects.equals(getSource(), that.getSource());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getType());
        result = 31 * result + Objects.hashCode(getTitle());
        result = 31 * result + Objects.hashCode(getSource());
        return result;
    }

    public enum Type {
        MODULE,
        /**
         * xql-file-manager.yml
         */
        XQL_CONFIG,
        /**
         * home -> xqls/home.xql
         */
        XQL_FILE,
        /**
         * xqls/home.xql -> xqls
         */
        XQL_FILE_FOLDER,
        /**
         * getUsersTop10
         */
        XQL_FRAGMENT
    }
}
