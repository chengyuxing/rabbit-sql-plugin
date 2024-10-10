package com.github.chengyuxing.plugin.rabbit.sql.ui.types;

import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;

import java.util.*;

public class XQLMapperTemplateData {
    private final String packageName;
    private final String mapperAlias;
    private String baki;
    private String description;
    private final String mapperInterfaceName;
    private List<Method> methods = new ArrayList<>();
    private Set<String> entityImports = new LinkedHashSet<>();
    private String userImports = "";
    private String userMethods = "";

    private String user;
    private String date;

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

    public void setMethods(List<Method> methods) {
        if (methods != null) {
            this.methods = methods;
        }
    }

    public String getPackageName() {
        return packageName;
    }

    public String getMapperAlias() {
        return mapperAlias;
    }

    public String getMapperInterfaceName() {
        return mapperInterfaceName;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public XQLMapperTemplateData(String packageName, String mapperAlias) {
        this.packageName = packageName;
        this.mapperAlias = mapperAlias;
        this.mapperInterfaceName = StringUtil.camelizeAndClean(mapperAlias.substring(0, 1).toUpperCase() + mapperAlias.substring(1)) + "Mapper";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserImports() {
        return userImports;
    }

    public void setUserImports(String userImports) {
        this.userImports = userImports;
    }

    public String getUserMethods() {
        return userMethods;
    }

    public void setUserMethods(String userMethods) {
        this.userMethods = userMethods;
    }

    public String getBaki() {
        return baki;
    }

    public void setBaki(String baki) {
        this.baki = baki;
    }

    public Set<String> getEntityImports() {
        return entityImports;
    }

    public void setEntityImports(Set<String> entityImports) {
        this.entityImports = entityImports;
    }

    public static class Method {
        private final String name;
        private final String returnType;
        private String description;
        private String annotationValue;
        private String sqlType;
        private String paramType;
        private Set<String> parameters = new LinkedHashSet<>();
        private String countQuery;

        public Method(String returnType, String methodName) {
            this.returnType = returnType;
            this.name = methodName;
        }

        public void setParameters(Set<String> parameters) {
            if (parameters != null) {
                this.parameters = parameters;
            }
        }

        public String getReturnType() {
            return returnType;
        }

        public String getAnnotationValue() {
            return annotationValue;
        }

        public void setAnnotationValue(String annotationValue) {
            this.annotationValue = annotationValue;
        }

        public String getSqlType() {
            return sqlType;
        }

        public void setSqlType(String sqlType) {
            this.sqlType = sqlType;
        }

        public String getName() {
            return name;
        }

        public Set<String> getParameters() {
            return parameters;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getParamType() {
            return paramType;
        }

        public void setParamType(String paramType) {
            this.paramType = paramType;
        }

        public String getCountQuery() {
            return countQuery;
        }

        public void setCountQuery(String countQuery) {
            this.countQuery = countQuery;
        }
    }
}
