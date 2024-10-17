package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.github.chengyuxing.common.MostDateTime;
import com.github.chengyuxing.plugin.rabbit.sql.Helper;
import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XQLJavaType;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XQLMapperConfig;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XQLMapperTemplateData;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.MapperGenerateForm;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.ReturnTypesForm;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.NotificationUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.github.chengyuxing.sql.Args;
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
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class MapperGenerateDialog extends DialogWrapper {
    private final static Logger log = Logger.getInstance(MapperGenerateDialog.class);
    private static final String PACKAGE_PATTERN = "[a-zA-Z]\\w*(\\.[a-zA-Z]\\w*)*";
    private final Project project;
    private final String alias;
    private final XQLConfigManager.Config config;
    private final XQLFileManager xqlFileManager;
    private final SqlGenerator sqlGenerator;
    private final MapperGenerateForm myForm;
    private final JButton message;
    private final Path configPath;

    public MapperGenerateDialog(@Nullable Project project, String alias, XQLConfigManager.Config config) {
        super(project, true);
        this.project = project;
        this.alias = alias;
        this.config = config;
        this.xqlFileManager = this.config.getXqlFileManager();
        this.sqlGenerator = this.config.getSqlGenerator();
        this.message = new JButton();

        {
            // load mapper config
            var resource = xqlFileManager.getResource(alias);
            configPath = Path.of(URI.create(resource.getFilename() + ".rbm"));
            var mapperConfig = XQLMapperConfig.load(configPath);

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

    @Override
    protected @NonNls @Nullable String getHelpId() {
        return Helper.SPRING_INTERFACE_MAPPER_USAGE;
    }

    @Override
    protected void doOKAction() {
        var newMapperConfig = new XQLMapperConfig();
        var resource = this.xqlFileManager.getResource(alias);
        var baki = myForm.getBaki();
        var packageName = myForm.getPackage();
        var pageKey = myForm.getPageKey();
        var sizeKey = myForm.getSizeKey();

        if (!packageName.matches(PACKAGE_PATTERN)) {
            myForm.selectConfigTab();
            this.message.setVisible(true);
            this.message.setText(HtmlUtil.toHtml(HtmlUtil.span("Package '" + packageName + "' is invalid.", HtmlUtil.Color.WARNING)));
            return;
        }
        if (pageKey.isEmpty() || sizeKey.isEmpty()) {
            myForm.selectConfigTab();
            this.message.setVisible(true);
            this.message.setText(HtmlUtil.toHtml(HtmlUtil.span("Page or Size key is invalid.", HtmlUtil.Color.WARNING)));
            return;
        }

        newMapperConfig.setPackageName(packageName);
        newMapperConfig.setPageKey(pageKey);
        newMapperConfig.setSizeKey(sizeKey);

        var templateData = new XQLMapperTemplateData(packageName, alias);

        templateData.setUser(System.getProperty("user.name"));
        templateData.setDate(MostDateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
        if (baki != null) {
            var bakiBean = baki;
            if (bakiBean.isEmpty()) {
                bakiBean = "baki";
            }
            templateData.setBaki(bakiBean);
            newMapperConfig.setBaki(bakiBean);
        }
        templateData.setDescription(resource.getDescription());

        var methods = new ArrayList<XQLMapperTemplateData.Method>();
        var entityImports = new LinkedHashSet<String>();

        var packages = packageName.split("\\.");
        var sourceRoot = config.getModulePath()
                .resolve(Constants.KT_SOURCE_ROOT);
        if (!Files.exists(sourceRoot)) {
            sourceRoot = config.getModulePath().resolve(Constants.JAVA_SOURCE_ROOT);
        }

        var absFilename = sourceRoot
                .resolve(Path.of(packages[0], Arrays.copyOfRange(packages, 1, packages.length)))
                .resolve(templateData.getMapperInterfaceName() + ".java");

        var data = myForm.getData();

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Generate interface mapper.", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                try {
                    data.forEach(row -> {
                        var enable = (Boolean) row.get(6);

                        var sqlName = row.get(0).toString();

                        var mapperMethod = new XQLMapperConfig.XQLMethod();
                        mapperMethod.setEnable(enable);

                        var methodName = row.get(1).toString().trim();

                        if (methodName.isEmpty()) {
                            methodName = StringUtil.camelizeAndClean(methodName);
                        }

                        var sqlType = row.get(2).toString();
                        mapperMethod.setSqlType(sqlType);

                        var paramType = row.get(3).toString().trim();
                        mapperMethod.setParamType(paramType);

                        if (paramType.isEmpty()) {
                            paramType = XQLJavaType.Map.getValue();
                        }

                        var returnTypes = row.get(4).toString().trim();
                        mapperMethod.setReturnType(returnTypes);

                        var returnTypeList = ReturnTypesForm.splitReturnTypes(returnTypes);
                        if (returnTypeList.isEmpty()) {
                            returnTypeList = List.of(XQLJavaType.List.toString());
                        }

                        var returnGenericType = row.get(5).toString().trim();
                        mapperMethod.setReturnGenericType(returnGenericType);

                        if (returnGenericType.isEmpty()) {
                            returnGenericType = XQLJavaType.DataRow.getValue();
                        }

                        var sql = resource.getEntry().get(sqlName);
                        var sqlDefinition = sql.getContent();
                        var params = StringUtil.getParamsMappingInfo(sqlGenerator, sqlDefinition, true)
                                .keySet();

                        var paramUserEntity = getUserEntity(paramType);
                        if (Objects.nonNull(paramUserEntity)) {
                            entityImports.add(paramType);
                            paramType = paramUserEntity;
                        }
                        var genericUserEntity = getUserEntity(returnGenericType);
                        if (Objects.nonNull(genericUserEntity)) {
                            entityImports.add(returnGenericType);
                            returnGenericType = genericUserEntity;
                        }

                        if (returnTypeList.size() == 1) {
                            var method = new XQLMapperTemplateData.Method(replaceGenericT(returnTypeList.get(0), returnGenericType), methodName);
                            method.setEnable(enable);
                            addMethod(methods, sqlName, methodName, sqlType, paramType, sql, params, method, pageKey, sizeKey);
                        } else {
                            var newReturnTypes = new LinkedHashSet<String>();
                            for (var returnType : returnTypeList) {
                                newReturnTypes.add(replaceGenericT(returnType, returnGenericType));
                            }
                            for (var returnType : newReturnTypes) {
                                var extMethodName = methodName + returnTypeName(returnType, returnGenericType);
                                var method = new XQLMapperTemplateData.Method(replaceGenericT(returnType, returnGenericType), extMethodName);
                                method.setEnable(enable);
                                addMethod(methods, sqlName, extMethodName, sqlType, paramType, sql, params, method, pageKey, sizeKey);
                            }
                        }

                        newMapperConfig.getMethods().put(sqlName, mapperMethod);
                    });

                    templateData.setMethods(methods);
                    templateData.setEntityImports(entityImports);

                    var template = FileTemplateManager.getInstance(project).getInternalTemplate("xqlMapperInterface.java");
                    if (Files.exists(absFilename)) {
                        var userImports = new StringJoiner("\n");
                        var userMethods = new StringJoiner("\n");
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
                                        if (importContent.contains(" //CODE-END:imports")) {
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
                        if (importsBlockFlag == 2) {
                            templateData.setUserImports(userImports.toString());
                        }
                        if (methodsBlockFlag == 2) {
                            templateData.setUserMethods(userMethods.toString());
                        }
                    }

                    var abs = absFilename.getParent();
                    if (!Files.exists(abs)) {
                        Files.createDirectories(abs);
                    }

                    var args = Args.ofEntity(templateData);
                    var result = template.getText(args);
                    Files.writeString(absFilename, result, StandardCharsets.UTF_8);
                    generateMapperConfig(newMapperConfig);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public void onSuccess() {
                ApplicationManager.getApplication().invokeLater(() ->
                        ApplicationManager.getApplication().runWriteAction(() -> {
                            var vf = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(absFilename);
                            if (Objects.nonNull(vf)) {
                                vf.refresh(false, false);
                            }
                        }));
            }

            @Override
            public void onThrowable(@NotNull Throwable error) {
                ApplicationManager.getApplication().invokeLater(() -> NotificationUtil.showMessage(project, error.getMessage(), NotificationType.WARNING));
                log.warn(error);
            }
        });
        dispose();
    }

    private void generateMapperConfig(XQLMapperConfig config) {
        var yaml = new Yaml();
        try {
            var result = yaml.dumpAsMap(config);
            result = "# Rabbit-SQL-Plugin - XQL mapper generate configuration - DO NOT MODIFY\n\n" + result;
            Files.writeString(configPath, result, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn(e);
        }
    }

    private void addMethod(ArrayList<XQLMapperTemplateData.Method> methods, String sqlName, String methodName, String sqlType, String paramType, XQLFileManager.Sql sql, Set<String> params, XQLMapperTemplateData.Method method, String pageKey, String sizeKey) {
        method.setParamType(paramType);
        method.setDescription(sql.getDescription());
        if (!sqlName.equals(methodName)) {
            method.setAnnotationValue(sqlName);
        }
        method.setSqlType(sqlType);
        var newParams = new LinkedHashSet<>(params);
        if (method.getReturnType().equals(XQLJavaType.PagedResource.getValue()) || method.getReturnType().startsWith(XQLJavaType.PagedResource.getValue() + "<")) {
            newParams.add(pageKey);
            newParams.add(sizeKey);
        }
        method.setParameters(newParams);

        if (com.github.chengyuxing.common.utils.StringUtil.startsWiths(method.getReturnType(),
                XQLJavaType.PagedResource.getValue() + "<",
                XQLJavaType.PagedResource.getValue() + " ",
                XQLJavaType.IPageable.getValue())) {
            var cqName = sqlName + "Count";
            if (!xqlFileManager.contains(alias + "." + cqName)) {
                cqName = sqlName + "count";
            }
            if (!xqlFileManager.contains(alias + "." + cqName)) {
                cqName = sqlName + "-count";
            }
            if (xqlFileManager.contains(alias + "." + cqName)) {
                method.setCountQuery(cqName);
            }
        }

        methods.add(method);
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
        if (paramTypeOrGenericType.matches(PACKAGE_PATTERN)) {
            int lastDotIdx = paramTypeOrGenericType.lastIndexOf(".");
            if (lastDotIdx != -1) {
                return paramTypeOrGenericType.substring(lastDotIdx + 1);
            }
            return paramTypeOrGenericType;
        }
        return null;
    }
}
