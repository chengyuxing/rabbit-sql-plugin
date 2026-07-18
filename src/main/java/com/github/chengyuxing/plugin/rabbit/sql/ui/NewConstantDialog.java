package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.NewConstantForm;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class NewConstantDialog extends DialogWrapper {
    private final Project project;
    private final XQLConfigManager.Config config;
    private final XQLFileManager xqlFileManager;
    private final NewConstantForm form;
    private final JButton message;

    public NewConstantDialog(@Nullable Project project, XQLConfigManager.Config config) {
        super(true);
        this.project = project;
        this.config = config;
        this.xqlFileManager = config.getXqlFileManager();
        this.message = new JButton();
        this.form = new NewConstantForm();
        this.form.setInputChanged(name -> {
            var nameInvalid = !name.matches("[a-zA-Z]\\w*");
            setOKActionEnabled(!nameInvalid);
        });
        setOKActionEnabled(false);
        setTitle(MessageBundle.message("ui.dialog.newConstant.title"));
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return form;
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
        var name = form.getName();
        var value = form.getValue();
        if (xqlFileManager.getConstants().containsKey(name)) {
            message.setVisible(true);
            message.setText(HtmlUtil.toHtml(HtmlUtil.span(MessageBundle.message("object.error.exists", name), HtmlUtil.Color.WARNING)));
            return;
        }
        dispose();
        WriteCommandAction.runWriteCommandAction(project, MessageBundle.message("command.modify", config.getConfigName()), null, () -> {
            var doc = ProjectFileUtil.getDocument(project, config.getConfigVfs());
            if (doc == null) return;
            int nodeIndex = -1;
            for (int i = 0; i < doc.getLineCount(); i++) {
                var line = doc.getText(new TextRange(doc.getLineStartOffset(i), doc.getLineEndOffset(i)));
                if (line.matches("^constants: *")) {
                    nodeIndex = doc.getLineEndOffset(i);
                    break;
                }
                if (line.matches("#constants: *")) {
                    int start = doc.getLineStartOffset(i);
                    doc.replaceString(start, start + 1, "");
                    nodeIndex = doc.getLineEndOffset(i);
                    break;
                }
            }
            var content = "  " + name + ": " + StringUtil.safeYamlValue(value) + "\n";
            if (nodeIndex != -1) {
                doc.insertString(nodeIndex + 1, content);
            } else {
                content = "\nconstants:\n" + content;
                int start = doc.getTextLength();
                doc.insertString(start, content);
            }
            PsiDocumentManager.getInstance(project).commitDocument(doc);
            FileDocumentManager.getInstance().saveDocument(doc);
        });
    }
}
