package com.github.chengyuxing.plugin.rabbit.sql.ui.types;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.sql.PagedResource;
import com.github.chengyuxing.sql.page.IPageable;
import com.github.chengyuxing.sql.types.BatchResult;

public enum XQLJavaType {
    List("List", "<T>"),
    Set("Set", "<T>"),
    Stream("Stream", "<T>"),
    Optional("Optional", "<T>"),
    PagedResource(PagedResource.class.getSimpleName(), "<T>"),
    GenericT("<T>", ""),
    IPageable(IPageable.class.getSimpleName(), ""),
    String("String", ""),
    Integer("Integer", ""),
    Long("Long", ""),
    Double("Double", ""),
    Boolean("Boolean", ""),
    BatchResult(BatchResult.class.getSimpleName(), ""),

    Map("Map", "<String, ?>"),
    DataRow(DataRow.class.getSimpleName(), ""),
    Object("Object", "");

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
