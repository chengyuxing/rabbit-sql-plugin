package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.StatisticsForm;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatisticsDialog extends DialogWrapper {
    private final StatisticsForm statisticsForm;

    public StatisticsDialog(@Nullable Project project) {
        super(true);
        XQLConfigManager xqlConfigManager = XQLConfigManager.getInstance();
        this.statisticsForm = new StatisticsForm(project, xqlConfigManager.getConfigMap(project));
        this.statisticsForm.setClickEmptyTableTextLink(module -> {
            dispose();
            var primaryAbsFilename = module.resolve(Constants.CONFIG_PATH);
            ProjectFileUtil.createXqlConfigByTemplate(project, primaryAbsFilename, () ->
                    ApplicationManager.getApplication().runWriteAction(() ->
                            ProjectFileUtil.openFile(project, primaryAbsFilename, true)));
        });
        setTitle("Statistics");
        setSize(650, 320);
        init();
    }

    @Override
    protected @NotNull Action getOKAction() {
        var a = super.getOKAction();
        a.putValue("Name", "Copy and Close");
        return a;
    }

    @Override
    protected @NotNull Action getCancelAction() {
        var a = super.getCancelAction();
        a.putValue("Name", "Close");
        return a;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return statisticsForm;
    }

    @Override
    protected void doOKAction() {
        var p = statisticsForm.getDisplayData();
        if (Objects.isNull(p)) {
            return;
        }
        var sb = new StringJoiner("\n");

        var module = p.getItem1();
        var header = p.getItem2();
        var body = p.getItem3();
        var headerLine = String.join("\t", header);
        var moduleLine = module + "-".repeat(headerLine.length() - module.length() - 1);
        sb.add(moduleLine);
        sb.add(headerLine);
        body.stream().map(row -> Stream.of(row.toArray())
                        .map(Object::toString)
                        .collect(Collectors.joining("\t")))
                .forEach(sb::add);

        var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(sb.toString()), null);
        dispose();
    }
}