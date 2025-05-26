package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.MostDateTime;
import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.common.utils.ObjectUtil;
import com.github.chengyuxing.plugin.rabbit.sql.Helper;
import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XQLJavaType;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLMapperConfig;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XQLMapperTemplateData;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.MapperGenerateForm;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.ReturnTypesForm;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.NotificationUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.utils.SqlGenerator;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.LocalFileSystem;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

import static com.github.chengyuxing.plugin.rabbit.sql.common.Constants.FULLY_CLASS_PATTERN;
import static com.github.chengyuxing.plugin.rabbit.sql.common.Constants.PACKAGE_PATTERN;

public class MapperGenerateDialog extends DialogWrapper {
    private final static Logger log = Logger.getInstance(MapperGenerateDialog.class);
    private final Project project;
    private final String alias;
    private final XQLConfigManager.Config config;
    private final XQLFileManager xqlFileManager;
    private final SqlGenerator sqlGenerator;
    private final MapperGenerateForm myForm;
    private final JButton message;
    private final Path configPath;
    private final XQLMapperConfig mapperConfig;

    public MapperGenerateDialog(@Nullable Project project, String alias, XQLConfigManager.Config config) {
        super(project, true);
        this.project = project;
        this.alias = alias;
        this.config = config;
        this.xqlFileManager = this.config.getXqlFileManager();
        this.sqlGenerator = this.config.getSqlGenerator();
        this.message = new JButton();
        this.configPath = XQLMapperConfig.getDefaultPath(config, xqlFileManager.getResource(alias));

        {
            this.mapperConfig = XQLMapperConfig.load(configPath);
            this.myForm = new MapperGenerateForm(project, this.alias, this.xqlFileManager, mapperConfig, getDisposable());
            myForm.setBaki(mapperConfig.getBaki());
            myForm.setPackage(mapperConfig.getPackageName());
            myForm.setPageKey(mapperConfig.getPageKey());
            myForm.setSizeKey(mapperConfig.getSizeKey());
        }

        setTitle("[ " + alias + " ] XQL Mapper Interface Generator");
        setOKButtonText("Generate");
        setCancelButtonText("Close");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return myForm;
    }

    @Override
    protected @Nullable JPanel createSouthAdditionalPanel() {
        var panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
        message.setVisible(false);
        panel.add(message);
        return panel;
    }

    private Path createMapperFilePath(String packageName) {
        var sourceRoot = config.getModulePath()
                .resolve(Constants.KT_SOURCE_ROOT);
        if (!Files.exists(sourceRoot)) {
            sourceRoot = config.getModulePath().resolve(Constants.JAVA_SOURCE_ROOT);
        }

        var packages = packageName.split("\\.");
        return sourceRoot
                .resolve(Path.of(packages[0], Arrays.copyOfRange(packages, 1, packages.length)))
                .resolve(StringUtil.generateInterfaceMapperName(this.alias) + ".java");
    }

    @Override
    protected @NonNls @Nullable String getHelpId() {
        return Helper.SPRING_INTERFACE_MAPPER_USAGE;
    }

