package com.github.chengyuxing.plugin.rabbit.sql.action;

import com.github.chengyuxing.plugin.rabbit.sql.XqlFileListenOnStartup;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaToken;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import static com.github.chengyuxing.plugin.rabbit.sql.XqlFileListenOnStartup.xqlFileManager;

public class CopySqlDefinition extends PsiElementBaseIntentionAction {
    private static final Logger log = Logger.getInstance(XqlFileListenOnStartup.class);

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        var sqlName = element.getText().replace("\"", "").substring(1);
        try {
            var sqlDefinition = xqlFileManager.get(sqlName);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(sqlDefinition), null);
        } catch (Exception e) {
            log.error(e);
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        if (element instanceof PsiJavaToken) {
            String sqlRef = element.getText().replace("\"", "");
            if (sqlRef.matches("^&\\w+\\..+")) {
                String sqlName = sqlRef.substring(1);
                return xqlFileManager.contains(sqlName);
            }
        }
        return false;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Copy sql definition";
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return "Copy sql definition";
    }

}
