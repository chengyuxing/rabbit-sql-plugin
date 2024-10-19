package com.github.chengyuxing.plugin.rabbit.sql.plugins.database;

import java.util.Objects;

public final class DatabaseId {
    private final String name;
    private final String id;

    public DatabaseId(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public static DatabaseId of(String name, String id) {
        return new DatabaseId(name, id);
    }

    public static DatabaseId empty(String placeholder) {
        return of(placeholder, "");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatabaseId)) return false;

        DatabaseId that = (DatabaseId) o;
        return Objects.equals(getName(), that.getName()) && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getName());
        result = 31 * result + Objects.hashCode(getId());
        return result;
    }
}
