package com.github.chengyuxing.plugin.rabbit.sql.extensions.support;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.java.extensions.OpenParamsDialogInJava;
import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.diagnostic.ControlFlowException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.github.chengyuxing.plugin.rabbit.sql.common.Constants.SQL_NAME_PATTERN;

public abstract class SqlNameIntentionActionInJvmLang extends PsiElementBaseIntentionAction {
    private static final Logger log = Logger.getInstance(OpenParamsDialogInJava.class);
    private final XQLConfigManager xqlConfigManager = XQLConfigManager.getInstance();

    protected String intentionTarget;

    public abstract void invokeIfSuccess(Project project, PsiElement element, XQLConfigManager.Config config, String sqlName);

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        try {
            var config = xqlConfigManager.getActiveConfig(element);
            if (Objects.isNull(config)) {
                return;
            }
            var sqlName = PsiUtil.getJvmLangLiteral(element);
            if (Objects.isNull(sqlName)) {
                return;
            }
            invokeIfSuccess(project, element, config, sqlName.substring(1));
        } catch (Exception e) {
            if (e instanceof ControlFlowException) {
                throw e;
            }
            log.warn(e);
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        String sqlRef = PsiUtil.getJvmLangLiteral(element);
        if (Objects.isNull(sqlRef)) {
            return false;
        }
        if (sqlRef.matches(SQL_NAME_PATTERN)) {
            String sqlName = sqlRef.substring(1);
            var xqlFileManager = xqlConfigManager.getActiveXqlFileManager(project, element);
            if (Objects.nonNull(xqlFileManager)) {
                var contains = xqlFileManager.contains(sqlName);
                if (contains) {
                    intentionTarget = sqlRef;
                }
                return contains;
            }
        }
        return false;
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
