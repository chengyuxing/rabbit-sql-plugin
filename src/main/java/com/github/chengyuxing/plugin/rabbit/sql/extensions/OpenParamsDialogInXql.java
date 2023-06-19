package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.DatasourceCache;
import com.github.chengyuxing.plugin.rabbit.sql.common.ResourceCache;
import com.github.chengyuxing.plugin.rabbit.sql.ui.DynamicSqlCalcDialog;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.regex.Pattern;

public class OpenParamsDialogInXql extends PsiElementBaseIntentionAction {
    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        var sqlNameTag = element.getText();
        var pattern = Pattern.compile(Constants.SQL_NAME_ANNOTATION_PATTERN);
        var m = pattern.matcher(sqlNameTag);
        if (m.find()) {
            var sqlName = m.group("name");
            var xqlFile = element.getContainingFile();
            var xqlVf = xqlFile.getVirtualFile();
            var resource = ResourceCache.getInstance().getResource(xqlFile);
            var xqlFileManager = resource.getXqlFileManager();
            for (Map.Entry<String, String> file : xqlFileManager.getFiles().entrySet()) {
                if (file.getValue().equals(xqlVf.toNioPath().toUri().toString())) {
                    var sqlPath = file.getKey() + "." + sqlName;
                    var dsResource = DatasourceCache.getInstance().getResource(project);
                    ApplicationManager.getApplication().invokeLater(() -> new DynamicSqlCalcDialog(sqlPath, resource, dsResource).showAndGet());
                    return;
                }
            }
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        if (!(element instanceof PsiComment)) {
            return false;
        }
        String sqlNameTag = element.getText();
        if (sqlNameTag == null) {
            return false;
        }
        var pattern = Pattern.compile(Constants.SQL_NAME_ANNOTATION_PATTERN);
        var m = pattern.matcher(sqlNameTag);
        if (m.find()) {
            var sqlName = m.group("name");
            var xqlFile = element.getContainingFile();
            if (xqlFile == null || !xqlFile.isPhysical() || !xqlFile.isValid()) {
                return false;
            }
            var resource = ResourceCache.getInstance().getResource(xqlFile);
            if (resource != null) {
                var xqlFileManager = resource.getXqlFileManager();
                for (Map.Entry<String, String> file : xqlFileManager.getFiles().entrySet()) {
                    if (file.getValue().equals(xqlFile.getVirtualFile().toNioPath().toUri().toString())) {
                        var sqlPath = file.getKey() + "." + sqlName;
                        if (resource.getXqlFileManager().contains(sqlPath)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Test dynamic sql";
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return "Test dynamic sql";
    }
}
