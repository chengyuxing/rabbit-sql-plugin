package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.diagnostic.ControlFlowException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Objects;

import static com.github.chengyuxing.plugin.rabbit.sql.common.Constants.SQL_NAME_PATTERN;

public class CopySqlDefinition extends PsiElementBaseIntentionAction implements Iconable {
    private static final Logger log = Logger.getInstance(CopySqlDefinition.class);
    private final XQLConfigManager xqlConfigManager = XQLConfigManager.getInstance();

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        try {
            @SuppressWarnings("DataFlowIssue") var sqlName = ((PsiLiteralExpression) element.getParent()).getValue().toString().substring(1);
            var xqlFileManager = xqlConfigManager.getActiveXqlFileManager(project, element);
            var sqlDefinition = xqlFileManager.get(sqlName);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(sqlDefinition), null);
        } catch (Exception e) {
            if (e instanceof ControlFlowException) {
                throw e;
            }
            log.warn(e);
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        String sqlRef = PsiUtil.getJavaLiteral(element);
        if (sqlRef == null) {
            return false;
        }
        if (sqlRef.matches(SQL_NAME_PATTERN)) {
            String sqlName = sqlRef.substring(1);
            var xqlFileManager = xqlConfigManager.getActiveXqlFileManager(project, element);
            if (Objects.nonNull(xqlFileManager)) {
                return xqlFileManager.contains(sqlName);
            }
        }
        return false;
    }

    @Override
    public boolean startInWriteAction() {
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

    @Override
    public Icon getIcon(int flags) {
        return AllIcons.Actions.Copy;
    }
}
