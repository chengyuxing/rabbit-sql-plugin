package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.NewSQLForm;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDocumentManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;

public class NewSQLDialog extends DialogWrapper {
    private final NewSQLForm newSQLForm;
    private final Project project;
    private final String alias;
    private final XQLConfigManager.Config config;

    public NewSQLDialog(@Nullable Project project, String alias, XQLConfigManager.Config config) {
        super(true);
        this.project = project;
        this.alias = alias;
        this.config = config;
        this.newSQLForm = new NewSQLForm();
        this.newSQLForm.setInputChanged(name -> {
            if (name.matches("[a-zA-Z][-\\w]*")) {
                setOKActionEnabled(true);
                this.newSQLForm.setMessage("");
            } else {
                setOKActionEnabled(false);
                this.newSQLForm.setMessage(HtmlUtil.toHtml(HtmlUtil.span("'" + name + "' is invalid.", HtmlUtil.Color.WARNING)));
            }
        });
        setOKActionEnabled(false);
        setSize(450, 103);
        setTitle("New SQL");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return newSQLForm;
    }

    @Override
    protected void doOKAction() {
        var data = newSQLForm.getData();
        var name = data.getItem1();
        var desc = data.getItem2().replaceAll("\\s+", "\n");
        var sqlReference = alias + "." + name;
        var xqlFileManager = config.getXqlFileManager();
        if (Objects.nonNull(xqlFileManager)) {
            if (xqlFileManager.contains(sqlReference)) {
                newSQLForm.setMessage(HtmlUtil.toHtml(HtmlUtil.span("'" + name + "' already exists.", HtmlUtil.Color.WARNING)));
                return;
            }
            var sqlFile = xqlFileManager.getResource(alias).getFilename();
            var sqlFileVf = VirtualFileManager.getInstance().findFileByNioPath(Path.of(URI.create(sqlFile)));
            if (Objects.isNull(sqlFileVf)) {
                return;
            }
            var doc = ProjectFileUtil.getDocument(project, sqlFileVf);
            if (Objects.isNull(doc)) {
                return;
            }
            dispose();
            ApplicationManager.getApplication().runWriteAction(() ->
                    WriteCommandAction.runWriteCommandAction(project, "Modify '" + sqlFileVf.getName() + "'", null, () -> {
                        var sqlFragment = "\n\n/*[" + name + "]*/";
                        if (!desc.trim().isEmpty()) {
                            sqlFragment += "\n/*#" + desc + "#*/";
                        }
                        sqlFragment += "\n\n;\n";
                        var lastIdx = doc.getTextLength();
                        doc.insertString(lastIdx, sqlFragment);
                        PsiDocumentManager.getInstance(project).commitDocument(doc);
                        FileDocumentManager.getInstance().saveDocument(doc);
                        PsiUtil.navigate2xqlFile(alias, name, config);
                    }));
        }
    }
}
