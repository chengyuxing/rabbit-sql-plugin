package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.ModifyPropertyForm;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.NodeData;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.Constant;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.Property;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.regex.Pattern;

public class ModifyPropertyDialog extends DialogWrapper {
    private static final Pattern NAMED_PARAM_PREFIX_CHAR_PATTERN = Pattern.compile("[^'\")($=+\\-.<,!%&>*`/\\\\\\s\\w|]");
    private final Project project;
    private final NodeData nodeData;
    private final XQLConfigManager.Config config;
    private final ModifyPropertyForm form;

    public ModifyPropertyDialog(@Nullable Project project, NodeData nodeData) {
        super(true);
        this.project = project;
        this.nodeData = nodeData;
        this.config = nodeData instanceof Property property
                ? property.config()
                : nodeData instanceof Constant constant
                  ? constant.config()
                  : null;
        form = new ModifyPropertyForm();
        form.setInputChanged(value -> {
            if (nodeData instanceof Property property) {
                if (property.key().equals("named-param-prefix")) {
                    setOKActionEnabled(NAMED_PARAM_PREFIX_CHAR_PATTERN.matcher(value.trim()).matches());
                    return;
                }
                if (property.key().equals("charset")) {
                    setOKActionEnabled(!value.trim().isEmpty());
                }
            } else if (nodeData instanceof Constant) {
                setOKActionEnabled(true);
            }
        });
        setOKActionEnabled(false);
        setTitle(MessageBundle.message("ui.dialog.modify.title", nodeData.toString()));
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return form;
    }

    @Override
    protected void doOKAction() {
        var value = form.getValue();
        if (config == null) return;
        dispose();
        WriteCommandAction.runWriteCommandAction(project, MessageBundle.message("command.modify", config.getConfigName()), null, () -> {
            var doc = ProjectFileUtil.getDocument(project, config.getConfigVfs());
            if (doc == null) return;
            if (nodeData instanceof Property property) {
                updateProperty(doc, property, value);
                return;
            }
            Constant constant = (Constant) nodeData;
            updateConstant(doc, constant, value);
        });
    }

    private void updateConstant(Document doc, Constant constant, String newValue) {
        int ln = -1;
        for (int i = 0; i < doc.getLineCount(); i++) {
            var line = doc.getText(new TextRange(doc.getLineStartOffset(i), doc.getLineEndOffset(i)));
            if (line.matches("^constants: *")) {
                ln = i;
                break;
            }
        }
        if (ln != -1) {
            ln++;
            while (ln < doc.getLineCount()) {
                var line = doc.getText(new TextRange(doc.getLineStartOffset(ln), doc.getLineEndOffset(ln)));
                if (line.startsWith("  " + constant.name() + ": ")) {
                    int startIdx = doc.getLineStartOffset(ln) + constant.name().length() + 4;
                    int endIdx = doc.getLineEndOffset(ln);
                    doc.replaceString(startIdx, endIdx, StringUtil.safeYamlValue(newValue));
                    break;
                }
                if (line.matches("^\\w+.*")) {
                    break;
                }
                ln++;
            }
            PsiDocumentManager.getInstance(project).commitDocument(doc);
            FileDocumentManager.getInstance().saveDocument(doc);
        }
    }

    private void updateProperty(Document doc, Property property, String newValue) {
        int startIdx = -1;
        int endIdx = -1;
        for (int i = 0; i < doc.getLineCount(); i++) {
            var line = doc.getText(new TextRange(doc.getLineStartOffset(i), doc.getLineEndOffset(i)));
            if (line.startsWith(property.key() + ": ")) {
                startIdx = doc.getLineStartOffset(i) + property.key().length() + 2;
                endIdx = doc.getLineEndOffset(i);
                break;
            }
            if (line.startsWith("#" + property.key() + ": ")) {
                int lintStartIdx = doc.getLineStartOffset(i);
                doc.replaceString(lintStartIdx, lintStartIdx + 1, "");
                startIdx = doc.getLineStartOffset(i) + property.key().length() + 2;
                endIdx = doc.getLineEndOffset(i);
                break;
            }
        }
        var safeValue = StringUtil.safeYamlValue(newValue);
        if (startIdx < endIdx && startIdx >= 0) {
            doc.replaceString(startIdx, endIdx, safeValue);
        } else {
            int docEndIdx = doc.getTextLength();
            doc.insertString(docEndIdx, "\n" + property.key() + ": " + safeValue);
        }
        PsiDocumentManager.getInstance(project).commitDocument(doc);
        FileDocumentManager.getInstance().saveDocument(doc);
    }
}
