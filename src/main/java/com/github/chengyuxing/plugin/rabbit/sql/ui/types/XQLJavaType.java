package com.github.chengyuxing.plugin.rabbit.sql.ui.types;

public enum XQLJavaType {
    List("List", "<T>"),
    Set("Set", "<T>"),
    Stream("Stream", "<T>"),
    Optional("Optional", "<T>"),
    PagedResource("PagedResource", "<T>"),
    GenericT("<T>", ""),
    IPageable("IPageable", ""),
    Integer("Integer", ""),
    Long("Long", ""),
    Double("Double", ""),

    Map("Map", "<String, Object>"),
    DataRow("DataRow", ""),
    MultiArgs("@Arg", "");

    private final String value;
    private final String generic;

    XQLJavaType(String value, String generic) {
        this.value = value;
        this.generic = generic;
    }

    public String getValue() {
        return value;
    }

    public String getGeneric() {
        return generic;
    }

    @Override
    public String toString() {
        return value + generic;
    }
}
