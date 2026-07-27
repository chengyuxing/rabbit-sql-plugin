package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.plugin.rabbit.sql.Helper;
import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.common.Global;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLMapperConfig;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.EntityTemplateData;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XQLMapperTemplateData;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.MapperGenerateForm;
import com.github.chengyuxing.plugin.rabbit.sql.util.*;
import com.github.chengyuxing.sql.Args;
import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFileManager;
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
import java.util.function.BiConsumer;

import static com.github.chengyuxing.plugin.rabbit.sql.common.Constants.FULLY_CLASS_PATTERN;
import static com.github.chengyuxing.plugin.rabbit.sql.common.Constants.PACKAGE_PATTERN;
import static com.github.chengyuxing.plugin.rabbit.sql.common.XQLMapperConfig.ParamSource.GENERATED;
import static com.github.chengyuxing.plugin.rabbit.sql.common.XQLMapperConfig.ParamSource.USER;
import static com.github.chengyuxing.plugin.rabbit.sql.ui.components.MapperGenerateForm.GENERIC_TYPES;
import static com.github.chengyuxing.plugin.rabbit.sql.ui.components.MapperGenerateForm.PARAM_TYPES;

public class MapperGenerateDialog extends DialogWrapper {
    private final static Logger log = Logger.getInstance(MapperGenerateDialog.class);
    private final Project project;
    private final String alias;
    private final XQLConfigManager.Config config;
    private final XQLFileManager xqlFileManager;
    private final MapperGenerateForm myForm;
    private final JButton message;
    private final Path configPath;
    private final XQLMapperConfig mapperConfig;

    public MapperGenerateDialog(@NotNull Project project, String alias, XQLConfigManager.Config config) {
        super(project, true);
        this.project = project;
        this.alias = alias;
        this.config = config;
        this.xqlFileManager = this.config.getXqlFileManager();
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

        setTitle(MessageBundle.message("ui.dialog.mapperGen.title", alias));
        setOKButtonText(MessageBundle.message("ui.dialog.mapperGen.ok"));
        setCancelButtonText(MessageBundle.message("confirm.close"));
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
        var packageName = myForm.getPackage();

        if (!PACKAGE_PATTERN.matcher(packageName).matches()) {
            myForm.selectConfigTab();
            setMessage(MessageBundle.message("package.invalid.message", packageName));
            return;
        }
        if (myForm.getPageKey().isEmpty() || myForm.getSizeKey().isEmpty()) {
            myForm.selectConfigTab();
            setMessage(MessageBundle.message("ui.dialog.mapperGen.error.pageSize"));
            return;
        }

        var mapperClass = myForm.getPackage() + "." + StringUtil.generateInterfaceMapperName(alias);
        var absFilename = ProjectFileUtil.createJavaFilePath(config, mapperClass);
        var isSame = Objects.equals(mapperConfig.getPackageName(), myForm.getPackage());
        if (!Files.exists(absFilename) || isSame) {
            doSaveConfiguration(mapperClass, absFilename);
            return;
        }
        setMessage(MessageBundle.message("overwrite.error.exists"));
        this.message.setToolTipText(MessageBundle.message("overwrite.error.exists.tooltip", mapperClass));
    }

