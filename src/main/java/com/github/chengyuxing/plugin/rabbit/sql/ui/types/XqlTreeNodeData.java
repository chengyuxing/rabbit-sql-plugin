package com.github.chengyuxing.plugin.rabbit.sql.ui.types;

public record XqlTreeNodeData(Type type, String title, Object source) {
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
         * xqls/home.xql -> xqls
         */
        XQL_FILE_FOLDER,
        /**
         * getUsersTop10
         */
        XQL_FRAGMENT
    }
}
