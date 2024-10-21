package com.github.chengyuxing.plugin.rabbit.sql.extensions.support;

import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.diagnostic.ControlFlowException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public abstract class SqlNameIntentionActionInXql extends PsiElementBaseIntentionAction {
    private static final Logger log = Logger.getInstance(SqlNameIntentionActionInXql.class);

    private final XQLConfigManager xqlConfigManager = XQLConfigManager.getInstance();

    protected String intentionTarget;

    public abstract void invokeIfSuccess(Project project, PsiElement element, XQLConfigManager.Config config, String sqlName);

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        try {
            var sqlNameTag = element.getText();
            var pattern = Pattern.compile(Constants.SQL_NAME_ANNOTATION_PATTERN);
            var m = pattern.matcher(sqlNameTag);
            if (m.find()) {
                var xqlFile = element.getContainingFile();
                if (xqlFile == null || !xqlFile.isValid() || !xqlFile.isPhysical()) {
                    return;
                }
                var xqlVf = xqlFile.getVirtualFile();
                if (xqlVf == null) {
                    return;
                }
                var config = xqlConfigManager.getActiveConfig(element);
                if (Objects.isNull(config)) {
                    return;
                }
                var xqlFileManager = config.getXqlFileManager();
                for (Map.Entry<String, String> file : xqlFileManager.getFiles().entrySet()) {
                    if (file.getValue().equals(xqlVf.toNioPath().toUri().toString())) {
                        var sqlPath = XQLFileManager.encodeSqlReference(file.getKey(),m.group("name"));
                        invokeIfSuccess(project, element, config, sqlPath);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof ControlFlowException) {
                throw e;
            }
            log.warn(e);
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var xqlVf = PsiUtilCore.getVirtualFile(element);
        if(Objects.isNull(xqlVf)) {
            return false;
        }
        if (!(element instanceof PsiComment)) {
            return false;
        }
        String sqlNameTag = element.getText();
        if (sqlNameTag == null) {
            return false;
        }
        var pattern = Pattern.compile(Constants.SQL_NAME_ANNOTATION_PATTERN);
        var m = pattern.matcher(sqlNameTag);
        if (m.matches()) {
            var xqlFileManager = xqlConfigManager.getActiveXqlFileManager(element);
            if (xqlFileManager != null) {
                for (Map.Entry<String, String> file : xqlFileManager.getFiles().entrySet()) {
                    if (file.getValue().equals(xqlVf.toNioPath().toUri().toString())) {
                        var sqlName = m.group("name");
                        var sqlPath = XQLFileManager.encodeSqlReference(file.getKey(),m.group("name"));
                        if (xqlFileManager.contains(sqlPath)) {
                            intentionTarget = sqlName;
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
