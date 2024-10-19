package com.github.chengyuxing.plugin.rabbit.sql.plugins.database;

public record DatabaseId(String name, String id) {

    public static DatabaseId of(String name, String id) {
        return new DatabaseId(name, id);
    }

    public static DatabaseId empty(String placeholder) {
        return of(placeholder, "");
    }

    @Override
    public String toString() {
        return name;
    }
}
