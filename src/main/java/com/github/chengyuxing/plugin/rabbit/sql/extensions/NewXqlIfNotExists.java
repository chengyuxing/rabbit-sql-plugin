package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.FeatureChecker;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.yml.YmlUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.github.chengyuxing.plugin.rabbit.sql.ui.NewXqlDialog;
import com.github.chengyuxing.plugin.rabbit.sql.util.*;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.icons.AllIcons;
import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.ControlFlowException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

import static com.github.chengyuxing.plugin.rabbit.sql.common.Constants.SQL_NAME_PATTERN;

public class NewXqlIfNotExists extends PsiElementBaseIntentionAction implements Iconable {
    private static final Logger log = Logger.getInstance(NewXqlIfNotExists.class);
    private final XQLConfigManager xqlConfigManager = XQLConfigManager.getInstance();

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        try {
            var sqlName = PsiUtil.getJvmLangLiteral(element);
            if (Objects.isNull(sqlName)) {
                return;
            }
            var config = xqlConfigManager.getActiveConfig(element);
            var xqlFileManager = xqlConfigManager.getActiveXqlFileManager(project, element);
            if (Objects.isNull(xqlFileManager)) {
                return;
            }
            var sqlRefParts = StringUtil.extraSqlReference(sqlName.substring(1));
            var alias = sqlRefParts.getItem1();
            var name = sqlRefParts.getItem2();
            var resource = xqlFileManager.getResource(alias);

            // do create xql file
            if (Objects.isNull(resource)) {
                if (Objects.isNull(config)) {
                    return;
                }
                var configVf = VirtualFileManager.getInstance().findFileByNioPath(config.getConfigPath());
                var doc = ProjectFileUtil.getDocument(project, configVf);
                if (Objects.isNull(doc)) {
                    return;
                }
                Map<String, String> anchors;
                if (FeatureChecker.isPluginEnabled(FeatureChecker.YML_PLUGIN_ID)) {
                    anchors = YmlUtil.getYmlAnchors(project, configVf);
                } else {
                    anchors = Map.of();
                    NotificationUtil.showMessage(project, "YAML plugin is not enabled. YAML-anchor features are disabled.", NotificationType.WARNING);
                }
                ApplicationManager.getApplication().invokeLater(() -> {
                    var d = new NewXqlDialog(project, config, doc, anchors);
                    d.setDefaultAlias(alias);
                    d.setEnableAutoGenAlias(false);
                    d.setTemplateContent("/*[" + name + "]*/\n\n" + xqlFileManager.getDelimiter() + "\n");
                    d.setWhenComplete(psi -> PsiUtil.navigate2xqlFile(psi, name));
                    d.initContent();
                    d.showAndGet();
                });
                return;
            }

            // do append xql fragment
            var sqlFile = resource.getFilename();
            if (!ProjectFileUtil.isLocalFileUri(sqlFile)) {
                NotificationUtil.showMessage(project, "only support local file", NotificationType.WARNING);
                return;
            }
            var sqlFileVf = VirtualFileManager.getInstance().findFileByNioPath(Path.of(URI.create(sqlFile)));
            if (Objects.isNull(sqlFileVf)) {
                return;
            }
            var doc = ProjectFileUtil.getDocument(project, sqlFileVf);
            if (Objects.isNull(doc)) {
                return;
            }
            ApplicationManager.getApplication().runWriteAction(() ->
                    WriteCommandAction.runWriteCommandAction(project, "Modify '" + sqlFileVf.getName() + "'", null, () -> {
                        var lastIdx = doc.getTextLength();
                        doc.insertString(lastIdx, "\n/*[" + name + "]*/\n\n" + xqlFileManager.getDelimiter() + "\n");
                        PsiDocumentManager.getInstance(project).commitDocument(doc);
                        FileDocumentManager.getInstance().saveDocument(doc);
                        PsiUtil.navigate2xqlFile(alias, name, config);
                    }));

        } catch (Exception e) {
            if (e instanceof ControlFlowException) {
                throw e;
            }
            log.warn(e);
        }
    }

    protected boolean isValidFileLanguage(Language language) {
        return language == JavaLanguage.INSTANCE;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        if (!isValidFileLanguage(element.getLanguage())) {
            return false;
        }
        String sqlRef = PsiUtil.getJvmLangLiteral(element);
        if (sqlRef == null) {
            return false;
        }
        if (sqlRef.matches(SQL_NAME_PATTERN)) {
            String sqlName = sqlRef.substring(1);
            var xqlFileManager = xqlConfigManager.getActiveXqlFileManager(project, element);
            if (Objects.nonNull(xqlFileManager)) {
                return !xqlFileManager.contains(sqlName);
            }
        }
        return false;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Smart create/append xql...";
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return "Smart create/append xql...";
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public Icon getIcon(int flags) {
        return AllIcons.Actions.AddMulticaret;
    }
}
