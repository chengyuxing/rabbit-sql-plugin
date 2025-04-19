package com.github.chengyuxing.plugin.rabbit.sql.ui.types;

import java.util.LinkedHashSet;
import java.util.Set;

public class ClassTemplateData {
    private final String className;
    private final String packageName;
    private String comment;
    private Set<Field> fields = new LinkedHashSet<>();
    private Set<String> imports = new LinkedHashSet<>();
    private Set<String> lombok = new LinkedHashSet<>();

    private String user;
    private String date;

    public ClassTemplateData(String className) {
        if (className.contains(".")) {
            this.packageName = className.substring(0, className.lastIndexOf("."));
            this.className = className.substring(className.lastIndexOf(".") + 1);
        } else {
            this.packageName = null;
            this.className = className;
        }
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Set<String> getLombok() {
        return lombok;
    }

    public void setLombok(Set<String> lombok) {
        if (lombok != null) {
            this.lombok = lombok;
        }
    }

    public Set<String> getImports() {
        return imports;
    }

    public void setImports(Set<String> imports) {
        if (imports != null) {
            this.imports = imports;
        }
    }

    public Set<Field> getFields() {
        return fields;
    }

    public void setFields(Set<Field> fields) {
        if (fields != null) {
            this.fields = fields;
        }
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public static class Field {
        private final String name;
        private final String type;
        private String comment;

        public Field(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }
}
