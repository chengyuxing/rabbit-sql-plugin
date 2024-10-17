package com.github.chengyuxing.plugin.rabbit.sql.ui.types;

import com.intellij.openapi.diagnostic.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class XQLMapperConfig {
    private static final Logger log = Logger.getInstance(XQLMapperConfig.class);
    private String baki;
    private String packageName;
    private Map<String, XQLMethod> methods = new HashMap<>();
    private String pageKey;
    private String sizeKey;

    public static XQLMapperConfig load(Path path) {
        if (Files.exists(path)) {
            var yaml = new Yaml();
            try {
                return yaml.loadAs(Files.newInputStream(path), XQLMapperConfig.class);
            } catch (IOException e) {
                log.warn(e);
            }
        }
        return new XQLMapperConfig();
    }

    public String getBaki() {
        return baki;
    }

    public void setBaki(String baki) {
        this.baki = baki;
    }

    public Map<String, XQLMethod> getMethods() {
        return methods;
    }

    public void setMethods(Map<String, XQLMethod> methods) {
        if (methods != null) {
            this.methods = methods;
        }
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPageKey() {
        return pageKey;
    }

    public void setPageKey(String pageKey) {
        this.pageKey = pageKey;
    }

    public String getSizeKey() {
        return sizeKey;
    }

    public void setSizeKey(String sizeKey) {
        this.sizeKey = sizeKey;
    }

    public static class XQLMethod {
        private String returnType;
        private String returnGenericType;
        private String sqlType;
        private String paramType;
        private Boolean enable = true;

        public String getReturnType() {
            return returnType;
        }

        public void setReturnType(String returnType) {
            this.returnType = returnType;
        }

        public String getSqlType() {
            return sqlType;
        }

        public void setSqlType(String sqlType) {
            this.sqlType = sqlType;
        }

        public String getParamType() {
            return paramType;
        }

        public void setParamType(String paramType) {
            this.paramType = paramType;
        }

        public String getReturnGenericType() {
            return returnGenericType;
        }

        public void setReturnGenericType(String returnGenericType) {
            this.returnGenericType = returnGenericType;
        }

        public Boolean getEnable() {
            return enable;
        }

        public void setEnable(Boolean enable) {
            this.enable = enable;
        }
    }
}