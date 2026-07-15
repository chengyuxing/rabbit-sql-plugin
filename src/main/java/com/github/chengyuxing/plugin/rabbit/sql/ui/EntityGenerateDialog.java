package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.EntityGenerateFrom;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLMapperConfig;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.EntityTemplateData;
import com.github.chengyuxing.plugin.rabbit.sql.util.*;
import com.intellij.icons.AllIcons;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.notification.NotificationType;
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
    private String loadedClassName;

    public EntityGenerateDialog(@NotNull Project project, String alias, String sqlName, XQLConfigManager.Config config, Map<String, Set<String>> fieldMapping) {
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
                        this.loadedClassName = className;
                    }
                }
            }
            this.myForm = new EntityGenerateFrom(fieldMapping, params, lombok, getDisposable());
            this.myForm.setClassName(className);
            this.myForm.setComment(comment);
        }

        setTitle(MessageBundle.message("ui.dialog.entityGen.title", sqlName));
        setOKButtonText(MessageBundle.message("ui.dialog.entityGen.ok"));
        setOKButtonTooltip(MessageBundle.message("ui.dialog.entityGen.ok.tooltip"));
        setCancelButtonText(MessageBundle.message("ui.dialog.entityGen.cancel"));
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
    protected void doOKAction() {
        if (!FULLY_CLASS_PATTERN.matcher(myForm.getFullyClassName()).matches()) {
            myForm.selectConfigTab();
            message.setVisible(true);
            message.setText(HtmlUtil.toHtml(HtmlUtil.span(MessageBundle.message("ui.dialog.entityGen.error.classname", myForm.getFullyClassName()), HtmlUtil.Color.WARNING)));
            return;
        }

        var absFilename = ProjectFileUtil.createJavaFilePath(config, myForm.getFullyClassName());
        var isSameFile = Objects.equals(loadedClassName, myForm.getFullyClassName());
        if (!Files.exists(absFilename) || isSameFile) {
            doSaveConfiguration(true, () -> {
                String message = MessageBundle.message("ui.dialog.entityGen.save.generated") + " " + myForm.getFullyClassName();
                NotificationUtil.showMessage(project, message, NotificationType.INFORMATION);
            });
            dispose();
            return;
        }
        message.setVisible(true);
        message.setText(HtmlUtil.toHtml(HtmlUtil.span(MessageBundle.message("overwrite.error.exists"), HtmlUtil.Color.WARNING)));
        message.setToolTipText(MessageBundle.message("overwrite.error.exists.tooltip", myForm.getFullyClassName()));
    }

    private void doSaveConfiguration(boolean generateEntityClass, Runnable success) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, MessageBundle.message("ui.dialog.entityGen.ok.progress"), false) {
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
                xqlParamMeta.setClassName(myForm.getFullyClassName());
                xqlParamMeta.setLombok(myForm.getSelectedLombok());
                xqlParamMeta.setComment(myForm.getComment());
                xqlParamMeta.setParams(params);

                var exists = xqlMapperConfig.getMethods().get(sqlName);
                if (Objects.nonNull(exists)) {
                    exists.setParamMeta(xqlParamMeta);
                    exists.setParamType(xqlParamMeta.getClassName());
                } else {
                    var xqlMethod = new XQLMapperConfig.XQLMethod();
                    xqlMethod.setParamMeta(xqlParamMeta);
                    xqlMethod.setParamType(xqlParamMeta.getClassName());
                    xqlMapperConfig.getMethods().put(sqlName, xqlMethod);
                }

                try {
                    xqlMapperConfig.saveTo(configPath);
                    if (!generateEntityClass) {
                        return;
                    }
                    var absFilename = ProjectFileUtil.createJavaFilePath(config, myForm.getFullyClassName());
                    var classpackagePath = absFilename.getParent();
                    if (!Files.exists(classpackagePath)) {
                        Files.createDirectories(classpackagePath);
                    }
                    var template = FileTemplateManager.getInstance(project).getInternalTemplate("entity.java");
                    var templateData = EntityTemplateData.of(xqlParamMeta);

                    var result = template.getText(DataRow.ofEntity(templateData));
                    Files.writeString(absFilename, result, StandardCharsets.UTF_8);
                    var vf = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(absFilename);
                    if (Objects.nonNull(vf)) {
                        vf.refresh(false, false);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public void onSuccess() {
                success.run();
            }

            @Override
            public void onThrowable(@NotNull Throwable error) {
                NotificationUtil.showMessage(project, error.getMessage(), NotificationType.WARNING);
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
        return new BtnAction(MessageBundle.message("ui.dialog.entityGen.save.title"), MessageBundle.message("ui.dialog.entityGen.save.tooltip"), AllIcons.Actions.MenuSaveall) {

            @Override
            public void actionPerformed(ActionEvent e) {
                doSaveConfiguration(false, () -> {
                    message.setVisible(true);
                    message.setText(HtmlUtil.toHtml(HtmlUtil.span(MessageBundle.message("ui.dialog.entityGen.save.success"), HtmlUtil.Color.STRING)));
                });
            }
        };
    }
}
