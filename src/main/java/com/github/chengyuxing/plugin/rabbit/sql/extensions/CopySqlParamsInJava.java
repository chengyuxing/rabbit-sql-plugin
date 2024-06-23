package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.github.chengyuxing.common.script.lexer.FlowControlLexer;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.extensions.support.SqlNameIntentionActionInJava;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.github.chengyuxing.sql.XQLFileManager;
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
import java.util.ArrayList;
import java.util.Map;

public class CopySqlParamsInJava extends SqlNameIntentionActionInJava implements Iconable {
    @Override
    public void invokeIfSuccess(Project project, PsiElement element, XQLConfigManager.Config config, String sqlName) {
        var sqlDefinition = config.getXqlFileManager().get(sqlName);
        for (String keyword : FlowControlLexer.KEYWORDS) {
            sqlDefinition = sqlDefinition.replaceAll("--\\s*" + keyword, keyword);
        }
        var namedParams = config.getSqlGenerator().generatePreparedSql(sqlDefinition, Map.of())
                .getItem2()
                .keySet()
                .stream()
                .filter(name -> !name.startsWith(XQLFileManager.DynamicSqlParser.FOR_VARS_KEY + "."))
                .distinct()
                .map(key -> "\"" + key + "\", " + key)
                .toList();

        var templateParams = StringUtil.getTemplateParameters(sqlDefinition, "", "")
                .stream()
                .distinct()
                .map(key -> "\"" + key + "\", " + key)
                .toList();

        if (namedParams.isEmpty() && templateParams.isEmpty()) {
            return;
        }

        var paramsGroup = new ArrayList<String>();

        if (!templateParams.isEmpty()) {
            paramsGroup.add("// template parameters\n" + String.join(",\n", templateParams));
        }

        if (!namedParams.isEmpty()) {
            paramsGroup.add("// named parameters\n" + String.join(",\n", namedParams));
        }

        var result = String.join(",\n", paramsGroup);

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(result), null);
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Copy sql params in java";
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return "Copy sql parameters to key-value pairs";
    }

    @Override
    public Icon getIcon(int i) {
        return AllIcons.Actions.Copy;
    }
}
