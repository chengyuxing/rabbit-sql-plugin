package com.github.chengyuxing.plugin.rabbit.sql.ui.types;

import com.github.chengyuxing.common.MostDateTime;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLMapperConfig;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.TypeUtil;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ClassTemplateData {
    private final ClassInfo clazz;
    private String comment;
    private Set<Field> fields = new LinkedHashSet<>();
    private Set<String> imports = new LinkedHashSet<>();
    private Set<String> lombok = new LinkedHashSet<>();

    private String user;
    private String date;

    public ClassTemplateData(String className) {
        this.clazz = TypeUtil.extractFullClassInfo(className);
    }

    public static ClassTemplateData of(XQLMapperConfig.XQLParamMeta paramMeta) {
        var imports = new LinkedHashSet<String>();
        var fields = new LinkedHashSet<Field>();
        for (Map.Entry<String, XQLMapperConfig.XQLParam> entry : paramMeta.getParams().entrySet()) {
            String name = entry.getKey();
            XQLMapperConfig.XQLParam param = entry.getValue();
            if (!param.getRequired()) {
                continue;
            }
            var type = param.getType();
            var shortType = type;
            if (type.contains(".")) {
                var typeNameAndPackage = StringUtil.getTypeAndPackagePath(type);
                shortType = typeNameAndPackage.getItem1();
                imports.add(typeNameAndPackage.getItem2());
            }
            var field = new ClassTemplateData.Field(name, shortType);
            field.setComment(param.getComment());
            fields.add(field);
        }
        var templateData = new ClassTemplateData(paramMeta.getClassName());
        templateData.setUser(System.getProperty("user.name"));
        templateData.setDate(MostDateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
        templateData.setImports(imports);
        templateData.setFields(fields);
        templateData.setLombok(paramMeta.getLombok());
        templateData.setComment(paramMeta.getComment());
        return templateData;
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

    public ClassInfo getClazz() {
        return clazz;
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