    @Override
    protected void doOKAction() {
        if (Objects.isNull(project)) {
            this.message.setText(HtmlUtil.toHtml(HtmlUtil.span("Cannot find current project.", HtmlUtil.Color.WARNING)));
            return;
        }

        var packageName = myForm.getPackage();

        if (!packageName.matches(PACKAGE_PATTERN)) {
            myForm.selectConfigTab();
            this.message.setVisible(true);
            this.message.setText(HtmlUtil.toHtml(HtmlUtil.span("Package '" + packageName + "' is invalid.", HtmlUtil.Color.WARNING)));
            return;
        }
        if (myForm.getPageKey().isEmpty() || myForm.getSizeKey().isEmpty()) {
            myForm.selectConfigTab();
            this.message.setVisible(true);
            this.message.setText(HtmlUtil.toHtml(HtmlUtil.span("Page or Size key is invalid.", HtmlUtil.Color.WARNING)));
            return;
        }

        doSaveConfiguration(config -> {
            try {
                var resource = this.xqlFileManager.getResource(alias);

                var classImports = new LinkedHashSet<>(Set.of(
                        List.class.getName(),
                        Map.class.getName(),
                        Set.class.getName()
                ));
                var methods = new ArrayList<XQLMapperTemplateData.Method>();

                for (Map.Entry<String, XQLMapperConfig.XQLMethod> entry : config.getMethods().entrySet()) {
                    String sqlName = entry.getKey();
                    XQLMapperConfig.XQLMethod method = entry.getValue();

                    if (!method.getEnable()) {
                        continue;
                    }

                    var paramType = method.getParamType();
                    if (paramType.isEmpty()) {
                        paramType = XQLJavaType.Map.getValue();
                    }
                    var paramUserEntity = getUserEntity(paramType);
                    if (Objects.nonNull(paramUserEntity)) {
                        classImports.add(paramType);
                        paramType = paramUserEntity;
                    }

                    var returnTypes = method.getReturnType();
                    var returnGenericType = method.getReturnGenericType();
                    var methodName = StringUtil.camelizeAndClean(sqlName);
                    var sqlType = method.getSqlType();
                    var sql = resource.getEntry().get(sqlName);
                    var sqlParamNames = StringUtil.getParamsMappingInfo(sqlGenerator, sql.getContent(), false)
                            .keySet();
                    var paramMeta = method.getParamMeta();
                    var sqlParamObj = paramMeta == null ? Map.<String, XQLMapperConfig.XQLParam>of() : paramMeta.getParams();

                    var genericUserEntity = getUserEntity(returnGenericType);
                    if (Objects.nonNull(genericUserEntity)) {
                        classImports.add(returnGenericType);
                        returnGenericType = genericUserEntity;
                    }

                    var returnTypeList = ReturnTypesForm.splitReturnTypes(returnTypes);
                    if (returnTypeList.isEmpty()) {
                        returnTypeList = List.of(XQLJavaType.List.toString());
                    }
                    if (returnTypeList.size() == 1) {
                        var methodData = new XQLMapperTemplateData.Method(replaceGenericT(returnTypeList.get(0), returnGenericType), methodName);
                        methodData.setEnable(method.getEnable());
                        methodData.setParamType(paramType);
                        methodData.setDescription(sql.getDescription());
                        methodData.setSqlType(sqlType);
                        methodData.setCountQuery(detectCountQuery(sqlName, methodData));
                        if (!sqlName.equals(methodName)) {
                            methodData.setAnnotationValue(sqlName);
                        }
                        if (Objects.nonNull(paramMeta)) {
                            methodData.setParamClassComment(paramMeta.getComment());
                        }
                        var newParams = collectParams(sqlParamNames, sqlParamObj, methodData, config);
                        methodData.setParameters(newParams.getItem1());

                        classImports.addAll(newParams.getItem2());
                        methods.add(methodData);
                    } else {
                        var newReturnTypes = new LinkedHashSet<String>();
                        for (var returnType : returnTypeList) {
                            newReturnTypes.add(replaceGenericT(returnType, returnGenericType));
                        }
                        for (var returnType : newReturnTypes) {
                            var extMethodName = methodName + returnTypeName(returnType, returnGenericType);
                            var methodData = new XQLMapperTemplateData.Method(replaceGenericT(returnType, returnGenericType), extMethodName);
                            methodData.setEnable(method.getEnable());
                            methodData.setParamType(paramType);
                            methodData.setDescription(sql.getDescription());
                            methodData.setSqlType(sqlType);
                            methodData.setCountQuery(detectCountQuery(sqlName, methodData));

                            methodData.setAnnotationValue(sqlName);
                            if (Objects.nonNull(paramMeta)) {
                                methodData.setParamClassComment(paramMeta.getComment());
                            }
                            var newParams = collectParams(sqlParamNames, sqlParamObj, methodData, config);
                            methodData.setParameters(newParams.getItem1());

                            classImports.addAll(newParams.getItem2());
                            methods.add(methodData);
                        }
                    }
                }

                var absFilename = createMapperFilePath(config.getPackageName());
                var abs = absFilename.getParent();
                if (!Files.exists(abs)) {
                    Files.createDirectories(abs);
                }

                var userImports = new StringJoiner("\n");
                var userMethods = new StringJoiner("\n");

                if (Files.exists(absFilename)) {
                    var importsBlockFlag = 0;
                    var methodsBlockFlag = 0;
                    try (var reader = Files.newBufferedReader(absFilename)) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (importsBlockFlag == 2 && methodsBlockFlag == 2) {
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

                var template = FileTemplateManager.getInstance(project).getInternalTemplate("xqlMapperInterface.java");

                var templateData = new XQLMapperTemplateData(config.getPackageName(), alias);
                templateData.setUserImports(userImports.toString().trim());
                templateData.setUserMethods(userMethods.toString().trim());
                templateData.setUser(System.getProperty("user.name"));
                templateData.setDate(MostDateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
                templateData.setBaki(config.getBaki());
                templateData.setDescription(resource.getDescription());
                templateData.setMethods(methods);
                templateData.setClassImports(classImports);

                var result = template.getText(DataRow.ofEntity(templateData));
                Files.writeString(absFilename, result, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }, () -> ApplicationManager.getApplication().runWriteAction(() -> {
            var absFilename = createMapperFilePath(myForm.getPackage());
            var vf = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(absFilename);
            if (Objects.nonNull(vf)) {
                vf.refresh(false, false);
            }
        }));
        dispose();
    }

    private void doSaveConfiguration(Consumer<XQLMapperConfig> then, Runnable onSuccess) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Generating interface mapper.", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);

                mapperConfig.setPackageName(myForm.getPackage());
                mapperConfig.setPageKey(myForm.getPageKey());
                mapperConfig.setSizeKey(myForm.getSizeKey());

                var baki = myForm.getBaki();
                if (baki != null) {
                    var bakiBean = baki;
                    if (bakiBean.isEmpty()) {
                        bakiBean = "baki";
                    }
                    mapperConfig.setBaki(bakiBean);
                }

                myForm.getData().forEach(row -> {
                    var mapperMethod = new XQLMapperConfig.XQLMethod();
                    mapperMethod.setEnable((Boolean) row.get(6));
                    mapperMethod.setSqlType(row.get(2).toString());
                    mapperMethod.setParamType(row.get(3).toString().trim());
                    mapperMethod.setReturnType(row.get(4).toString().trim());
                    mapperMethod.setReturnGenericType(row.get(5).toString().trim());

                    var exists = mapperConfig.getMethods().get(row.get(0).toString());
                    if (Objects.nonNull(exists)) {
                        var paramMeta = exists.getParamMeta();
                        if (Objects.nonNull(paramMeta)) {
                            mapperMethod.setParamMeta(paramMeta);
                        }
                    }

                    mapperConfig.getMethods().put(row.get(0).toString(), mapperMethod);
                });
                // remove sqls if cache contains the changed sql name.
                mapperConfig.getMethods().entrySet().removeIf(e -> {
                    var resource = xqlFileManager.getResource(alias);
                    if (Objects.nonNull(resource)) {
                        return !resource.getEntry().containsKey(e.getKey());
                    }
                    return true;
                });

                mapperConfig.saveTo(configPath);
                then.accept(mapperConfig);
            }

            @Override
            public void onSuccess() {
                ApplicationManager.getApplication().invokeLater(onSuccess);
            }

            @Override
            public void onThrowable(@NotNull Throwable error) {
                ApplicationManager.getApplication().invokeLater(() -> NotificationUtil.showMessage(project, error.getMessage(), NotificationType.WARNING));
                log.warn(error);
            }
        });
    }

    private Pair<Set<XQLMapperTemplateData.Parameter>, Set<String>> collectParams(Set<String> paramNames, Map<String, XQLMapperConfig.XQLParam> paramObjects, XQLMapperTemplateData.Method method, XQLMapperConfig config) {
        var newParams = new LinkedHashSet<XQLMapperTemplateData.Parameter>();
        var imports = new LinkedHashSet<String>();
        if (paramObjects.isEmpty()) {
            for (var param : paramNames) {
                newParams.add(new XQLMapperTemplateData.Parameter(param, Object.class.getSimpleName(), ""));
            }
        } else {
            for (var paramName : paramNames) {
                var paramObj = paramObjects.get(paramName);
                if (Objects.isNull(paramObj)) {
                    newParams.add(new XQLMapperTemplateData.Parameter(paramName, Object.class.getSimpleName(), ""));
                    continue;
                }
                if (Objects.equals(paramObj.getRequired(), false)) {
                    continue;
                }

                String shortType = Object.class.getSimpleName();

                var type = paramObj.getType();
                if (Objects.nonNull(type)) {
                    shortType = type;
                    if (type.contains(".")) {
                        var typeNameAndPackage = StringUtil.getTypeAndPackagePath(type);
                        shortType = typeNameAndPackage.getItem1();
                        imports.add(typeNameAndPackage.getItem2());
                    }
                }

                newParams.add(new XQLMapperTemplateData.Parameter(
                        paramName,
                        shortType,
                        ObjectUtil.coalesce(paramObj.getComment(), ""))
                );
            }
        }

        if (method.getReturnType().equals(XQLJavaType.PagedResource.getValue()) || method.getReturnType().startsWith(XQLJavaType.PagedResource.getValue() + "<")) {
            newParams.add(new XQLMapperTemplateData.Parameter(config.getPageKey(), int.class.getSimpleName(), "page number"));
            newParams.add(new XQLMapperTemplateData.Parameter(config.getSizeKey(), int.class.getSimpleName(), "page size"));
        }
        return Pair.of(newParams, imports);
    }

    private String detectCountQuery(String sqlName, XQLMapperTemplateData.Method method) {
        if (com.github.chengyuxing.common.utils.StringUtil.startsWiths(method.getReturnType(),
                XQLJavaType.PagedResource.getValue() + "<",
                XQLJavaType.PagedResource.getValue() + " ",
                XQLJavaType.IPageable.getValue())) {
            var cqName = sqlName + "Count";
            if (!xqlFileManager.contains(XQLFileManager.encodeSqlReference(alias, cqName))) {
                cqName = sqlName + "count";
            }
            if (!xqlFileManager.contains(XQLFileManager.encodeSqlReference(alias, cqName))) {
                cqName = sqlName + "-count";
            }
            if (!xqlFileManager.contains(XQLFileManager.encodeSqlReference(alias, cqName))) {
                cqName = sqlName + "_count";
            }
            if (xqlFileManager.contains(XQLFileManager.encodeSqlReference(alias, cqName))) {
                return cqName;
            }
        }
        return null;
    }

    private static String replaceGenericT(String returnType, String genericType) {
        if (returnType.equals(XQLJavaType.GenericT.getValue())) {
            return genericType;
        }
        return returnType.replace(XQLJavaType.GenericT.getValue(), "<" + genericType + ">");
    }

    private static String returnTypeName(String returnType, String genericType) {
        if (returnType.equals(XQLJavaType.GenericT.getValue())) {
            return genericType;
        }
        if (returnType.contains("<")) {
            return returnType.substring(0, returnType.indexOf("<"));
        }
        return returnType;
    }

    private static String getUserEntity(String paramTypeOrGenericType) {
        if (MapperGenerateForm.GENERIC_TYPES.contains(paramTypeOrGenericType)) {
            return null;
        }
        if (MapperGenerateForm.PARAM_TYPES.contains(paramTypeOrGenericType)) {
            return null;
        }
        if (paramTypeOrGenericType.matches(FULLY_CLASS_PATTERN)) {
            int lastDotIdx = paramTypeOrGenericType.lastIndexOf(".");
            return paramTypeOrGenericType.substring(lastDotIdx + 1);
        }
        return null;
    }
}