    private void doSaveConfiguration(String mapperClass, Path absFile) {
        final var paramTypes4overwrite = new HashSet<String>();
        final var methodsCache = mapperConfig.getMethods();
        for (Vector<?> row : myForm.getData()) {
            var methodName = row.get(0).toString();
            var inputParamType = row.get(3).toString().trim();
            var inputReturnGenericType = row.get(5).toString().trim();

            if (!isValidParamType(inputParamType)) {
                setMessage(MessageBundle.message("classname.invalid", inputParamType));
                return;
            }
            if (!isValidReturnGenericType(inputReturnGenericType)) {
                setMessage(MessageBundle.message("classname.invalid", inputReturnGenericType));
                return;
            }

            var exists = methodsCache.get(methodName);
            var inputParamSource = detectParamSource(inputParamType, exists);

            var newMapperMethod = new XQLMapperConfig.XQLMethod();
            newMapperMethod.setEnable((Boolean) row.get(6));
            newMapperMethod.setSqlType(row.get(2).toString());
            newMapperMethod.setParamSource(inputParamSource);
            newMapperMethod.setParamType(inputParamType);
            newMapperMethod.setReturnType((XQLMapperConfig.ReturnType) row.get(4));
            newMapperMethod.setReturnGenericType(inputReturnGenericType);

            if (exists != null) {
                var paramMeta = exists.getParamMeta();
                if (paramMeta != null) {
                    newMapperMethod.setParamMeta(paramMeta);
                }
            }
            methodsCache.put(methodName, newMapperMethod);

            // excludes Map, @Args
            if (isUserCustomClass(inputParamType) && inputParamSource == GENERATED) {
                paramTypes4overwrite.add(inputParamType);
            }
        }

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

        // remove sqls if cache contains the changed sql name.
        methodsCache.entrySet().removeIf(e -> {
            var resource = xqlFileManager.getResource(alias);
            if (Objects.nonNull(resource)) {
                return !resource.getEntry().containsKey(e.getKey());
            }
            return true;
        });

        dispose();
        ProgressManager.getInstance().run(new Task.Backgroundable(project, MessageBundle.message("ui.dialog.mapperGen.ok.progress"), false) {
            private final Set<Path> refreshFiles = new HashSet<>();
            private final StringJoiner generated = new StringJoiner(", ");

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                try {
                    mapperConfig.saveTo(configPath);

                    var template = FileTemplateManager.getInstance(project).getInternalTemplate("xqlMapperInterface.java");
                    var templateData = XQLMapperTemplateData.of(mapperConfig, alias, config, absFile);
                    var result = template.getText(DataRow.ofEntity(templateData));

                    var pDir = absFile.getParent();
                    if (!Files.exists(pDir)) {
                        Files.createDirectories(pDir);
                    }
                    Files.writeString(absFile, result, StandardCharsets.UTF_8);
                    refreshFiles.add(absFile);
                    generated.add(mapperClass);

                    // generate user custom entity type files
                    var userEntityClasses = templateData.getUserEntities();
                    if (!userEntityClasses.isEmpty()) {
                        generateCustomEntityClass(userEntityClasses, paramTypes4overwrite, (clazz, file) -> {
                            refreshFiles.add(file);
                            generated.add(clazz);
                        });
                    }

                    var lsm = LocalFileSystem.getInstance();
                    refreshFiles.forEach(file -> {
                        var vf = lsm.refreshAndFindFileByNioFile(file);
                        if (vf != null) {
                            vf.refresh(false, false);
                        }
                    });
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public void onSuccess() {
                String message = MessageBundle.message("ui.dialog.mapperGen.save.generated") + " " + generated;
                NotificationUtil.showMessage(project, message, NotificationType.INFORMATION);
            }

            @Override
            public void onThrowable(@NotNull Throwable error) {
                NotificationUtil.showMessage(project, error.getMessage(), NotificationType.WARNING);
                log.warn(error);
            }
        });
    }

    private void setMessage(String message) {
        this.message.setVisible(true);
        this.message.setText(HtmlUtil.toHtml(HtmlUtil.span(message, HtmlUtil.Color.WARNING)));
    }

    private boolean isValidParamType(String s) {
        if (PARAM_TYPES.contains(s)) {
            return true;
        }
        return FULLY_CLASS_PATTERN.matcher(s).matches();
    }

    private boolean isValidReturnGenericType(String s) {
        if (GENERIC_TYPES.contains(s)) {
            return true;
        }
        return FULLY_CLASS_PATTERN.matcher(s).matches();
    }

    private boolean isUserCustomClass(String input) {
        return !PARAM_TYPES.contains(input);
    }

    private XQLMapperConfig.ParamSource detectParamSource(String inputParamType, XQLMapperConfig.XQLMethod exists) {
        if (!isUserCustomClass(inputParamType)) return GENERATED;
        var inputParamFile = ProjectFileUtil.createJavaFilePath(config, inputParamType);
        // 1. file not exists, generate
        if (!Files.exists(inputParamFile)) return GENERATED;
        var vf = VirtualFileManager.getInstance().findFileByNioPath(inputParamFile);
        if (exists != null && Objects.equals(inputParamType, exists.getParamType()) &&
                exists.getParamSource() == USER) {
            // the real file status priority gt than config
            if (ProjectFileUtil.isPluginGenerated(vf)) return GENERATED;
            return USER;
        }
        // at first generate action with an exists input class name, do not overwrite.
        if (exists == null) return USER;
        // another already exists user custom class, do not overwrite
        if (!Objects.equals(inputParamType, exists.getParamType())) return USER;
        // generated user class is modified by user, do not overwrite
        if (!ProjectFileUtil.isPluginGenerated(vf)) return USER;
        // file has comment: @RabbitSqlGenerated
        return GENERATED;
    }

    private void generateCustomEntityClass(Map<String, XQLMapperTemplateData.SimpleEntity> userEntityClasses, Set<String> entities4overwrite, BiConsumer<String, Path> generate) throws IOException {
        for (Map.Entry<String, XQLMapperTemplateData.SimpleEntity> entry : userEntityClasses.entrySet()) {
            Path file = ProjectFileUtil.createJavaFilePath(config, entry.getKey());
            var pDir = file.getParent();
            if (!Files.exists(pDir)) {
                Files.createDirectories(pDir);
            }
            var simpleEntity = entry.getValue();
            var tmpName = simpleEntity.getTemplateName();
            switch (tmpName) {
                case "class.java", "pagehelper.java" -> {
                    if (!Files.exists(file)) {
                        var template = FileTemplateManager.getInstance(project).getInternalTemplate(tmpName);
                        var args = Global.usefulArgs()
                                .add("clazz", TypeUtil.extractFullClassInfo(entry.getKey()));
                        var result = template.getText(args);
                        Files.writeString(file, result, StandardCharsets.UTF_8);
                        generate.accept(entry.getKey(), file);
                    }
                }
                case "entity.java" -> {
                    if (entities4overwrite.contains(entry.getKey())) {
                        var template = FileTemplateManager.getInstance(project).getInternalTemplate(tmpName);
                        var paramMeta = new XQLMapperConfig.XQLParamMeta();
                        paramMeta.setLombok(simpleEntity.getLombok());
                        paramMeta.setComment(simpleEntity.getComment());
                        paramMeta.setClassName(entry.getKey());
                        var params = new LinkedHashMap<String, XQLMapperConfig.XQLParam>();
                        simpleEntity.getParameters().forEach(p -> {
                            var xqlParam = new XQLMapperConfig.XQLParam();
                            xqlParam.setComment(p.getComment());
                            xqlParam.setType(p.getType());
                            xqlParam.setRequired(true);
                            params.put(p.getName(), xqlParam);
                        });
                        paramMeta.setParams(params);
                        var templateData = EntityTemplateData.of(paramMeta);
                        var args = Args.ofEntity(templateData);
                        var result = template.getText(args);
                        Files.writeString(file, result, StandardCharsets.UTF_8);
                        generate.accept(entry.getKey(), file);
                    }
                }
            }
        }
    }
}
