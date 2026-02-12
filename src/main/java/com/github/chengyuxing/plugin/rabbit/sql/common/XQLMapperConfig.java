package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.openapi.diagnostic.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class XQLMapperConfig {
    private static final Logger log = Logger.getInstance(XQLMapperConfig.class);
    private String baki;
    private String packageName;
    private Map<String, XQLMethod> methods = new LinkedHashMap<>();
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

    public void saveTo(Path path) {
        var yaml = new Yaml();
        try {
            var result = yaml.dumpAsMap(this);
            result = "# Rabbit SQL plugin - XQL mapper generate configuration - DO NOT MODIFY\n\n" + result;
            Files.writeString(path, result, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn(e);
        }
    }

    public static Path getDefaultPath(XQLConfigManager.Config config, XQLFileManager.Resource resource) {
        var filename = resource.getFilename();
        if (ProjectFileUtil.isLocalFileUri(filename)) {
            return Path.of(URI.create(resource.getFilename() + ".rbm"));
        }
        // http://localhost:8080/share/cyx.xql?token=abcdef
        var remotePath = filename;
        int qIdx = filename.indexOf('?');
        if (qIdx > 0) {
            remotePath = filename.substring(0, qIdx).replaceAll("[\\\\/:*?\"<>|&$]+", "_");
        }
        return config.getResourcesRoot().resolve(remotePath + ".rbm");
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
        private XQLParamMeta paramMeta;
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

        public XQLParamMeta getParamMeta() {
            return paramMeta;
        }

        public void setParamMeta(XQLParamMeta paramMeta) {
            this.paramMeta = paramMeta;
        }
    }

    public static class XQLParamMeta {
        private String className;
        private String comment;
        private Set<String> lombok = new HashSet<>();
        private Map<String, XQLParam> params = new HashMap<>();

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public Set<String> getLombok() {
            return lombok;
        }

        public void setLombok(Set<String> lombok) {
            if (Objects.nonNull(lombok)) {
                this.lombok = lombok;
            }
        }

        public Map<String, XQLParam> getParams() {
            return params;
        }

        public void setParams(Map<String, XQLParam> params) {
            if (Objects.nonNull(params)) {
                this.params = params;
            }
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }

    public static class XQLParam {
        private String type;
        private String comment;
        private Boolean required;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public Boolean getRequired() {
            return required;
        }

        public void setRequired(Boolean required) {
            this.required = required;
        }
    }
}