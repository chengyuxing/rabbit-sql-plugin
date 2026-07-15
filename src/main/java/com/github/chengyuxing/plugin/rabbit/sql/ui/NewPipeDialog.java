package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.github.chengyuxing.plugin.rabbit.sql.Helper;
import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.common.Global;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.NewPipeForm;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.NotificationUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.TypeUtil;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import static com.github.chengyuxing.plugin.rabbit.sql.common.Constants.PACKAGE_PATTERN;

public class NewPipeDialog extends DialogWrapper {
    private final static Logger log = Logger.getInstance(NewPipeDialog.class);

    private final Project project;
    private final XQLConfigManager.Config config;
    private final NewPipeForm form;

    public NewPipeDialog(@Nullable Project project, XQLConfigManager.Config config) {
        super(true);
        this.project = project;
        this.config = config;
        this.form = new NewPipeForm();
        this.form.setMessage(formatPath("<IPipe>"));
        this.form.setInputChanged(p -> {
            var nameInvalid = !p.getItem2().matches("[a-zA-Z]\\w*");
            var packageInvalid = !PACKAGE_PATTERN.matcher(p.getItem1()).matches();
            setOKActionEnabled(!nameInvalid && !packageInvalid);
            if (packageInvalid) {
                showWarn(MessageBundle.message("package.invalid.message", p.getItem1()));
                return;
            }
            this.form.setMessage(formatPath(p.getItem1()));
        });
        setOKActionEnabled(false);
        setTitle("New Pipe Class");
        init();
    }

    private void showWarn(String message) {
        this.form.setMessage(HtmlUtil.toHtml(HtmlUtil.span(message, HtmlUtil.Color.WARNING)));
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return form;
    }

    private String formatPath(String name) {
        var absFilename = ProjectFileUtil.createJavaFilePath(config, name);
        return ProjectFileUtil.formatPath(config.getModulePath().getParent().relativize(absFilename));
    }

    @Override
    protected void doOKAction() {
        var pipeName = form.getPipeName();
        var resultType = form.getResultType();

        var absFilename = ProjectFileUtil.createJavaFilePath(config, form.getFullClassName());
        var classInfo = TypeUtil.extractFullClassInfo(form.getFullClassName());

        var args = Global.usefulArgs()
                .add("clazz", classInfo)
                .add("resultType", resultType);

        if (config.getXqlFileManagerConfig().getPipes().containsKey(pipeName)) {
            showWarn(MessageBundle.message("ui.dialog.newPipe.ok.error.alias", pipeName));
            return;
        }
        // whatever do not overwrite the exists file
        if (Files.exists(absFilename)) {
            showWarn(MessageBundle.message("file.error.exists", form.getFullClassName()));
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(project, MessageBundle.message("ui.dialog.newPipe.ok.progress"), false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    var clazz = FileTemplateManager.getInstance(project).getInternalTemplate("pipe.java");
                    var path = absFilename.getParent();
                    if (!Files.exists(path)) {
                        Files.createDirectories(path);
                    }
                    var template = clazz.getText(args);
                    Files.writeString(absFilename, template, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public void onSuccess() {
                dispose();
                ProjectFileUtil.openFile(project, absFilename, true);
                String message = MessageBundle.message("ui.dialog.entityGen.save.generated") + " " + form.getFullClassName();
                NotificationUtil.showMessage(project, message, NotificationType.INFORMATION);
                WriteCommandAction.runWriteCommandAction(project, MessageBundle.message("command.modify", config.getConfigName()), null, () -> {
                    var doc = ProjectFileUtil.getDocument(project, config.getConfigVfs());
                    if (doc == null) return;
                    int nodeIndex = -1;
                    for (int i = 0; i < doc.getLineCount(); i++) {
                        var line = doc.getText(new TextRange(doc.getLineStartOffset(i), doc.getLineEndOffset(i)));
                        if (line.equals("pipes:")) {
                            nodeIndex = doc.getLineEndOffset(i);
                            break;
                        }
                        if (line.equals("#pipes:")) {
                            int start = doc.getLineStartOffset(i);
                            doc.replaceString(start, start + 1, "");
                            nodeIndex = doc.getLineEndOffset(i);
                            break;
                        }
                    }
                    var content = "  " + pipeName + ": " + form.getFullClassName() + "\n";
                    if (nodeIndex != -1) {
                        doc.insertString(nodeIndex + 1, content);
                    } else {
                        content = "pipes:\n" + content;
                        int start = doc.getTextLength();
                        if (start != 0) {
                            content = "\n" + content;
                        }
                        doc.insertString(start, content);
                    }
                    PsiDocumentManager.getInstance(project).commitDocument(doc);
                    FileDocumentManager.getInstance().saveDocument(doc);
                });
            }

            @Override
            public void onThrowable(@NotNull Throwable error) {
                showWarn(error.getMessage());
                log.warn(error);
            }
        });
    }

    @Override
    protected @NonNls @Nullable String getHelpId() {
        return Helper.XQL_FILE_MANAGER_PIPE;
    }
}
