package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.DatasourceManager;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.DynamicSqlCalcDialog;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class OpenParamsDialogInXql extends PsiElementBaseIntentionAction implements Iconable {
    private final XQLConfigManager xqlConfigManager = XQLConfigManager.getInstance();

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
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
                    var sqlPath = file.getKey() + "." + m.group("name");
                    var dsResource = DatasourceManager.getInstance().getResource(project);
                    ApplicationManager.getApplication().invokeLater(() -> new DynamicSqlCalcDialog(sqlPath, config, dsResource).showAndGet());
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
            var xqlFile = element.getContainingFile();
            if (xqlFile == null || !xqlFile.isPhysical() || !xqlFile.isValid()) {
                return false;
            }
            var xqlVf = xqlFile.getVirtualFile();
            if (xqlVf == null) {
                return false;
            }
            if (!Objects.equals(xqlVf.getExtension(), "xql")) {
                return false;
            }
            var xqlFileManager = xqlConfigManager.getActiveXqlFileManager(xqlFile);
            if (xqlFileManager != null) {
                for (Map.Entry<String, String> file : xqlFileManager.getFiles().entrySet()) {
                    if (file.getValue().equals(xqlVf.toNioPath().toUri().toString())) {
                        var sqlPath = file.getKey() + "." + m.group("name");
                        if (xqlFileManager.contains(sqlPath)) {
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

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Execute dynamic sql";
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return "Execute dynamic sql";
    }

    @Override
    public Icon getIcon(int flags) {
        return AllIcons.Actions.Execute;
    }
}
