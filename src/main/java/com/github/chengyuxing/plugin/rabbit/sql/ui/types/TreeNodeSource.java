package com.github.chengyuxing.plugin.rabbit.sql.ui.types;

public record TreeNodeSource(Type type, String title, Object source) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TreeNodeSource that)) return false;

        if (type != that.type) return false;
        if (!title.equals(that.title)) return false;
        return source.equals(that.source);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + source.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return title;
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
         * getUsersTop10
         */
        XQL_FRAGMENT
    }
}
