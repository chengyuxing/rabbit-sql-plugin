package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.github.chengyuxing.common.MostDateTime;
import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XQLMapperTemplateData;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.MapperGenerateForm;
import com.github.chengyuxing.plugin.rabbit.sql.util.NotificationUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.github.chengyuxing.sql.Args;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.utils.SqlGenerator;
import com.github.chengyuxing.sql.yaml.HyphenatedPropertyUtil;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MapperGenerateDialog extends DialogWrapper {
    private final static Logger log = Logger.getInstance(MapperGenerateDialog.class);
    private static final String PACKAGE_PATTERN = "[a-zA-Z]\\w*(\\.[a-zA-Z]\\w*)*";
    private final Project project;
    private final String alias;
    private final XQLConfigManager.Config config;
    private final XQLFileManager xqlFileManager;
    private final SqlGenerator sqlGenerator;
    private final MapperGenerateForm myForm;
    private final JBCheckBox bakiCheck;
    private final JTextField bakiInput;
    private final JTextField packageInput;

    public MapperGenerateDialog(@Nullable Project project, String alias, XQLConfigManager.Config config) {
        super(project, true);
        this.project = project;
        this.alias = alias;
        this.config = config;
        this.xqlFileManager = this.config.getXqlFileManager();
        this.sqlGenerator = this.config.getSqlGenerator();
        this.myForm = new MapperGenerateForm(project, this.alias, this.xqlFileManager);

        {
            bakiCheck = new JBCheckBox();
            bakiCheck.setText("Baki:");
            bakiCheck.setToolTipText("Specify the name if there are multiple baki in the spring context.");

            bakiInput = new JBTextField();
            bakiInput.setPreferredSize(new Dimension(70, (int) bakiInput.getPreferredSize().getHeight()));
            bakiInput.setEditable(false);

            bakiCheck.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    bakiInput.setEditable(bakiCheck.isSelected());
                }
            });
            packageInput = new JBTextField();
            packageInput.setPreferredSize(new Dimension(230, (int) packageInput.getPreferredSize().getHeight()));
            packageInput.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    setOKActionEnabled(isPackageValid());
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    setOKActionEnabled(isPackageValid());
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    setOKActionEnabled(isPackageValid());
                }
            });
        }

        setTitle("XQL Mapper Interface Generator");
        setOKButtonText("Generate");
        setCancelButtonText("Close");
        setSize(750, 300);
        setOKActionEnabled(isPackageValid());
        init();
    }

    boolean isPackageValid() {
        return this.packageInput.getText().trim().matches(PACKAGE_PATTERN);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return myForm;
    }

    @Override
    protected @Nullable JPanel createSouthAdditionalPanel() {
        var panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));

        panel.add(bakiCheck);
        panel.add(bakiInput);

        panel.add(new JBLabel("Package:"));
        panel.add(packageInput);
        return panel;
    }

    @Override
    protected void doOKAction() {
        var resource = this.xqlFileManager.getResource(alias);
        var packageName = packageInput.getText().trim();
        var templateData = new XQLMapperTemplateData(packageName, alias);
        templateData.setUser(System.getProperty("user.name"));
        templateData.setDate(MostDateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
        if (bakiCheck.isSelected()) {
            var bakiBean = bakiInput.getText().trim();
            if (bakiBean.isEmpty()) {
                bakiBean = "baki";
            }
            templateData.setBaki(bakiBean);
        }
        templateData.setDescription(resource.getDescription());

        var methods = new ArrayList<XQLMapperTemplateData.Method>();
        var entityImports = new LinkedHashSet<String>();
        var data = myForm.getData();
        data.forEach(row -> {
            var sqlName = row.get(0).toString();
            var methodName = row.get(1).toString().trim();
            if (methodName.isEmpty()) {
                methodName = sqlName.replace("_", "-");
                methodName = HyphenatedPropertyUtil.camelize(methodName);
                methodName = methodName.replaceAll("\\W", "");
            }

            var sqlType = row.get(2).toString();

            var paramType = row.get(3).toString().trim();
            if (paramType.isEmpty()) {
                paramType = "Map";
            }

            var returnTypes = row.get(4).toString().trim().split(" & ");
            if (returnTypes.length == 0) {
                returnTypes = new String[]{"List<T>"};
            }

            var returnGenericType = row.get(5).toString().trim();
            if (returnGenericType.isEmpty()) {
                returnGenericType = "DataRow";
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

            if (returnTypes.length == 1) {
                var method = new XQLMapperTemplateData.Method(replaceGenericT(returnTypes[0], returnGenericType), methodName);
                addMethod(methods, sqlName, methodName, sqlType, paramType, sql, params, method);
            } else {
                var newReturnTypes = new LinkedHashSet<String>();
                for (var returnType : returnTypes) {
                    newReturnTypes.add(replaceGenericT(returnType, returnGenericType));
                }
                for (var returnType : newReturnTypes) {
                    var extMethodName = methodName + returnTypeName(returnType, returnGenericType);
                    var method = new XQLMapperTemplateData.Method(replaceGenericT(returnType, returnGenericType), extMethodName);
                    addMethod(methods, sqlName, extMethodName, sqlType, paramType, sql, params, method);
                }
            }
        });

        templateData.setMethods(methods);
        templateData.setEntityImports(entityImports);

        var template = FileTemplateManager.getInstance(project).getInternalTemplate("xqlMapperInterface.java");
        try {
            var packages = packageName.split("\\.");

            var absFilename = config.getModulePath()
                    .resolve(Constants.SOURCE_ROOT)
                    .resolve(Path.of(packages[0], Arrays.copyOfRange(packages, 1, packages.length)))
                    .resolve(templateData.getMapperInterfaceName() + ".java");

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
            var vf = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(absFilename);
            if (Objects.nonNull(vf)) {
                vf.refresh(false, false);
            }
            dispose();
        } catch (IOException e) {
            NotificationUtil.showMessage(project, e.getMessage(), NotificationType.WARNING);
            log.warn(e);
        }
    }

    private void addMethod(ArrayList<XQLMapperTemplateData.Method> methods, String sqlName, String methodName, String sqlType, String paramType, XQLFileManager.Sql sql, Set<String> params, XQLMapperTemplateData.Method method) {
        method.setParamType(paramType);
        method.setDescription(sql.getDescription());
        if (!sqlName.equals(methodName)) {
            method.setAnnotationValue(sqlName);
        }
        method.setSqlType(sqlType);
        var newParams = new LinkedHashSet<>(params);
        if (method.getReturnType().startsWith("PagedResource<")) {
            newParams.add("page");
            newParams.add("size");
        }
        method.setParameters(newParams);

        methods.add(method);
    }

    private static String replaceGenericT(String returnType, String genericType) {
        if (returnType.equals("<T>")) {
            return genericType;
        }
        return returnType.replace("<T>", "<" + genericType + ">");
    }

    private static String returnTypeName(String returnType, String genericType) {
        if (returnType.equals("<T>")) {
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
