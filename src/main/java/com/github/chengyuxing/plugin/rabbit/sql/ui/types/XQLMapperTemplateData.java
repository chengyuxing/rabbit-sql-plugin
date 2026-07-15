package com.github.chengyuxing.plugin.rabbit.sql.ui.types;

import com.github.chengyuxing.common.MostDateTime;
import com.github.chengyuxing.common.util.StringUtils;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLMapperConfig;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.TypeUtil;
import com.github.chengyuxing.sql.annotation.SqlStatementType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public class XQLMapperTemplateData {
    private final String packageName;
    private final String mapperAlias;
    private String baki;
    private String description;
    private final String mapperInterfaceName;
    private List<Method> methods = new ArrayList<>();
    private Set<String> classImports = new LinkedHashSet<>();
    private Map<String, XQLMapperTemplateData.SimpleEntity> userEntities = new LinkedHashMap<>();
    private State state = new State();
    private String userImports = "";
    private String userMethods = "";
    private String userAnnotations = "";

    private String user;
    private String date;

    public XQLMapperTemplateData(String packageName, String mapperAlias) {
        this.packageName = packageName;
        this.mapperAlias = mapperAlias;
        this.mapperInterfaceName = StringUtil.generateInterfaceMapperName(mapperAlias);
    }

    public static XQLMapperTemplateData of(XQLMapperConfig xqlMapperCnf, String alias, XQLConfigManager.Config config, Path mapperFile) throws IOException {
        var xqlFileManager = config.getXqlFileManager();
        var resource = xqlFileManager.getResource(alias);
        if (resource == null) {
            throw new IOException(alias + " of resource not found");
        }
        var classImports = new LinkedHashSet<>(List.of(
                List.class.getName(),
                Map.class.getName(),
                ""
        ));
        // use for generate user custom entity class
        var userEntityClasses = new LinkedHashMap<String, XQLMapperTemplateData.SimpleEntity>();
        var methods = new ArrayList<XQLMapperTemplateData.Method>();
        var state = new State();
        for (Map.Entry<String, XQLMapperConfig.XQLMethod> entry : xqlMapperCnf.getMethods().entrySet()) {
            String sqlName = entry.getKey();
            XQLMapperConfig.XQLMethod method = entry.getValue();
            if (!method.getEnable()) {
                continue;
            }
            var paramType = method.getParamType();
            if (paramType.isEmpty()) {
                paramType = XQLJavaType.Map.getValue();
            }

            var simpleParamType = paramType;
            var paramUserEntity = TypeUtil.getUserEntity(paramType);
            if (Objects.nonNull(paramUserEntity)) {
                classImports.add(paramType);
                var simple = new XQLMapperTemplateData.SimpleEntity();
                simple.setTemplateName("entity.java");
                userEntityClasses.put(paramType, simple);
                simpleParamType = paramUserEntity;
            }
            var optionalSimpleEntity = Optional.ofNullable(userEntityClasses.get(paramType));

            var returnTypes = method.getReturnType();
            var returnGenericType = method.getReturnGenericType();
            var methodName = StringUtil.camelizeAndClean(sqlName);
            var sqlType = method.getSqlType();
            var sql = resource.getEntry().get(sqlName);

            var sqlParamNames = StringUtil.getParamsMappingInfo(config.getSqlGenerator(), sql.getSource())
                    .keySet();
            var paramMeta = method.getParamMeta();
            var sqlParamObj = paramMeta == null ? Map.<String, XQLMapperConfig.XQLParam>of() : paramMeta.getParams();

            var simpleReturnGenericType = returnGenericType;
            var genericUserEntity = TypeUtil.getUserEntity(returnGenericType);
            if (Objects.nonNull(genericUserEntity)) {
                classImports.add(returnGenericType);
                userEntityClasses.put(returnGenericType, new XQLMapperTemplateData.SimpleEntity());
                simpleReturnGenericType = genericUserEntity;
            }

            var pageHelperClass = returnTypes.getPageConfig().getPageHelperClass();
            if (!pageHelperClass.isEmpty()) {
                classImports.add(pageHelperClass);
                var simple = new XQLMapperTemplateData.SimpleEntity();
                simple.setTemplateName("pagehelper.java");
                userEntityClasses.put(pageHelperClass, simple);
            }

            var returnTypeList = returnTypes.getItems();
            if (returnTypeList.isEmpty()) {
                returnTypeList = List.of(XQLJavaType.List.toString());
            }

            final String finalSimpleParamType = simpleParamType;
            final Consumer<XQLMapperTemplateData.Method> fillMethodData = methodData -> {
                methodData.setEnable(method.getEnable());
                methodData.setParamType(finalSimpleParamType);
                methodData.setDescription(sql.getDescription());
                methodData.setSqlType(sqlType);
                methodData.setCountQuery(TypeUtil.detectCountQuery(sqlName, methodData, resource));
                methodData.setPageConfig(TypeUtil.getPageConfig(returnTypes.getPageConfig(), methodData));
                if (Objects.nonNull(paramMeta)) {
                    methodData.setParamClassComment(paramMeta.getComment());
                    optionalSimpleEntity.ifPresent(se -> {
                        se.setComment(paramMeta.getComment());
                        se.setLombok(paramMeta.getLombok());
                    });
                }
                var newParams = TypeUtil.collectParams(sqlParamNames, sqlParamObj, methodData, xqlMapperCnf);
                methodData.setParameters(newParams.getItem1());
                optionalSimpleEntity.ifPresent(se -> {
                    var oldSize = se.getParameters().size();
                    if (oldSize < newParams.getItem1().size()) {
                        se.setParameters(newParams.getItem1());
                    }
                });
                classImports.addAll(newParams.getItem2());
                methods.add(methodData);
            };

            final Consumer<String> updateReturnTypeImportState = returnType -> {
                if (returnType.contains(XQLJavaType.Stream.getValue())) {
                    state.setHasStream(true);
                } else if (returnType.contains(XQLJavaType.Optional.getValue())) {
                    state.setHasOptional(true);
                } else if (returnType.equals(XQLJavaType.IPageable.getValue())) {
                    state.setHasIPageable(true);
                } else if (returnType.contains(XQLJavaType.PagedResource.getValue())) {
                    state.setHasPagedResource(true);
                } else if (returnType.contains(XQLJavaType.Set.getValue())) {
                    state.setHasSet(true);
                }
                if (StringUtils.equalsAny(method.getSqlType(), SqlStatementType.procedure.name(), SqlStatementType.function.name())) {
                    state.setHasProcedure(true);
                }
            };

            if (returnTypeList.size() == 1) {
                var returnType = TypeUtil.replaceGenericT(returnTypeList.get(0), simpleReturnGenericType);
                var methodData = new XQLMapperTemplateData.Method(returnType, methodName);
                if (!sqlName.equals(methodName)) {
                    methodData.setAnnotationValue(sqlName);
                }
                updateReturnTypeImportState.accept(returnType);
                fillMethodData.accept(methodData);
            } else {
                var newReturnTypes = new LinkedHashSet<String>();
                for (var returnType : returnTypeList) {
                    newReturnTypes.add(TypeUtil.replaceGenericT(returnType, simpleReturnGenericType));
                }
                for (var returnType : newReturnTypes) {
                    var extMethodName = methodName + TypeUtil.returnTypeName(returnType, simpleReturnGenericType);
                    var methodData = new XQLMapperTemplateData.Method(returnType, extMethodName);
                    methodData.setAnnotationValue(sqlName);
                    updateReturnTypeImportState.accept(returnType);
                    fillMethodData.accept(methodData);
                }
            }
        }

        var userImports = new StringJoiner("\n");
        var userMethods = new StringJoiner("\n");
        var userAnnotations = new StringJoiner("\n");

        if (Files.exists(mapperFile)) {
            var importsBlockFlag = 0;
            var methodsBlockFlag = 0;
            var annotationsBlockFlag = 0;
            try (var reader = Files.newBufferedReader(mapperFile)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (importsBlockFlag == 2 && methodsBlockFlag == 2 && annotationsBlockFlag == 2) {
                        break;
                    }
                    if (line.contains("//CODE-BEGIN:imports")) {
                        importsBlockFlag++;
                        String importContent;
                        while ((importContent = reader.readLine()) != null) {
                            if (importContent.contains("//CODE-END:imports")) {
                                importsBlockFlag++;
                                break;
                            }
                            userImports.add(importContent);
                        }
                    }
                    if (line.contains("//CODE-BEGIN:annotations")) {
                        annotationsBlockFlag++;
                        String annoContent;
                        while ((annoContent = reader.readLine()) != null) {
                            if (annoContent.contains("//CODE-END:annotations")) {
                                annotationsBlockFlag++;
                                break;
                            }
                            userAnnotations.add(annoContent);
                        }
                    }
                    if (line.contains("//CODE-BEGIN:methods")) {
                        methodsBlockFlag++;
                        String declarationContent;
                        while ((declarationContent = reader.readLine()) != null) {
                            if (declarationContent.contains("//CODE-END:methods")) {
                                methodsBlockFlag++;
                                break;
                            }
                            userMethods.add(declarationContent);
                        }
                    }
                }
            }
        }

        var templateData = new XQLMapperTemplateData(xqlMapperCnf.getPackageName(), alias);
        templateData.setUserImports(userImports.toString().trim());
        templateData.setUserMethods(userMethods.toString().trim());
        templateData.setUserAnnotations(userAnnotations.toString().trim());
        templateData.setUser(System.getProperty("user.name"));
        templateData.setDate(MostDateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
        templateData.setBaki(xqlMapperCnf.getBaki());
        templateData.setDescription(resource.getDescription());
        templateData.setMethods(methods);
        templateData.setClassImports(classImports);
        templateData.setUserEntities(userEntityClasses);
        templateData.setState(state);
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

    public String getUserAnnotations() {
        return userAnnotations;
    }

    public void setUserAnnotations(String userAnnotations) {
        this.userAnnotations = userAnnotations;
    }

    public String getBaki() {
        return baki;
    }

    public void setBaki(String baki) {
        this.baki = baki;
    }

    public Set<String> getClassImports() {
        return classImports;
    }

    public void setClassImports(Set<String> classImports) {
        this.classImports = classImports;
    }

    public Map<String, SimpleEntity> getUserEntities() {
        return userEntities;
    }

    public void setUserEntities(Map<String, SimpleEntity> userEntities) {
        this.userEntities = userEntities;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public static class State {
        private Boolean hasStream = false;
        private Boolean hasOptional = false;
        private Boolean hasSet = false;
        private Boolean hasProcedure = false;
        private Boolean hasIPageable = false;
        private Boolean hasPagedResource = false;

        public Boolean getHasStream() {
            return hasStream;
        }

        public void setHasStream(Boolean hasStream) {
            this.hasStream = hasStream;
        }

        public Boolean getHasOptional() {
            return hasOptional;
        }

        public void setHasOptional(Boolean hasOptional) {
            this.hasOptional = hasOptional;
        }

        public Boolean getHasProcedure() {
            return hasProcedure;
        }

        public void setHasProcedure(Boolean hasProcedure) {
            this.hasProcedure = hasProcedure;
        }

        public Boolean getHasIPageable() {
            return hasIPageable;
        }

        public void setHasIPageable(Boolean hasIPageable) {
            this.hasIPageable = hasIPageable;
        }

        public Boolean getHasPagedResource() {
            return hasPagedResource;
        }

        public void setHasPagedResource(Boolean hasPagedResource) {
            this.hasPagedResource = hasPagedResource;
        }

        public Boolean getHasSet() {
            return hasSet;
        }

        public void setHasSet(Boolean hasSet) {
            this.hasSet = hasSet;
        }
    }

    public static class Method {
        private final String name;
        private final String returnType;
        private String description;
        private String annotationValue;
        private String sqlType;
        private String paramType;
        private String paramClassComment;
        private Set<Parameter> parameters = new LinkedHashSet<>();
        private String countQuery;
        private String pageConfig;
        protected Boolean enable = true;

        public Method(String returnType, String methodName) {
            this.returnType = returnType;
            this.name = methodName;
        }

        public void setParameters(Set<Parameter> parameters) {
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

        public Set<Parameter> getParameters() {
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

        public Boolean isEnable() {
            return enable;
        }

        public void setEnable(Boolean enable) {
            this.enable = enable;
        }

        public String getParamClassComment() {
            return paramClassComment;
        }

        public void setParamClassComment(String paramClassComment) {
            this.paramClassComment = paramClassComment;
        }

        public String getPageConfig() {
            return pageConfig;
        }

        public void setPageConfig(String pageConfig) {
            this.pageConfig = pageConfig;
        }
    }

    public static class Parameter {
        private final String name;
        private final String type;
        private final String comment;

        public Parameter(String name, String type, String comment) {
            this.name = name;
            this.type = type;
            this.comment = comment;
        }

        public String getName() {
            return name;
        }

        public String getComment() {
            return comment;
        }

        public String getType() {
            return type;
        }
    }

    public static class SimpleEntity {
        private String comment;
        private Set<String> lombok = new LinkedHashSet<>();
        private Set<Parameter> parameters = new LinkedHashSet<>();
        private String templateName = "class.java";

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public Set<Parameter> getParameters() {
            return parameters;
        }

        public void setParameters(Set<Parameter> parameters) {
            this.parameters = parameters;
        }

        public String getTemplateName() {
            return templateName;
        }

        public void setTemplateName(String templateName) {
            if (templateName != null) {
                this.templateName = templateName;
            }
        }

        public Set<String> getLombok() {
            return lombok;
        }

        public void setLombok(Set<String> lombok) {
            this.lombok = lombok;
        }
    }


}
