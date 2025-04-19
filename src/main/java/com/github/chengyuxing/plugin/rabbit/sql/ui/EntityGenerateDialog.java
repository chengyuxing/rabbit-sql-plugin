package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.MostDateTime;
import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.EntityGenerateFrom;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLMapperConfig;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.ClassTemplateData;
import com.github.chengyuxing.plugin.rabbit.sql.util.BtnAction;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.NotificationUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.intellij.icons.AllIcons;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

import static com.github.chengyuxing.plugin.rabbit.sql.common.Constants.FULLY_CLASS_PATTERN;

public class EntityGenerateDialog extends DialogWrapper {
    private final static Logger log = Logger.getInstance(EntityGenerateDialog.class);

    private final Project project;
    private final String sqlName;
    private final XQLConfigManager.Config config;
    private final EntityGenerateFrom myForm;
    private final JButton message;
    private final Path configPath;
    private final XQLMapperConfig xqlMapperConfig;

    public EntityGenerateDialog(@Nullable Project project, String alias, String sqlName, XQLConfigManager.Config config, Map<String, Set<String>> fieldMapping) {
        super(project, true);
        this.project = project;
        this.sqlName = sqlName;
        this.config = config;
        this.message = new JButton();
        this.configPath = XQLMapperConfig.getDefaultPath(config, this.config.getXqlFileManager().getResource(alias));

        {
            this.xqlMapperConfig = XQLMapperConfig.load(configPath);
            var methods = this.xqlMapperConfig.getMethods();
            String className = null;
            String comment = null;
            Set<String> lombok = new HashSet<>();
            Map<String, XQLMapperConfig.XQLParam> params = new HashMap<>();
            if (!methods.isEmpty()) {
                var method = methods.get(sqlName);
                if (method != null) {
                    var paramMeta = method.getParamMeta();
                    if (paramMeta != null) {
                        className = paramMeta.getClassName();
                        lombok = paramMeta.getLombok();
                        params = paramMeta.getParams();
                        comment = paramMeta.getComment();
                    }
                }
            }
            this.myForm = new EntityGenerateFrom(project, fieldMapping, params, lombok, getDisposable());
            this.myForm.setClassName(className);
            this.myForm.setComment(comment);
        }

        setTitle("[" + sqlName + "] Params Configuration");
        setOKButtonText("Generate");
        setOKButtonTooltip("Generate params entity class");
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

    private Path createEntityFilePath(String className) {
        var sourceRoot = config.getModulePath()
                .resolve(Constants.KT_SOURCE_ROOT);
        if (!Files.exists(sourceRoot)) {
            sourceRoot = config.getModulePath().resolve(Constants.JAVA_SOURCE_ROOT);
        }
        var packages = className.split("\\.");
        return sourceRoot
                .resolve(Path.of(packages[0], Arrays.copyOfRange(packages, 1, packages.length - 1)))
                .resolve(packages[packages.length - 1] + ".java");
    }

    @Override
    protected void doOKAction() {
        if (Objects.isNull(project)) {
            this.message.setVisible(true);
            this.message.setText(HtmlUtil.toHtml(HtmlUtil.span("Cannot find current project.", HtmlUtil.Color.WARNING)));
            return;
        }

        if (!myForm.getFullyClassName().matches(FULLY_CLASS_PATTERN)) {
            myForm.selectConfigTab();
            message.setVisible(true);
            message.setText(HtmlUtil.toHtml(HtmlUtil.span("Class name '" + myForm.getFullyClassName() + "' is invalid.", HtmlUtil.Color.WARNING)));
            return;
        }

        doSaveConfiguration(myForm.getFullyClassName(), paramMeta -> {
            var imports = new LinkedHashSet<String>();
            var fields = new LinkedHashSet<ClassTemplateData.Field>();

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

            try {
                var absFilename = createEntityFilePath(paramMeta.getClassName());
                var abs = absFilename.getParent();
                if (!Files.exists(abs)) {
                    Files.createDirectories(abs);
                }

                var template = FileTemplateManager.getInstance(project).getInternalTemplate("entity.java");

                var templateData = new ClassTemplateData(paramMeta.getClassName());
                templateData.setUser(System.getProperty("user.name"));
                templateData.setDate(MostDateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
                templateData.setImports(imports);
                templateData.setFields(fields);
                templateData.setLombok(paramMeta.getLombok());
                templateData.setComment(paramMeta.getComment());

                var result = template.getText(DataRow.ofEntity(templateData));
                Files.writeString(absFilename, result, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }, () -> ApplicationManager.getApplication().runWriteAction(() -> {
            var absFilename = createEntityFilePath(myForm.getFullyClassName());
            var vf = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(absFilename);
            if (Objects.nonNull(vf)) {
                vf.refresh(false, false);
            }
        }));
        dispose();
    }

    private void doSaveConfiguration(String className, Consumer<XQLMapperConfig.XQLParamMeta> then, Runnable onSuccess) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Generating params entity.", false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                progressIndicator.setIndeterminate(true);

                var params = new HashMap<String, XQLMapperConfig.XQLParam>();

                myForm.getFieldMappingData().forEach(v -> {
                    var xqlParam = new XQLMapperConfig.XQLParam();
                    xqlParam.setType(v.get(1).toString());
                    xqlParam.setComment(v.get(2).toString());
                    xqlParam.setRequired((Boolean) v.get(3));
                    params.put(v.get(0).toString(), xqlParam);
                });

                var xqlParamMeta = new XQLMapperConfig.XQLParamMeta();
                xqlParamMeta.setClassName(className);
                xqlParamMeta.setLombok(myForm.getSelectedLombok());
                xqlParamMeta.setComment(myForm.getComment());
                xqlParamMeta.setParams(params);

                var exists = xqlMapperConfig.getMethods().get(sqlName);
                if (Objects.nonNull(exists)) {
                    exists.setParamMeta(xqlParamMeta);
                } else {
                    var xqlMethod = new XQLMapperConfig.XQLMethod();
                    xqlMethod.setParamMeta(xqlParamMeta);
                    xqlMapperConfig.getMethods().put(sqlName, xqlMethod);
                }

                xqlMapperConfig.saveTo(configPath);

                then.accept(xqlParamMeta);
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

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{
                getCancelAction(),
                getSaveAction(),
                getOKAction(),
        };
    }

    private @NotNull AbstractAction getSaveAction() {
        return new BtnAction("Save", "Save these fields configuration.", AllIcons.Actions.MenuSaveall) {

            @Override
            public void actionPerformed(ActionEvent e) {
                doSaveConfiguration(null, paramMeta -> {
                }, () -> {
                    message.setVisible(true);
                    message.setText(HtmlUtil.toHtml(HtmlUtil.span("Saved successfully!", HtmlUtil.Color.STRING)));
                });
            }
        };
    }
}
