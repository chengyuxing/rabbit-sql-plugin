package com.github.chengyuxing.plugin.rabbit.sql.plugins.database.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.extensions.support.SqlNameIntentionActionInXql;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Objects;

public class CopySqlDefinitionInXql extends SqlNameIntentionActionInXql implements Iconable {
    @Override
    public void invokeIfSuccess(Project project, PsiElement element, XQLConfigManager.Config config, String sqlName) {
        var xqlFileManager = config.getXqlFileManager();
        var sqlDefinition = xqlFileManager.get(sqlName);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(sqlDefinition), null);
    }

    @Override
    protected boolean isValidFileExtension(String extension) {
        return Objects.equals(extension, "xql");
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Copy sql definition in xql";
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
